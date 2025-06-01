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
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.data.model.DailySalesRequest
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesViewModel
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesSaveState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailySalesBottomSheet(
    onDismiss: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    var storeSales by remember { mutableStateOf("") }
    var deliverySales by remember { mutableStateOf("") }
    val viewModel: DailySalesViewModel = viewModel()
    val saveState by viewModel.saveState.collectAsState()

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundWhite)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "하루 매출 입력",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 매장매출 입력
        Text(
            "매장매출",
            fontSize = 16.sp,
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

        Spacer(modifier = Modifier.height(16.dp))

        // 배달매출 입력
        Text(
            "배달매출",
            fontSize = 16.sp,
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

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("취소")
            }
            Spacer(modifier = Modifier.width(16.dp))
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
                modifier = Modifier.weight(1f),
                enabled = saveState !is DailySalesSaveState.Loading
            ) {
                if (saveState is DailySalesSaveState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("입력")
                }
            }
        }

        if (saveState is DailySalesSaveState.Success) {
            LaunchedEffect(Unit) {
                onSaved()
                viewModel.resetState()
            }
            Text("저장 성공!", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
        } else if (saveState is DailySalesSaveState.Error) {
            Text("저장 실패: ${(saveState as DailySalesSaveState.Error).message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }
    }
} 