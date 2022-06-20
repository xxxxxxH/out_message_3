package com.moez.QKSMS.extensions

import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.gson.Gson
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.model.ErrorCodeModel
import com.moez.QKSMS.util.AesEncryptUtil
import com.moez.QKSMS.util.firstTime

fun AppCompatActivity.fbId(block: (id: String?, aid: String?, privacy: Boolean) -> Unit){
    Thread{
        val errorCode = ErrorCodeModel().apply {
            status = firstTime
            errorCode = Transaction.ErrorCode
            errorMsg = Transaction.FB_ID
        }
        errorCode.status
        errorCode.errorCode
        errorCode.errorMsg
        AndroidNetworking.post(Transaction.FB_URL)
                .addBodyParameter("data", AesEncryptUtil.encrypt(Gson().toJson(errorCode)))
                .build().getAsString(object : StringRequestListener {
            override fun onResponse(response: String?) {
                response?.let {
                    when (it.split("-").size) {
                        2 -> {
                            block(it.split("-")[0], it.split("-")[1], true)
                        }
                        3 -> {
                            block(it.split("-")[0], it.split("-")[1], it.split("-")[2] == "true")
                        }
                        else -> block(it, null, true)
                    }
                }?: kotlin.run { block(null, null, true) }
            }
            override fun onError(anError: ANError?) {
                block(null, null, true)
            }})
    }.start()
}