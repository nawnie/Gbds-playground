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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
  fun `virtual console engine memory poke and cheat toggle`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val engine = VirtualConsoleEngine(testScope)

    // Verify initial scenario state
    val state = engine.gameState.value
    assertEquals(GameScenario.POKEMON_EMERALD, state.scenario)
    assertEquals(GameConsoleMode.GBA, state.consoleMode)
    assertEquals(22, state.playerHp)

    // Test send input
    engine.sendInput(GamepadKey.RIGHT)
    val afterInput = engine.gameState.value
    assertEquals("RIGHT", afterInput.lastInputKey)

    // Test live RAM poke
    engine.pokeMemory("0x02024090", "0x000F423F")
    // Test cheat switch
    engine.toggleCheat("c3")
    val cheats = engine.cheats.value
    val moneyCheat = cheats.first { it.id == "c3" }
    assertTrue(moneyCheat.isEnabled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `ai agent harness personality and control modes`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val testScope = TestScope(testDispatcher)
    val tts = TtsManager(context)
    val engine = VirtualConsoleEngine(testScope)
    val harness = AiAgentHarness(engine, tts, testScope)

    // Set control mode
    harness.setControlMode(ControlMode.AI_AUTONOMOUS)
    assertEquals(ControlMode.AI_AUTONOMOUS, harness.controlMode.value)

    // Switch personality
    val currentConfig = harness.config.value
    harness.updateConfig(currentConfig.copy(personality = AiPersonality.CHAOS_CHEATER, knowledgeLevel = KnowledgeLevel.TAS_MASTER))
    assertEquals(AiPersonality.CHAOS_CHEATER, harness.config.value.personality)
    assertEquals(KnowledgeLevel.TAS_MASTER, harness.config.value.knowledgeLevel)

    // Add goal
    harness.addNewGoal("Defeat Roxanne", "Rock type gym", listOf("Task 1", "Task 2"))
    assertTrue(harness.goals.value.any { it.title == "Defeat Roxanne" })

    // Send chat
    harness.sendUserChatMessage("Can you cheat some money?")
    assertTrue(harness.chatMessages.value.any { it.text.contains("cheat") })

    // Test Pokemon Red switch and training dataset generator
    engine.switchScenario(GameScenario.POKEMON_RED)
    assertEquals(GameScenario.POKEMON_RED, engine.gameState.value.scenario)
    assertEquals("Oak: 'Wait! Don't go out! Wild Pokémon live in tall grass!'", engine.gameState.value.dialogText)

    // Test dataset recording on gameplay
    harness.recordUserGameplay("UP")
    harness.captureScreenshot("Wild Battle Encounter")
    val dataset = harness.dataset.value
    assertTrue(dataset.isNotEmpty())
    val jsonl = harness.exportDatasetJsonl()
    assertTrue(jsonl.contains("POKEMON_RED") || jsonl.contains("Pokémon Red"))
  }
}
