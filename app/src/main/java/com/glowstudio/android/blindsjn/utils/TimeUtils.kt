package com.glowstudio.android.blindsjn.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    // String 형태의 날짜를 파싱해서 "~ 전" 표시를 돌려줍니다.
    fun getTimeAgo(dateStr: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = try {
            dateFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        } ?: return dateStr

        // UTC → KST 변환 (9시간 더하기)
        val kstDate = Date(date.time + 9 * 60 * 60 * 1000)
        val now = Date()
        val diff = now.time - kstDate.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            seconds < 60 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            weeks < 4 -> "${weeks}주 전"
            months < 12 -> "${months}달 전"
            else -> "${years}년 전"
        }
    }
} 