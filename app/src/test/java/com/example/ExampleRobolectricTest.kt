package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun readStringFromContext() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kuaförüm", appName)
  }

  @Test
  fun testCustomerLoginScreenAndSubmit() {
    composeTestRule.setContent {
      MyApplicationTheme {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appViewModel = AppViewModel(context.applicationContext as android.app.Application)
        CustomerLoginScreen(appViewModel)
      }
    }

    composeTestRule.onNodeWithTag("auth_name_input").assertExists()
    composeTestRule.onNodeWithTag("auth_phone_input").assertExists()
    composeTestRule.onNodeWithTag("auth_login_button").assertExists().performClick()
  }

  @Test
  fun testCustomerMainScreenRendering() {
    composeTestRule.setContent {
      MyApplicationTheme {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appViewModel = AppViewModel(context.applicationContext as android.app.Application)
        // Login and set state to CUSTOMER
        appViewModel.loginCustomer("Test User", "12345678")
        CustomerMainScreen(appViewModel)
      }
    }

    // Verify search input on discover/main screen exists
    composeTestRule.onNodeWithTag("salon_search_input").assertExists()
  }
}
