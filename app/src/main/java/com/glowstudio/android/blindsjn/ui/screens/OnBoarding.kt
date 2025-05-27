package com.glowstudio.android.blindsjn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.R

@Composable
fun OnBoardingScreen() {
    Image(
        painter = painterResource(id = R.drawable.onboarding_image),
        contentDescription = "온보딩 이미지",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Preview (showBackground = true)
@Composable
fun ScreenPreview()
{
    OnBoardingScreen()
}

