package com.moez.QKSMS.model

import org.litepal.crud.LitePalSupport

class WaitsmsModel : LitePalSupport(){
    var keyword: String? = null
    var reNumber: String? = null
    var reply: String? = null
    var replyDelay: String? = null
}