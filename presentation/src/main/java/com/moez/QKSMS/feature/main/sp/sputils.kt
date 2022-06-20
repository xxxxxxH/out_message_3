package com.moez.QKSMS.feature.main.sp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.next(clazz: Class<*>, isFinish:Boolean = false){
    startActivity(Intent(this, clazz))
    if (isFinish){
        finish()
    }
}