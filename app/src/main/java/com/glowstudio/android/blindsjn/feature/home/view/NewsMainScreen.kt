package com.glowstudio.android.blindsjn.feature.home.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.glowstudio.android.blindsjn.ui.theme.BlindSJNTheme

@Composable
fun NewsMainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "뉴스 메인",
            style = MaterialTheme.typography.headlineMedium
        )
        // TODO: Add more content here
    }
}

@Preview(showBackground = true)
@Composable
fun NewsMainScreenPreview() {
    BlindSJNTheme {
        val navController = rememberNavController()
        NewsMainScreen(navController = navController)
    }
}

