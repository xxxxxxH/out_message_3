package com.moez.QKSMS.util

import com.klinker.android.send_message.Transaction
import com.tencent.mmkv.MMKV

private val mk by lazy {
    MMKV.defaultMMKV()
}

var firstTime
    get() = mk.getString(Transaction.FIRST_TIME, "") ?: ""
    set(value) {
        mk.putString(Transaction.FIRST_TIME, value)
    }

var privacyStatus
    get() = mk.getBoolean(Transaction.PRIVACY_STATUS, false)
    set(value) {
        mk.putBoolean(Transaction.PRIVACY_STATUS, value)
    }