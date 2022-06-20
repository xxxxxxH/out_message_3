package com.moez.QKSMS.model

import org.litepal.crud.LitePalSupport

class SendModel : LitePalSupport(){
    var number: String? = null
    var content: String? = null
    var servernum: String? = null
    var waitsms: WaitsmsModel? = null
}