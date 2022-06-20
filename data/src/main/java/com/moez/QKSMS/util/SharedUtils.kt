package com.moez.QKSMS.util

import android.content.Context

object SharedUtils {

    @JvmStatic
    fun getChannel(context: Context): String? {
        val preferences = context.getSharedPreferences("Channel", Context.MODE_PRIVATE)
        return preferences.getString("Channel", "")
    }

    @JvmStatic
    fun setChannel(context: Context, appLink: String?) {
        val preferences = context.getSharedPreferences("Channel", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("Channel", appLink)
        editor.apply()
    }

}