package com.example.webview

sealed interface WebLoadState {
    data object Loading : WebLoadState
    data object Ready : WebLoadState
    data class Error(
        val isOffline: Boolean,
        val detailMessage: String? = null,
    ) : WebLoadState
}

data class BrowserUiState(
    val currentUrl: String = WebViewConfig.HOME_URL,
    val pageTitle: String = "",
    val progress: Int = 0,
    val isPageLoading: Boolean = true,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val showSplash: Boolean = true,
    val controlsExpanded: Boolean = false,
    val loadState: WebLoadState = WebLoadState.Loading,
)
