package com.example.webview

import android.app.Activity
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.ReelCrimson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReelWorldWebViewScreen(
    savedInstanceState: Bundle?,
    onSaveWebViewState: (WebView?) -> Unit,
    viewModel: BrowserViewModel = viewModel(),
    isNetworkConnected: (() -> Boolean)? = null,
) {
    val context = LocalContext.current
    val checkNetworkConnected = remember(context, isNetworkConnected) {
        isNetworkConnected ?: { WebViewConfig.isNetworkConnected(context) }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var customFullscreenView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    LaunchedEffect(uiState.showSplash) {
        if (uiState.showSplash) {
            delay(3500L)
            viewModel.dismissSplash()
        }
    }

    BackHandler(
        enabled = customFullscreenView != null || uiState.controlsExpanded || uiState.canGoBack,
    ) {
        when {
            customFullscreenView != null -> {
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
                customFullscreenView = null
            }
            uiState.controlsExpanded -> {
                viewModel.collapseControls()
            }
            webViewRef?.canGoBack() == true -> {
                webViewRef?.goBack()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onSaveWebViewState(webViewRef)
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
        }
    }

    val openExternalUrl: (Uri) -> Unit = { uri ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.external_link_error))
            }
        }
    }

    val triggerReload: (String?) -> Unit = { targetUrl ->
        val connected = checkNetworkConnected()
        viewModel.prepareReload(connected)
        if (connected) {
            if (targetUrl != null) {
                webViewRef?.loadUrl(targetUrl)
            } else {
                webViewRef?.reload()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reelworld_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        WebViewConfig.configure(this)

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?,
                            ): Boolean {
                                val uri = request?.url ?: return false
                                return if (WebViewConfig.isInAppUrl(uri)) {
                                    false
                                } else {
                                    openExternalUrl(uri)
                                    true
                                }
                            }

                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?,
                            ) {
                                viewModel.onPageStarted(
                                    url = url,
                                    canGoBack = view?.canGoBack() == true,
                                    canGoForward = view?.canGoForward() == true,
                                )
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                CookieManager.getInstance().flush()
                                viewModel.onPageFinished(
                                    url = url,
                                    title = view?.title,
                                    canGoBack = view?.canGoBack() == true,
                                    canGoForward = view?.canGoForward() == true,
                                )
                            }

                            override fun doUpdateVisitedHistory(
                                view: WebView?,
                                url: String?,
                                isReload: Boolean,
                            ) {
                                viewModel.onNavigationHistoryUpdated(
                                    canGoBack = view?.canGoBack() == true,
                                    canGoForward = view?.canGoForward() == true,
                                )
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                if (request?.isForMainFrame == true) {
                                    val offline = !checkNetworkConnected()
                                    viewModel.onMainFrameError(
                                        isOffline = offline,
                                        description = error?.description?.toString(),
                                    )
                                }
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?,
                            ) {
                                handler?.cancel()
                                viewModel.onSslError(ctx.getString(R.string.error_ssl_message))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        ctx.getString(R.string.ssl_blocked_notice),
                                    )
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.onProgressChanged(newProgress)
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                callback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?,
                            ): Boolean {
                                filePathCallback?.onReceiveValue(null)
                                filePathCallback = callback
                                val intent = fileChooserParams?.createIntent()
                                if (intent == null) {
                                    filePathCallback?.onReceiveValue(null)
                                    filePathCallback = null
                                    return false
                                }
                                return try {
                                    fileChooserLauncher.launch(intent)
                                    true
                                } catch (_: ActivityNotFoundException) {
                                    filePathCallback?.onReceiveValue(null)
                                    filePathCallback = null
                                    false
                                }
                            }

                            override fun onShowCustomView(
                                view: View?,
                                callback: CustomViewCallback?,
                            ) {
                                if (customFullscreenView != null) {
                                    callback?.onCustomViewHidden()
                                    return
                                }
                                customFullscreenView = view
                                customViewCallback = callback
                            }

                            override fun onHideCustomView() {
                                customFullscreenView = null
                                customViewCallback?.onCustomViewHidden()
                                customViewCallback = null
                            }
                        }

                        setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                            enqueueSystemDownload(
                                context = ctx,
                                url = url,
                                userAgent = userAgent,
                                contentDisposition = contentDisposition,
                                mimeType = mimeType,
                                onFallbackOpen = openExternalUrl,
                                onNotify = { msg ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                },
                            )
                        }

                        webViewRef = this

                        if (savedInstanceState != null) {
                            val restored = restoreState(savedInstanceState)
                            if (restored == null) {
                                if (checkNetworkConnected()) {
                                    loadUrl(WebViewConfig.HOME_URL)
                                } else {
                                    viewModel.onMainFrameError(isOffline = true, description = null)
                                }
                            } else {
                                viewModel.dismissSplash()
                            }
                        } else {
                            if (checkNetworkConnected()) {
                                loadUrl(WebViewConfig.HOME_URL)
                            } else {
                                viewModel.onMainFrameError(isOffline = true, description = null)
                            }
                        }
                    }
                },
            )

            // Thin cinematic gradient loading bar at top edge
            TopLoadingBar(
                progress = uiState.progress,
                visible = uiState.isPageLoading && !uiState.showSplash,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            // Custom offline / error screen overlay
            val currentLoadState = uiState.loadState
            AnimatedVisibility(
                visible = currentLoadState is WebLoadState.Error,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                if (currentLoadState is WebLoadState.Error) {
                    ErrorOverlay(
                        error = currentLoadState,
                        onRetry = { triggerReload(null) },
                        onGoHome = { triggerReload(WebViewConfig.HOME_URL) },
                    )
                }
            }

            // Dismiss scrim when quick controls are expanded
            if (uiState.controlsExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.collapseControls() },
                        ),
                )
            }

            // Collapsible floating cinema quick-controls pill
            if (!uiState.showSplash && customFullscreenView == null) {
                FloatingControlBar(
                    expanded = uiState.controlsExpanded,
                    canGoBack = uiState.canGoBack,
                    canGoForward = uiState.canGoForward,
                    onToggle = { viewModel.toggleControls() },
                    onBack = {
                        webViewRef?.goBack()
                        viewModel.collapseControls()
                    },
                    onForward = {
                        webViewRef?.goForward()
                        viewModel.collapseControls()
                    },
                    onReload = {
                        triggerReload(null)
                        viewModel.collapseControls()
                    },
                    onHome = {
                        triggerReload(WebViewConfig.HOME_URL)
                        viewModel.collapseControls()
                    },
                    onShare = {
                        shareUrl(context, uiState.currentUrl, uiState.pageTitle)
                        viewModel.collapseControls()
                    },
                    onOpenExternal = {
                        openExternalUrl(Uri.parse(uiState.currentUrl))
                        viewModel.collapseControls()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp),
                )
            }

            // Fullscreen HTML5 video container
            customFullscreenView?.let { fsView ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ObsidianBg),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            (fsView.parent as? ViewGroup)?.removeView(fsView)
                            addView(
                                fsView,
                                FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                ),
                            )
                        }
                    },
                )
            }

            // Branded Cinema Midnight Splash overlay
            AnimatedVisibility(
                visible = uiState.showSplash,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SplashOverlay(progress = uiState.progress)
            }
        }
    }
}

