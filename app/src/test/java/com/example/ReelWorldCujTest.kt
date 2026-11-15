package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.theme.ReelWorldTheme
import com.example.webview.BrowserViewModel
import com.example.webview.ReelWorldWebViewScreen
import com.example.webview.WebLoadState
import com.example.webview.WebViewConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReelWorldCujTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cuj_splashDismissesOnPageFinished_andWebViewIsDisplayed() {
        val viewModel = BrowserViewModel()

        composeTestRule.setContent {
            ReelWorldTheme {
                ReelWorldWebViewScreen(
                    savedInstanceState = null,
                    onSaveWebViewState = {},
                    viewModel = viewModel,
                    isNetworkConnected = { true },
                )
            }
        }
        composeTestRule.waitForIdle()

        // Splash is initially visible while loading
        composeTestRule.onNodeWithTag("splash_logo").assertIsDisplayed()
        composeTestRule.onNodeWithText("ReelWorld GO").assertIsDisplayed()
        composeTestRule.onNodeWithText("Opening reelworld.illcoai.tech").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reelworld_webview").assertExists()

        // Simulate page load completion
        composeTestRule.runOnUiThread {
            viewModel.onPageFinished(
                url = WebViewConfig.HOME_URL,
                title = "ReelWorld",
                canGoBack = false,
                canGoForward = false,
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Opening reelworld.illcoai.tech").assertDoesNotExist()
        composeTestRule.onNodeWithTag("toggle_controls_button").assertIsDisplayed()
    }

    @Test
    fun cuj_floatingControlsExpand_andReflectHistoryNavigationState() {
        val viewModel = BrowserViewModel()

        composeTestRule.setContent {
            ReelWorldTheme {
                ReelWorldWebViewScreen(
                    savedInstanceState = null,
                    onSaveWebViewState = {},
                    viewModel = viewModel,
                    isNetworkConnected = { true },
                )
            }
        }
        composeTestRule.runOnUiThread {
            viewModel.onPageFinished(
                url = "https://reelworld.illcoai.tech/play",
                title = "ReelWorld Play",
                canGoBack = true,
                canGoForward = false,
            )
        }
        composeTestRule.waitForIdle()

        // Expand floating controls pill
        composeTestRule.onNodeWithTag("toggle_controls_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_back_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("nav_forward_button").assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithTag("reload_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("home_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("share_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("open_browser_button").assertIsDisplayed().assertIsEnabled()

        // Tapping toggle again collapses controls
        composeTestRule.onNodeWithTag("toggle_controls_button").performClick()
        composeTestRule.waitForIdle()
        assertFalse(viewModel.uiState.value.controlsExpanded)
    }

    @Test
    fun cuj_offlineErrorDisplaysIllustration_andRetryTransitionsBackToLoading() {
        val viewModel = BrowserViewModel()
        var online = false

        composeTestRule.setContent {
            ReelWorldTheme {
                ReelWorldWebViewScreen(
                    savedInstanceState = null,
                    onSaveWebViewState = {},
                    viewModel = viewModel,
                    isNetworkConnected = { online },
                )
            }
        }
        composeTestRule.waitForIdle()

        // Started offline -> Signal Lost screen appears immediately
        composeTestRule.onNodeWithText("Signal Lost").assertIsDisplayed()
        composeTestRule.onNodeWithTag("retry_button").assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag("error_home_button").assertIsDisplayed().assertIsEnabled()

        // Tapping retry while still offline keeps offline state
        composeTestRule.onNodeWithTag("retry_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.loadState is WebLoadState.Error)

        // Restore connectivity and tap Retry -> transitions to Loading
        online = true
        composeTestRule.onNodeWithTag("retry_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals(WebLoadState.Loading, viewModel.uiState.value.loadState)
        assertTrue(viewModel.uiState.value.isPageLoading)
    }

    @Test
    fun cuj_sslErrorShowsSecureConnectionWarning() {
        val viewModel = BrowserViewModel()

        composeTestRule.setContent {
            ReelWorldTheme {
                ReelWorldWebViewScreen(
                    savedInstanceState = null,
                    onSaveWebViewState = {},
                    viewModel = viewModel,
                    isNetworkConnected = { true },
                )
            }
        }
        composeTestRule.runOnUiThread {
            viewModel.onSslError("A secure connection to this page could not be verified.")
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Unable to Load Page").assertIsDisplayed()
        composeTestRule.onNodeWithText("A secure connection to this page could not be verified.")
            .assertIsDisplayed()
    }
}
