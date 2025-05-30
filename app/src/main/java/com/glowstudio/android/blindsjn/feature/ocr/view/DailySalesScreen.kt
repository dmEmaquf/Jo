package com.glowstudio.android.blindsjn.feature.ocr.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.data.model.DailySalesRequest
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesViewModel
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesSaveState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailySalesScreen(
    onBackClick: () -> Unit = {}
) {
    var storeSales by remember { mutableStateOf("") }
    var deliverySales by remember { mutableStateOf("") }
    val viewModel: DailySalesViewModel = viewModel()
    val saveState by viewModel.saveState.collectAsState()

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "하루 매출 입력",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 매장매출 입력
            Text(
                "매장매출",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            OutlinedTextField(
                value = storeSales,
                onValueChange = { storeSales = it.filter { c -> c.isDigit() } },
                label = { Text("매장 매출 입력") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 배달매출 입력
            Text(
                "배달매출",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            OutlinedTextField(
                value = deliverySales,
                onValueChange = { deliverySales = it.filter { c -> c.isDigit() } },
                label = { Text("배달 매출 입력") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val store = storeSales.toDoubleOrNull() ?: 0.0
                    val delivery = deliverySales.toDoubleOrNull() ?: 0.0
                    val req = DailySalesRequest(
                        date = today,
                        store_sales_amount = store,
                        delivery_sales_amount = delivery
                    )
                    viewModel.saveDailySales(req)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = saveState !is DailySalesSaveState.Loading
            ) {
                if (saveState is DailySalesSaveState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("입력", fontSize = 18.sp)
                }
            }

            if (saveState is DailySalesSaveState.Success) {
                Text("저장 성공!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            } else if (saveState is DailySalesSaveState.Error) {
                Text("저장 실패: ${(saveState as DailySalesSaveState.Error).message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DailySalesScreenPreview() {
    BlindSJNTheme {
        DailySalesScreen()
    }
} 