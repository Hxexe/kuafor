package com.example.composeapp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

class AppComposeUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appRendersGreetingText() = runComposeUiTest {
        setContent { App() }
        onNodeWithText(SPIKE_GREETING).assertExists()
    }
}
