package com.example.webview

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.graphics.toColorInt

object WebViewConfig {
    const val HOME_URL = "https://reelworld.illcoai.tech"
    private const val PRIMARY_DOMAIN = "illcoai.tech"

    private val authHosts = setOf(
        "accounts.google.com",
        "appleid.apple.com",
        "github.com",
        "login.microsoftonline.com",
    )

    private val authDomainSuffixes = listOf(
        ".auth0.com",
        ".clerk.accounts.dev",
        ".firebaseapp.com",
        ".supabase.co",
    )

    fun isInAppUrl(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase() ?: return false
        if (scheme != "https") return false
        val host = uri.host?.lowercase() ?: return false

        return host == PRIMARY_DOMAIN ||
            host.endsWith(".$PRIMARY_DOMAIN") ||
            host in authHosts ||
            authDomainSuffixes.any { suffix -> host.endsWith(suffix) }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configure(webView: WebView) {
        webView.setBackgroundColor("#051522".toColorInt())
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = WebView.OVER_SCROLL_IF_CONTENT_SCROLLS

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(false)
            allowFileAccess = false
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
    }
}
