package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun RecipeListScreen(
    onEditRecipeClick: (String) -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {}
) {
    val viewModel: com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val recipeList by viewModel.recipeList.collectAsState()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.getRecipeList(1) // businessId는 실제 값으로 대체 필요
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        Text("레시피", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = TextPrimary)
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("레시피 이름", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text("레시피 가격", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text("재료 가격", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text("마진", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
        }
        Divider(color = DividerGray, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))
        recipeList.forEach { recipe ->
            val margin = recipe.price - recipe.margin_info.total_ingredient_price.toInt()
            val marginRate = if (recipe.price > 0) (margin * 100f / recipe.price).toInt() else 0
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(CardWhite, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recipe.title, Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
                Text("%,d원".format(recipe.price), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
                Text("%,d원".format(recipe.margin_info.total_ingredient_price.toInt()), Modifier.weight(1f), fontSize = 14.sp, color = TextPrimary)
                Column(Modifier.weight(2f), horizontalAlignment = Alignment.Start) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.7f)
                            .height(12.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(marginRate / 100f)
                                .background(Blue, RoundedCornerShape(6.dp))
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("${margin}원 (${marginRate}%)", fontSize = 12.sp, color = Blue)
                }
                TextButton(onClick = { onEditRecipeClick(recipe.title) }) {
                    Text("수정", color = Blue, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onRegisterRecipeClick,
            colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("레시피 등록", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreenPreview() {
    RecipeListScreen()
} 