package com.moez.QKSMS.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.extensions.log

class SendReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.let {
            if (resultCode == Activity.RESULT_OK){
                when(it.action){
                    Transaction.AUTO_SENT ->
                        "S Success".log()
                    Transaction.AUTO_RECEIVE ->
                        "D Success".log()
                }
            }else
                    "S Failed".log()
        }
    }
}