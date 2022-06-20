package com.moez.QKSMS.util

import android.annotation.SuppressLint
import android.net.ParseException
import android.text.format.Time
import java.text.SimpleDateFormat
import java.util.*


object DateUtils {
    // currentTime要转换的long类型的时间 // formatType要转换的string类型的时间格式
    @Throws(ParseException::class, java.text.ParseException::class)
    fun longToString(currentTime: Long, formatType: String? = "yyyy-MM-dd"): String? {
        val date = longToDate(
            currentTime,
            formatType
        )
        // long类型转成Date类型
        return dateToString(date, formatType)
    }

    @Throws(ParseException::class, java.text.ParseException::class)
    fun longToDate(currentTime: Long, formatType: String? = "yyyy-MM-dd"): Date? {
        val dateOld = Date(currentTime)
        // 根据long类型的毫秒数生命一个date类型的时间
        val sDateTime = dateToString(
            dateOld,
            formatType
        ) // 把date类型的时间转换为string
        return stringToDate(
            sDateTime,
            formatType
        )
    }

    @Throws(ParseException::class, java.text.ParseException::class)
    fun stringToDate(strTime: String?, formatType: String? = "yyyy-MM-dd"): Date? {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    @SuppressLint("SimpleDateFormat")
    fun dateToString(data: Date?, formatType: String? = "yyyy-MM-dd"): String? {
        return SimpleDateFormat(formatType).format(data)
    }

    @Throws(java.text.ParseException::class)
    fun today(formatType: String?="yyyy-MM-dd"): String? {
        return longToString(
            System.currentTimeMillis(),
            formatType
        )
    }

    @Throws(java.text.ParseException::class)
    fun isToday(dateStr: String, formatType: String?="yyyy-MM-dd"): Boolean {
        return longToString(
            System.currentTimeMillis(),
            formatType
        ) == dateStr
    }

    fun isCurrentInTimeScope(
            beginHour: Int = 9,
            beginMin: Int = 0,
            endHour: Int = 22,
            endMin: Int = 0
    ): Boolean {
        var result = false
        val aDayInMillis = 1000 * 60 * 60 * 24.toLong()
        val currentTimeMillis = System.currentTimeMillis()
        val now = Time()
        now.set(currentTimeMillis)
        val startTime = Time()
        startTime.set(currentTimeMillis)
        startTime.hour = beginHour
        startTime.minute = beginMin
        val endTime = Time()
        endTime.set(currentTimeMillis)
        endTime.hour = endHour
        endTime.minute = endMin
        if (!startTime.before(endTime)) {
            // 跨天的特殊情况（比如22:00-8:00）
            startTime.set(startTime.toMillis(true) - aDayInMillis)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
            val startTimeInThisDay = Time()
            startTimeInThisDay.set(startTime.toMillis(true) + aDayInMillis)
            if (!now.before(startTimeInThisDay)) {
                result = true
            }
        } else {
            // 普通情况(比如 8:00 - 14:00)
            result = !now.before(startTime) && !now.after(endTime) // startTime <= now <= endTime
        }
        return result
    }
}