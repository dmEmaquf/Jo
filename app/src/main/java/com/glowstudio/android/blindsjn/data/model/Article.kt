/*
* 뉴스 기사 아티클
*
* */

package com.glowstudio.android.blindsjn.data.model

import java.io.Serializable

data class Article(
    val title: String?,
    val description: String?,
    val url: String?,
    val content: String?,
    val link: String?,
    val urlToImage: String?
) : Serializable {

}