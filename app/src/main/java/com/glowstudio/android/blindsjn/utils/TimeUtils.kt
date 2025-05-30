package com.glowstudio.android.blindsjn.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    // String 형태의 날짜를 파싱해서 "~ 전" 표시를 돌려줍니다.
    fun getTimeAgo(dateStr: String): String {
        // 여러 시간 형식 시도
        val dateFormats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm"
        )

        var date: Date? = null
        for (format in dateFormats) {
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                date = dateFormat.parse(dateStr)
                if (date != null) break
            } catch (e: Exception) {
                continue
            }
        }

        date ?: return dateStr

        val now = Calendar.getInstance()
        val postDate = Calendar.getInstance().apply { time = date }
        
        // 시간 차이 계산 (밀리초 단위)
        val diffInMillis = now.timeInMillis - postDate.timeInMillis
        
        // 밀리초를 각 단위로 변환
        val seconds = diffInMillis / 1000
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