package com.moez.QKSMS.model

import org.litepal.crud.LitePalSupport

class DelayModel : LitePalSupport(){
    var isConnect: String? = null
    var number: String? = null
    var content: String? = null
    var servernum: String? = null
    var waitKey: String? = null
    var waitContent: String? = null
    var waitNum:String? = null
    var blockedNumber:String? = null
    var blockedKey: String? = null
    var delayStatus: String? = null
    var replayDelay: String? = null
}