package com.glowstudio.android.blindsjn.feature.foodcost.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.glowstudio.android.blindsjn.feature.foodcost.viewmodel.RecipeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.glowstudio.android.blindsjn.feature.foodcost.model.Recipe

@Composable
fun RecipeListScreen(
    onEditRecipeClick: (String) -> Unit = {},
    onRegisterRecipeClick: () -> Unit = {}
) {
    val viewModel: RecipeViewModel = viewModel()
    val recipes by viewModel.recipeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getRecipeList(1) // TODO: 실제 앱에서는 로그인 정보에서 business_id 받아야 함
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
            Text("이름", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text("판매가", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text("마진", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.width(48.dp))
        }
        HorizontalDivider(color = DividerGray, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error ?: "오류가 발생했습니다.", color = Color.Red)
            }
        } else if (recipes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("등록된 레시피가 없습니다.", color = TextSecondary)
            }
        } else {
            recipes.forEach { recipe ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(CardWhite, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(recipe.title, Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                    Text("${recipe.price}원", Modifier.weight(1f), fontSize = 16.sp, color = TextPrimary)
                    Text(
                        "${recipe.margin_info.margin_percentage}%",
                        Modifier.weight(1f),
                        fontSize = 16.sp,
                        color = if (recipe.margin_info.margin_percentage >= 0) Color.Green else Color.Red
                    )
                    TextButton(onClick = { onEditRecipeClick(recipe.title) }) {
                        Text("수정", color = Blue, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
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