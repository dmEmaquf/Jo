package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
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

@Composable
fun EditRecipeScreen(
    recipeName: String = "빵",
    onEditIngredientClick: (String) -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    val viewModel: com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.IngredientViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val ingredients by viewModel.ingredients.collectAsState()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadIngredients()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        Text(recipeName, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("재료 이름", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text("단위(g)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.width(48.dp))
        }
        Divider(color = DividerGray, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))
        ingredients.forEach { ingredient ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ingredient.name, Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                Text("${ingredient.grams}", Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                TextButton(onClick = { onEditIngredientClick(ingredient.name) }) {
                    Text("수정", color = Blue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onSaveClick,
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("수정", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
} 