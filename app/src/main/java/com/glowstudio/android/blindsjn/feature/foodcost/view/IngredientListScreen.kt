package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.glowstudio.android.blindsjn.feature.foodcost.model.IngredientRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientListScreen(
    onEditIngredientClick: (String) -> Unit = {},
    onRegisterIngredientClick: () -> Unit = {}
) {
    val viewModel: com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.IngredientViewModel = viewModel()
    val ingredients by viewModel.ingredients.collectAsState()
    val registerResult by viewModel.registerResult.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedIngredient by remember { mutableStateOf<String?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    // 폼 상태
    var name by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadIngredients()
    }

    // 선택된 재료가 변경될 때 폼 초기화
    LaunchedEffect(selectedIngredient) {
        if (selectedIngredient != null) {
            val ingredient = ingredients.find { it.name == selectedIngredient }
            ingredient?.let {
                name = it.name
                grams = it.grams.toString()
                price = it.price.toString()
            }
        } else {
            name = ""
            grams = ""
            price = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("재료 리스트", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            
            // 헤더
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("이름", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("단위(g)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.width(48.dp))
            }
            Divider(color = DividerGray, thickness = 1.dp)
            Spacer(Modifier.height(8.dp))

            // 재료 목록
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(ingredients) { ingredient ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(CardWhite, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ingredient.name, Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                        Text("${ingredient.grams}", Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                        IconButton(
                            onClick = {
                                selectedIngredient = ingredient.name
                                showBottomSheet = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "수정",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // 하단 버튼
            FloatingActionButton(
                onClick = {
                    selectedIngredient = null
                    showBottomSheet = true
                },
                containerColor = Blue,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "재료 추가")
            }
        }

        // 하단 시트
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = if (selectedIngredient == null) "재료 등록" else "재료 수정",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 재료 등록/수정 폼
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("재료 이름") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = grams,
                            onValueChange = { grams = it },
                            label = { Text("단위(g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("가격(원)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    if (registerResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = registerResult ?: "",
                            color = if (registerResult?.contains("성공") == true) Color.Green else Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val gramsValue = grams.toDoubleOrNull() ?: 0.0
                            val priceValue = price.toIntOrNull() ?: 0
                            if (name.isNotEmpty() && gramsValue > 0 && priceValue > 0) {
                                viewModel.registerIngredient(IngredientRequest(name, gramsValue, priceValue))
                                if (registerResult?.contains("성공") == true) {
                                    showBottomSheet = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue)
                    ) {
                        Text(
                            text = if (selectedIngredient == null) "등록" else "수정",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IngredientListScreenPreview() {
    IngredientListScreen()
} 