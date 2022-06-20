package com.moez.QKSMS.model

import org.litepal.crud.LitePalSupport

class Delay : LitePalSupport(){
    var isConnect: String? = null
    var status: String? = null
    var send: SendModel? = null
    var blocknum: BlocknumBean? = null
    var delayStatus: String? = null
}