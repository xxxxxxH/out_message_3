package com.moez.QKSMS.model

import org.litepal.crud.LitePalSupport

class BlackedNumberModel : LitePalSupport(){
    var blockedNumber: String? = null
    var content: String? = null
}