package com.glowstudio.android.blindsjn.feature.ocr.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.data.model.DailySalesRequest
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesViewModel
import com.glowstudio.android.blindsjn.feature.ocr.viewmodel.DailySalesSaveState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.glowstudio.android.blindsjn.ui.components.common.CommonTextField
import com.glowstudio.android.blindsjn.ui.components.common.CommonButton

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
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommonTextField(
                value = storeSales,
                onValueChange = { storeSales = it.filter { c -> c.isDigit() } },
                label = "매장 매출",
                placeholder = "단위: 원",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            CommonTextField(
                value = deliverySales,
                onValueChange = { deliverySales = it.filter { c -> c.isDigit() } },
                label = "배달 매출",
                placeholder = "단위: 원",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        CommonButton(
            text = "입력",
            onClick = {
                val store = storeSales.toDoubleOrNull() ?: 0.0
                val delivery = deliverySales.toDoubleOrNull() ?: 0.0
                val req = DailySalesRequest(
                    date = today,
                    store_sales_amount = store,
                    delivery_sales_amount = delivery
                )
                viewModel.saveDailySales(req)
                onDismiss()
            },
            isLoading = saveState is DailySalesSaveState.Loading
        )
    }
} 