package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.webview.BrowserViewModel
import com.example.webview.WebLoadState
import com.example.webview.WebViewConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ReelWorld", appName)
    }

    @Test
    fun `isInAppUrl keeps reelworld and auth hosts in app`() {
        assertTrue(WebViewConfig.isInAppUrl(Uri.parse("https://reelworld.illcoai.tech")))
        assertTrue(WebViewConfig.isInAppUrl(Uri.parse("https://reelworld.illcoai.tech/watch/123")))
        assertTrue(WebViewConfig.isInAppUrl(Uri.parse("https://accounts.google.com/o/oauth2/auth")))
        assertFalse(WebViewConfig.isInAppUrl(Uri.parse("http://reelworld.illcoai.tech")))
        assertFalse(WebViewConfig.isInAppUrl(Uri.parse("https://external-site.example.com")))
        assertFalse(WebViewConfig.isInAppUrl(Uri.parse("mailto:hello@illcoai.tech")))
    }

    @Test
    fun `browser viewModel transitions splash loading and error states`() {
        val vm = BrowserViewModel()
        assertTrue(vm.uiState.value.showSplash)

        vm.onProgressChanged(90)
        assertFalse(vm.uiState.value.showSplash)

        vm.onPageFinished(
            url = WebViewConfig.HOME_URL,
            title = "ReelWorld",
            canGoBack = false,
            canGoForward = false,
        )
        assertEquals(WebLoadState.Ready, vm.uiState.value.loadState)
        assertFalse(vm.uiState.value.isPageLoading)

        vm.onMainFrameError(isOffline = true, description = "ERR_INTERNET_DISCONNECTED")
        assertTrue(vm.uiState.value.loadState is WebLoadState.Error)
        assertTrue((vm.uiState.value.loadState as WebLoadState.Error).isOffline)
    }
}
