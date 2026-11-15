package com.example.webview

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun onPageStarted(url: String?, canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { state ->
            state.copy(
                currentUrl = url ?: state.currentUrl,
                isPageLoading = true,
                progress = if (state.progress == 100) 10 else state.progress.coerceAtLeast(10),
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                loadState = WebLoadState.Loading,
            )
        }
    }

    fun onProgressChanged(newProgress: Int) {
        _uiState.update { state ->
            val shouldHideSplash = state.showSplash && newProgress >= 85
            state.copy(
                progress = newProgress.coerceIn(0, 100),
                isPageLoading = newProgress < 100,
                showSplash = if (shouldHideSplash) false else state.showSplash,
            )
        }
    }

    fun onPageFinished(
        url: String?,
        title: String?,
        canGoBack: Boolean,
        canGoForward: Boolean,
    ) {
        _uiState.update { state ->
            val nextLoadState =
                if (state.loadState is WebLoadState.Error) {
                    state.loadState
                } else {
                    WebLoadState.Ready
                }
            state.copy(
                currentUrl = url ?: state.currentUrl,
                pageTitle = title.orEmpty(),
                progress = 100,
                isPageLoading = false,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                showSplash = false,
                loadState = nextLoadState,
            )
        }
    }

    fun onNavigationHistoryUpdated(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { state ->
            state.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward,
            )
        }
    }

    fun onMainFrameError(isOffline: Boolean, description: String?) {
        _uiState.update { state ->
            state.copy(
                isPageLoading = false,
                progress = 0,
                showSplash = false,
                loadState = WebLoadState.Error(
                    isOffline = isOffline,
                    detailMessage = description,
                ),
            )
        }
    }

    fun onSslError(message: String) {
        _uiState.update { state ->
            state.copy(
                isPageLoading = false,
                progress = 0,
                showSplash = false,
                loadState = WebLoadState.Error(
                    isOffline = false,
                    detailMessage = message,
                ),
            )
        }
    }

    fun prepareReload(isNetworkConnected: Boolean) {
        _uiState.update { state ->
            if (!isNetworkConnected) {
                state.copy(
                    isPageLoading = false,
                    showSplash = false,
                    loadState = WebLoadState.Error(isOffline = true),
                )
            } else {
                state.copy(
                    isPageLoading = true,
                    progress = 10,
                    loadState = WebLoadState.Loading,
                )
            }
        }
    }

    fun dismissSplash() {
        _uiState.update { state ->
            if (state.showSplash) state.copy(showSplash = false) else state
        }
    }

    fun toggleControls() {
        _uiState.update { state ->
            state.copy(controlsExpanded = !state.controlsExpanded)
        }
    }

    fun collapseControls() {
        _uiState.update { state ->
            if (state.controlsExpanded) state.copy(controlsExpanded = false) else state
        }
    }
}
