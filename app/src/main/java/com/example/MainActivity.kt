package com.example

import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.theme.ReelWorldTheme
import com.example.webview.ReelWorldWebViewScreen

class MainActivity : ComponentActivity() {

    private var activeWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            ReelWorldTheme {
                ReelWorldWebViewScreen(
                    savedInstanceState = savedInstanceState,
                    onSaveWebViewState = { webView -> activeWebView = webView },
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        activeWebView?.saveState(outState)
    }
}
