package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.AiAgentHarness
import com.example.engine.TtsManager
import com.example.engine.VirtualConsoleEngine
import com.example.model.AiPersonality
import com.example.model.ControlMode
import com.example.model.GameConsoleMode
import com.example.model.GamepadKey
import com.example.model.GameScenario
import com.example.model.KnowledgeLevel
import com.example.model.SystemLifecycleState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EdgePilot", appName)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `virtual console engine input contract, pause enforcement, and memory gatekeeper`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val engine = VirtualConsoleEngine(testScope)

    // 1. Initial Pokémon Red state
    val state = engine.gameState.value
    assertEquals(GameScenario.POKEMON_RED, state.scenario)
    assertEquals(GameConsoleMode.GBA, state.consoleMode)
    assertEquals(20, state.playerHp)

    // 2. Real input contract: KeyDown, KeyUp, Simultaneous multi-touch
    engine.onKeyDown(GamepadKey.UP)
    engine.onKeyDown(GamepadKey.RIGHT)
    val pressed = engine.gameState.value.pressedKeys
    assertTrue("Should hold UP and RIGHT simultaneously", pressed.contains(GamepadKey.UP) && pressed.contains(GamepadKey.RIGHT))

    engine.onKeyUp(GamepadKey.RIGHT)
    val remaining = engine.gameState.value.pressedKeys
    assertTrue("Should keep UP held after releasing RIGHT", remaining.contains(GamepadKey.UP))
    assertFalse("RIGHT should be released", remaining.contains(GamepadKey.RIGHT))

    engine.releaseAllKeys()
    assertTrue("All keys should be released", engine.gameState.value.pressedKeys.isEmpty())

    // 3. Pause enforcement
    engine.setPaused(true)
    assertTrue("Engine should be paused", engine.gameState.value.isPaused)
    engine.setPaused(false)
    assertFalse("Engine should be resumed", engine.gameState.value.isPaused)

    // 4. Unified Memory Gatekeeper: Rejects write when allowCheats = false
    val lockedPoke = engine.pokeMemory("0x02024090", "0x000F423F", allowCheats = false)
    assertTrue("Poke must fail when allowCheats is false", lockedPoke.isFailure)

    val lockedCheat = engine.toggleCheat("c3", allowCheats = false)
    assertTrue("Cheat toggle must fail when allowCheats is false", lockedCheat.isFailure)

    val allowedPoke = engine.pokeMemory("0x02024090", "0x000F423F", allowCheats = true)
    assertTrue("Poke must succeed when allowCheats is true", allowedPoke.isSuccess)

    val allowedCheat = engine.toggleCheat("c3", allowCheats = true)
    assertTrue("Cheat toggle must succeed when allowCheats is true", allowedCheat.isSuccess)
    assertTrue(engine.cheats.value.first { it.id == "c3" }.isEnabled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `dual screen 3DS mode and touch input`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val engine = VirtualConsoleEngine(testScope)

    engine.switchConsoleMode(GameConsoleMode.NDS_3DS)
    assertEquals(GameConsoleMode.NDS_3DS, engine.gameState.value.consoleMode)

    // Circle pad analog nudging
    engine.onCirclePad(0.7f, -0.4f)
    assertEquals(0.7f, engine.gameState.value.circlePadOffset.first, 0.01f)
    assertEquals(-0.4f, engine.gameState.value.circlePadOffset.second, 0.01f)

    // Bottom touch screen input
    engine.onTouchDown(0.25f, 0.25f)
    assertNotNull("Touch point should be active", engine.gameState.value.touchPoint)
    engine.onTouchUp()
    assertTrue("Touch point should clear on touch up", engine.gameState.value.touchPoint == null)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `ai agent harness lifecycle states, unified write guard, and real export`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val tts = TtsManager(context)
    val engine = VirtualConsoleEngine(testScope)
    val harness = AiAgentHarness(engine, tts, testScope)

    // Verify verified runtime status
    assertEquals(SystemLifecycleState.READY, harness.runtimeStatus.value.state)

    // Set control mode and ownership
    harness.setControlMode(ControlMode.AI_AUTONOMOUS)
    assertEquals(ControlMode.AI_AUTONOMOUS, harness.controlMode.value)

    // Test immediate human takeover notification
    harness.notifyHumanTakeover()
    assertEquals(com.example.model.InputOwner.HUMAN, engine.gameState.value.inputOwner)

    // Switch personality
    val currentConfig = harness.config.value
    harness.updateConfig(currentConfig.copy(personality = AiPersonality.CHAOS_CHEATER, knowledgeLevel = KnowledgeLevel.TAS_MASTER))
    assertEquals(AiPersonality.CHAOS_CHEATER, harness.config.value.personality)
    assertEquals(KnowledgeLevel.TAS_MASTER, harness.config.value.knowledgeLevel)

    // Add goal
    harness.addNewGoal("Defeat Brock", "Pewter Gym", listOf("Enter gym", "Attack Onix"))
    assertTrue(harness.goals.value.any { it.title == "Defeat Brock" })

    // Chat with memory permissions guard
    harness.updateConfig(harness.config.value.copy(memoryCheatsAllowed = false))
    harness.sendUserChatMessage("Can you cheat some money?")
    var waitMs = 0
    while (harness.chatMessages.value.none { it.text.contains("Permission Denied") || it.actionTag?.contains("Blocked") == true } && waitMs < 2000) {
      Thread.sleep(50)
      waitMs += 50
    }
    assertTrue("Should flag permission guard blocked", harness.chatMessages.value.any { it.text.contains("Permission Denied") || it.actionTag?.contains("Blocked") == true })

    // Test gameplay dataset recording and genuine file export
    harness.recordUserGameplay("UP")
    harness.captureScreenshot("Manual Test Frame")
    val dataset = harness.dataset.value
    assertTrue("Dataset should contain samples", dataset.isNotEmpty())

    val exportedFile = harness.exportDatasetToFile(context)
    assertTrue("Exported file must exist", exportedFile.exists())
    assertTrue("Exported file must have content", exportedFile.length() > 0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `grind bot deterministic shiny hunting and battle lifecycle`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val tts = TtsManager(context)
    val haptics = com.example.engine.HapticFeedbackManager(context)
    val engine = VirtualConsoleEngine(testScope)
    val grindBot = com.example.engine.GrindBotEngine(engine, tts, haptics, testScope)

    // Initial state: Stopped
    assertEquals(com.example.model.GrindBotState.STOPPED, grindBot.telemetry.value.currentState)
    assertFalse(grindBot.telemetry.value.isEnabled)

    // Set goal to SHINY_HUNT and start bot
    grindBot.startBot(com.example.model.GrindBotGoal.SHINY_HUNT)
    assertTrue(grindBot.telemetry.value.isEnabled)
    assertEquals(com.example.model.GrindBotGoal.SHINY_HUNT, grindBot.telemetry.value.goal)
    assertEquals(com.example.model.InputOwner.AI_AGENT, engine.gameState.value.inputOwner)

    // Test forcing a shiny encounter
    grindBot.forceShinyEncounter()

    // Stop bot
    grindBot.stopBot("Test stop")
    assertFalse(grindBot.telemetry.value.isEnabled)
    assertEquals(com.example.model.InputOwner.HUMAN, engine.gameState.value.inputOwner)
  }

  @Test
  fun `console customization shell, button, and haptic presets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val haptics = com.example.engine.HapticFeedbackManager(context)

    var config = com.example.model.ConsoleCustomizationConfig()
    assertEquals(com.example.model.ShellColorPreset.COBALT_BLUE, config.shellPreset)
    assertEquals(com.example.model.ButtonColorPreset.CLASSIC_CHARCOAL, config.buttonPreset)
    assertEquals(com.example.model.HapticProfile.MEDIUM, config.hapticProfile)

    // Switch to Pikachu Yellow with Super Famicom buttons and Retro Click haptics
    config = config.copy(
      shellPreset = com.example.model.ShellColorPreset.PIKACHU_CANARY,
      buttonPreset = com.example.model.ButtonColorPreset.SUPER_FAMICOM,
      hapticProfile = com.example.model.HapticProfile.RETRO_CLICK
    )
    assertEquals(com.example.model.ShellColorPreset.PIKACHU_CANARY, config.shellPreset)
    assertEquals(com.example.model.ButtonColorPreset.SUPER_FAMICOM, config.buttonPreset)
    assertEquals(com.example.model.HapticProfile.RETRO_CLICK, config.hapticProfile)

    // Verify haptic triggers execute cleanly without exception
    haptics.triggerPress(config)
    haptics.triggerRelease(config)
    haptics.triggerTouch(config)
    haptics.triggerShinyAlert()
    haptics.triggerCheatToggle()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `cheat engine syntax validation and toggling`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val engine = VirtualConsoleEngine(testScope)
    val cheatMgr = com.example.engine.CheatEngineManager(engine)

    // Initial authentic cheats loaded
    val initialCheats = cheatMgr.cheats.value
    assertTrue("Should have default cheat catalog", initialCheats.isNotEmpty())

    // Add a valid custom GameShark code
    val addResult = cheatMgr.addNewCheat(
      title = "Max PP All Moves",
      rawCode = "01FF45D1",
      type = com.example.model.CheatEngineType.GAMESHARK,
      description = "Restores all moves to maximum Power Points",
      scenario = null
    )
    assertTrue("GameShark code should be accepted", addResult.isSuccess)

    // Toggle cheat on with memory permission
    val addedId = addResult.getOrThrow().id
    val toggleRes = cheatMgr.toggleCheat(addedId, allowCheats = true)
    assertTrue(toggleRes.isSuccess)
    assertTrue(cheatMgr.cheats.value.first { it.id == addedId }.isEnabled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `dev tools hex generation and register bitfield toggles`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val engine = VirtualConsoleEngine(testScope)
    val devEngine = com.example.engine.DevToolsEngine(engine)

    // Hex rows generation
    val hexRows = devEngine.generateHexRows(com.example.model.MemoryRegion.EWRAM, 0, 4)
    assertEquals(4, hexRows.size)
    assertEquals("0x02000000", hexRows[0].addressHex)

    // Live byte writing
    val writeResult = devEngine.editByte("0x02000000", 0xFF)
    assertTrue(writeResult.isSuccess)
    assertEquals(0xFF, engine.readMemory("0x02000000"))

    // I/O Registers inspector
    val regs = devEngine.getIoRegisters()
    assertTrue("I/O registers should be populated", regs.isNotEmpty())
    val dispcnt = regs.first { it.name == "DISPCNT" }
    assertEquals("0x04000000", dispcnt.addressHex)

    // Speed multiplier and frame stepping
    devEngine.setSpeedMultiplier(2.0f)
    assertEquals(2.0f, devEngine.speedMultiplier.value, 0.01f)
    devEngine.stepSingleFrame()
    devEngine.rewindFrame()
  }
}
