package com.glowstudio.android.blindsjn.feature.board.model

sealed class BoardCategory(
    val id: Int,
    val emoji: String,
    val title: String,
    val route: String,
    val group: String
) {
    // 소통 카테고리
    object FreeBoard : BoardCategory(1, "💬", "자유게시판", "freeBoard", "소통")
    object PopularBoard : BoardCategory(2, "🔥", "인기 게시판", "popularBoard", "소통")

    // 업종 카테고리
    object RestaurantCafe : BoardCategory(3, "🍴", "음식점 및 카페", "restaurant_cafe", "업종")
    object ShoppingRetail : BoardCategory(4, "🛍️", "쇼핑 및 리테일", "shopping_retail", "업종")
    object HealthMedical : BoardCategory(5, "💊", "건강 및 의료", "health_medical", "업종")
    object AccommodationTravel : BoardCategory(6, "🏨", "숙박 및 여행", "accommodation_travel", "업종")
    object EducationLearning : BoardCategory(7, "📚", "교육 및 학습", "education_learning", "업종")
    object LeisureEntertainment : BoardCategory(8, "🎮", "여가 및 오락", "leisure_entertainment", "업종")
    object FinancePublic : BoardCategory(9, "💰", "금융 및 공공기관", "finance_public", "업종")

    companion object {
        val allCategories = listOf(
            FreeBoard, PopularBoard,
            RestaurantCafe, ShoppingRetail, HealthMedical, AccommodationTravel,
            EducationLearning, LeisureEntertainment, FinancePublic
        )

        val communicationCategories = listOf(FreeBoard, PopularBoard)
        val industryCategories = listOf(
            RestaurantCafe, ShoppingRetail, HealthMedical, AccommodationTravel,
            EducationLearning, LeisureEntertainment, FinancePublic
        )
    }
}