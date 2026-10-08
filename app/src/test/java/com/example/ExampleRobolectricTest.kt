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
    testScheduler.advanceTimeBy(500)
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
}
