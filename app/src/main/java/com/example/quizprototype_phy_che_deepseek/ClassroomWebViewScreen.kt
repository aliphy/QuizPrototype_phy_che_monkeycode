package com.example.quizprototype_phy_che_deepseek

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomWebViewScreen(
    url: String,
    courseId: String,
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }

    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White,
                        onClick = onBack
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    // استخدام User Agent لمتصفح ديسكتوب للسماح بتسجيل الدخول وتجاوز الحظر
                    settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // إخفاء القائمة الجانبية والشعار لمنع التلميذ من مغادرة الفصل
                            view?.loadUrl("""
                                javascript:(function() {
                                    var hideItems = function() {
                                        var menuBtn = document.querySelector('div[aria-label="القائمة الرئيسية"], div[aria-label="Main menu"]');
                                        if (menuBtn) menuBtn.style.display = 'none';
                                        var logo = document.querySelector('a[href*="/h"]');
                                        if (logo) logo.style.display = 'none';
                                        var headerIcon = document.querySelector('div[role="button"] img');
                                        if (headerIcon) headerIcon.style.display = 'none';
                                    };
                                    hideItems();
                                    setInterval(hideItems, 1000); // تكرار المسح لضمان عدم ظهورها عند التحميل المتأخر
                                })()
                            """.trimIndent())
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val newUrl = request?.url?.toString() ?: return false
                            
                            // السماح بروابط الفصل أو روابط جوجل الأساسية (حسابات، درايف)
                            if (newUrl.contains(courseId) || newUrl.contains("google.com/accounts") || 
                                newUrl.contains("accounts.google.com") || newUrl.contains("drive.google.com")) {
                                return false
                            }

                            // حظر العودة للقائمة الرئيسية
                            if (newUrl.contains("classroom.google.com/h") || newUrl.endsWith("/h")) {
                                Toast.makeText(context, "عذراً، لا يمكنك مغادرة هذا الفصل", Toast.LENGTH_SHORT).show()
                                return true
                            }
                            return false
                        }
                    }
                    loadUrl(url)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
