package com.glowstudio.android.blindsjn.feature.main.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.R
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarState
import com.glowstudio.android.blindsjn.feature.main.viewmodel.TopBarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    state: TopBarState,
    modifier: Modifier = Modifier
) {
    when (state.type) {
        TopBarType.MAIN -> TopBarMain(
            modifier = modifier,
            onSearchClick = state.onSearchClick,
            onMoreClick = state.onMoreClick,
            onNotificationClick = state.onNotificationClick,
            showSearchButton = state.showSearchButton,
            showMoreButton = state.showMoreButton,
            showNotificationButton = state.showNotificationButton
        )
        TopBarType.DETAIL -> TopBarDetail(
            title = state.title,
            onBackClick = state.onBackClick,
            onSearchClick = state.onSearchClick,
            onMoreClick = state.onMoreClick,
            modifier = modifier,
            showSearchButton = state.showSearchButton,
            showMoreButton = state.showMoreButton
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarMain(
    modifier: Modifier = Modifier,
    onLogoClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    showSearchButton: Boolean = true,
    showMoreButton: Boolean = true,
    showNotificationButton: Boolean = true
) {
    CenterAlignedTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onLogoClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "로고",
                    tint = Color.Unspecified
                )
            }
        },
        actions = {
            if (showSearchButton) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "검색")
                }
            }
            if (showMoreButton) {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "더보기")
                }
            }
            if (showNotificationButton) {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Filled.NotificationsNone, contentDescription = "알림")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarDetail(
    title: String,
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showSearchButton: Boolean = true,
    showMoreButton: Boolean = true
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            if (showSearchButton) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Filled.Search, contentDescription = "검색")
                }
            }
            if (showMoreButton) {
                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "더보기")
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarMainPreview() {
    TopBarMain(
        onSearchClick = {},
        onMoreClick = {},
        onNotificationClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TopBarDetailPreview() {
    TopBarDetail(
        title = "상세화면",
        onBackClick = {},
        onSearchClick = {},
        onMoreClick = {}
    )
}
