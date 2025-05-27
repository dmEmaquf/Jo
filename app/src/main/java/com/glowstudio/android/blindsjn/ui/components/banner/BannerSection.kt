package com.glowstudio.android.blindsjn.ui.components.banner

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.glowstudio.android.blindsjn.R
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BannerSection() {
    val pagerState = rememberPagerState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalPager(
            count = 4,
            state = pagerState,
        ) { page ->
            if (page == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )

                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        factory = { context ->
                            val adWidth = (context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density).toInt()
                            AdView(context).apply {
                                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
                                adUnitId = "ca-app-pub-3940256099942544/6300978111" // 테스트용
                                loadAd(AdRequest.Builder().build())
                            }
                        }
                    )

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.login_image),
                    contentDescription = "배너 이미지 $page",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}