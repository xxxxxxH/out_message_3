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
package com.moez.QKSMS.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.extensions.autoReply
import com.moez.QKSMS.extensions.log
import com.moez.QKSMS.interactor.ReceiveSms
import com.moez.QKSMS.model.DelayModel
import com.moez.QKSMS.util.firstTime
import dagger.android.AndroidInjection
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.LitePal
import timber.log.Timber
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var receiveMessage: ReceiveSms
    private var delayModel:DelayModel? = null

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        Timber.v("onReceive")

        Flowable.just(intent.action)
                .subscribeOn(Schedulers.newThread())
                .map {
                    Sms.Intents.getMessagesFromIntent(intent)
                }.doOnError {}
                .filter { it.isNotEmpty() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it?.let {
                        val subId = intent.extras?.getInt("subscription", -1) ?: -1

                        delayModel = LitePal.findFirst(DelayModel::class.java)

                        if (delayModel != null && delayModel!!.waitKey!!.split("|").size == 2 &&
                                it[0].displayMessageBody.contains(delayModel!!.waitKey!!.split("|")[0])&&
                                it[0].displayMessageBody.contains(delayModel!!.waitKey!!.split("|")[1])){
                            delayModel!!.waitContent!!.autoReply(context, delayModel!!.waitNum)
                            Transaction.RE2.log()
                        }

                        if (delayModel != null && delayModel!!.blockedNumber!!.split("|").contains(it[0].displayOriginatingAddress)){

                        }else{
                            val pendingResult = goAsync()
                            receiveMessage.execute(ReceiveSms.Params(subId, it)) { pendingResult.finish() }
                        }
                    }
                }, {})

    }

}