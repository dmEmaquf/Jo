package com.glowstudio.android.blindsjn

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.ui.navigation.AppNavHost
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarViewModel
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // WebView Variations 서비스 비활성화
        WebView.setWebContentsDebuggingEnabled(false)
        WebView.enableSlowWholeDocumentDraw()
        
        setContent {
            BlindSJNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val topBarViewModel: TopBarViewModel = viewModel()
                    AppNavHost(
                        navController = navController
                    )
                }
            }
        }
    }
}

