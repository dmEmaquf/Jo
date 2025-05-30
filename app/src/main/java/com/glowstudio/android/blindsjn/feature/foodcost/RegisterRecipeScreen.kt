package com.glowstudio.android.blindsjn.feature.foodcost

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.glowstudio.android.blindsjn.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel
import com.glowstudio.android.blindsjn.feature.foodcost.model.RecipeIngredient
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Spring
import androidx.compose.material3.OutlinedTextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegisterRecipeScreen(
    onDismiss: () -> Unit = {},
    onTextFieldFocus: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var recipeItems by remember { mutableStateOf(listOf(RecipeItem())) }
    var searchQuery by remember { mutableStateOf("") }
    var showIngredientDropdown by remember { mutableStateOf(false) }
    var selectedIngredientIndex by remember { mutableStateOf<Int?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    var focusedItemIndex by remember { mutableStateOf<Int?>(null) }
    var selectedChip by remember { mutableStateOf<String?>(null) }
    var selectedChipGrams by remember { mutableStateOf("") }
    var selectedChips by remember { mutableStateOf(setOf<String>()) }
    var showAmountSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val amountSheetState = rememberModalBottomSheetState()
    var ingredientAmounts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    val viewModel: RecipeViewModel = viewModel()
    val registerResult by viewModel.registerResult.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()
    val businessId = 1
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.loadIngredients()
    }

    LaunchedEffect(focusedItemIndex) {
        focusedItemIndex?.let { index ->
            lazyListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(registerResult) {
        if (registerResult?.contains("성공") == true) {
            onDismiss()
            viewModel.clearResult()
            isRegistering = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        item {
            Text(
                text = "레시피 등록",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("레시피 제목") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) onTextFieldFocus?.invoke() },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "재료 목록",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "재료를 선택하세요",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ingredients.forEach { ingredient ->
                    val selected = selectedChips.contains(ingredient.name)
                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(selected) {
                        scale.animateTo(
                            targetValue = if (selected) 1.05f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }
                    Surface(
                        onClick = {
                            selectedChips = if (selected) selectedChips - ingredient.name else selectedChips + ingredient.name
                            if (!selected) ingredientAmounts = ingredientAmounts + (ingredient.name to "")
                            else ingredientAmounts = ingredientAmounts - ingredient.name
                        },
                        shape = RoundedCornerShape(24.dp),
                        color = if (selected) Blue else CardWhite,
                        border = if (!selected) BorderStroke(1.dp, Blue) else null,
                        modifier = Modifier
                            .scale(scale.value)
                            .animateContentSize()
                    ) {
                        Text(
                            text = ingredient.name,
                            color = if (selected) CardWhite else Blue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(selectedChips.toList()) { name ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                    OutlinedTextField(
                        value = ingredientAmounts[name] ?: "",
                        onValueChange = { newValue ->
                            ingredientAmounts = ingredientAmounts.toMutableMap().apply { put(name, newValue) }
                        },
                        placeholder = { Text("양", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(48.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
                        trailingIcon = { Text("g", color = Color.Gray, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent
                        )
                    )
                    IconButton(onClick = {
                        selectedChips = selectedChips - name
                        ingredientAmounts = ingredientAmounts - name
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("레시피 가격") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (it.isFocused) onTextFieldFocus?.invoke() },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (validationError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = validationError ?: "",
                    color = Color.Red,
                    fontSize = 14.sp
                )
            } else if (registerResult != null) {
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
                    if (isRegistering) return@Button
                    val ingredients = selectedChips.mapNotNull {
                        val n = it.trim()
                        val g = ingredientAmounts[it]?.toDoubleOrNull() ?: 0.0
                        if (n.isNotEmpty() && g > 0) RecipeIngredient(n, g) else null
                    }
                    val recipeTitle = title.trim()
                    val recipePrice = price.toLongOrNull() ?: 0L
                    if (recipeTitle.isEmpty()) {
                        validationError = "레시피 제목을 입력하세요."
                        return@Button
                    }
                    if (recipePrice <= 0) {
                        validationError = "레시피 가격을 올바르게 입력하세요."
                        return@Button
                    }
                    if (ingredients.isEmpty()) {
                        validationError = "재료를 1개 이상 입력하세요."
                        return@Button
                    }
                    validationError = null
                    isRegistering = true
                    viewModel.registerRecipe(recipeTitle, recipePrice, 1, ingredients)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                enabled = !isRegistering
            ) {
                Text(
                    text = isRegistering.let { if (it) "등록 중..." else "등록" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeItemRow(
    item: RecipeItem,
    itemIndex: Int,
    ingredients: List<com.glowstudio.android.blindsjn.feature.foodcost.model.Ingredient>,
    onItemChange: (RecipeItem) -> Unit,
    onDelete: () -> Unit,
    onFocus: (Int) -> Unit,
    onTextFieldFocus: (() -> Unit)? = null
) {
    var showDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    LaunchedEffect(isFocused) {
        if (isFocused) {
            onFocus(itemIndex)
        }
    }
    
    ExposedDropdownMenuBox(
        expanded = showDropdown && searchQuery.isNotEmpty(),
        onExpandedChange = { showDropdown = it }
    ) {
        OutlinedTextField(
            value = item.name,
            onValueChange = { 
                searchQuery = it
                showDropdown = true
                onItemChange(item.copy(name = it))
            },
            label = { Text("재료 검색") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .onFocusChanged { if (it.isFocused) onTextFieldFocus?.invoke() },
            singleLine = true,
            interactionSource = interactionSource,
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "검색",
                    tint = Color.LightGray
                )
            }
        )
        
        ExposedDropdownMenu(
            expanded = showDropdown && searchQuery.isNotEmpty(),
            onDismissRequest = { showDropdown = false }
        ) {
            ingredients
                .filter { it.name.contains(searchQuery, ignoreCase = true) }
                .forEach { ingredient ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "${ingredient.name} (${ingredient.grams}g)",
                                color = TextPrimary
                            )
                        },
                        onClick = {
                            onItemChange(item.copy(
                                name = ingredient.name,
                                grams = ""
                            ))
                            showDropdown = false
                        }
                    )
                }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = item.grams,
            onValueChange = { onItemChange(item.copy(grams = it)) },
            label = { Text("사용량(g)") },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) onTextFieldFocus?.invoke() },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "삭제",
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

data class RecipeItem(
    val name: String = "",
    val grams: String = ""
)

@Preview(showBackground = true)
@Composable
fun RegisterRecipeScreenPreview() {
    RegisterRecipeScreen()
}