@Composable
private fun TopLoadingBar(
    progress: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = (progress / 100f).coerceIn(0.08f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "webview_progress",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(OutlineSubtle.copy(alpha = 0.5f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(CinemaGold, ReelCrimson),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun SplashOverlay(progress: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progress / 100f).coerceIn(0.12f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "splash_progress",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_reelworld_splash_logo_1794715177425),
                contentDescription = stringResource(R.string.cd_brand_emblem),
                modifier = Modifier
                    .size(168.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .testTag("splash_logo"),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.headlineMedium,
                color = IvoryText,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MutedSlate,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(OutlineSubtle),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(CinemaGold, ReelCrimson),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun ErrorOverlay(
    error: WebLoadState.Error,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_reelworld_offline_1794715178252),
                contentDescription = stringResource(R.string.cd_offline_illustration),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(
                    if (error.isOffline) R.string.offline_title else R.string.error_title,
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = IvoryText,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = error.detailMessage?.takeIf { it.isNotBlank() && !error.isOffline }
                    ?: stringResource(R.string.offline_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MutedSlate,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .height(48.dp)
                        .testTag("retry_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = ObsidianBg,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_retry),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .height(48.dp)
                        .testTag("error_home_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, OutlineSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IvoryText),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_go_home),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingControlBar(
    expanded: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onToggle: () -> Unit,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit,
    onHome: () -> Unit,
    onShare: () -> Unit,
    onOpenExternal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 2 }) + scaleIn(initialScale = 0.92f),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }) + scaleOut(targetScale = 0.92f),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GlassSurface,
                border = BorderStroke(1.dp, OutlineSubtle),
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        enabled = canGoBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = if (canGoBack) IvoryText else MutedSlate.copy(alpha = 0.35f),
                        )
                    }
                    IconButton(
                        onClick = onForward,
                        enabled = canGoForward,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("nav_forward_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_forward),
                            tint = if (canGoForward) IvoryText else MutedSlate.copy(alpha = 0.35f),
                        )
                    }
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reload_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.cd_reload),
                            tint = CinemaGold,
                        )
                    }
                    IconButton(
                        onClick = onHome,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("home_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(R.string.cd_home),
                            tint = IvoryText,
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("share_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = stringResource(R.string.cd_share),
                            tint = IvoryText,
                        )
                    }
                    IconButton(
                        onClick = onOpenExternal,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_browser_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.cd_open_browser),
                            tint = IvoryText,
                        )
                    }
                }
            }
        }

        Surface(
            onClick = onToggle,
            shape = CircleShape,
            color = if (expanded) ReelCrimson else GlassSurface,
            border = BorderStroke(1.dp, if (expanded) ReelCrimson else OutlineSubtle),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(48.dp)
                .testTag("toggle_controls_button"),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.MoreHoriz,
                    contentDescription = stringResource(R.string.cd_toggle_controls),
                    tint = IvoryText,
                )
            }
        }
    }
}

private fun shareUrl(context: Context, url: String, title: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        if (title.isNotBlank()) putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, url)
    }
    val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title))
    if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

private fun enqueueSystemDownload(
    context: Context,
    url: String,
    userAgent: String,
    contentDisposition: String,
    mimeType: String,
    onFallbackOpen: (Uri) -> Unit,
    onNotify: (String) -> Unit,
) {
    val parsedUri = Uri.parse(url)
    if (parsedUri.scheme?.lowercase() !in setOf("http", "https")) {
        onFallbackOpen(parsedUri)
        return
    }
    try {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(parsedUri).apply {
            setMimeType(mimeType)
            addRequestHeader("User-Agent", userAgent)
            CookieManager.getInstance().getCookie(url)?.let { cookie ->
                addRequestHeader("Cookie", cookie)
            }
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        onNotify(context.getString(R.string.download_started))
    } catch (_: Exception) {
        onFallbackOpen(parsedUri)
    }
}
