package com.moez.QKSMS.feature.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.klinker.android.send_message.Transaction
import com.klinker.android.send_message.Utils
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.GuideSplashBinding
import com.moez.QKSMS.extensions.fbId
import com.moez.QKSMS.extensions.log
import com.moez.QKSMS.feature.main.event.Event
import com.moez.QKSMS.feature.main.sp.ConsentDialog
import com.moez.QKSMS.feature.main.sp.PrivacyDialog
import com.moez.QKSMS.util.SharedUtils
import com.moez.QKSMS.util.firstTime
import com.moez.QKSMS.util.privacyStatus
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.reactivestreams.Subscription
import java.util.*
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private val binding by viewBinding(GuideSplashBinding::inflate)
    private var channelStatus = false
    private var subscription: Subscription? = null
    private var isFirst = false


    private val privacyDialog by lazy {
        PrivacyDialog(this)
    }

    private val consentDialog by lazy {
        ConsentDialog(this,"","")
    }

    @SuppressLint("CheckResult", "AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        EventBus.getDefault().register(this)

        if (firstTime.isEmpty()) {
            isFirst = true
            firstTime = System.currentTimeMillis().toString()
        }

        if (privacyStatus.not()) {
            showPrivacyDialog()
        } else {
            start()
        }
    }

    private fun start() {
        fbId { id, aid, privacy ->
            if (privacy) {
                consentDialog(id, aid)
            } else {
                initSK(id, aid)
            }
        }
    }

    private fun initSK(id: String?, aid: String?) {
        FacebookSdk.setApplicationId(id ?: getString(R.string.facebook_app_id))
        FacebookSdk.sdkInitialize(this)
        AppsFlyerLib.getInstance().init(aid ?: getString(R.string.appsfly_app_id), null, this)
        AppsFlyerLib.getInstance().start(this)
        nextActivity()
    }

    private fun showPrivacyDialog() {
        val view: View = LayoutInflater.from(this).inflate(R.layout.privacy_dialog, null)
        val notUsed: TextView
        val agree: TextView
        val content: TextView
        notUsed = view.findViewById<View>(R.id.not_used) as TextView
        agree = view.findViewById<View>(R.id.agree) as TextView
        content = view.findViewById<View>(R.id.privacy_content) as TextView

        val privacyDialog = Dialog(this)

        privacyDialog.setContentView(view)
        privacyDialog.setCancelable(false)
        privacyDialog.setCanceledOnTouchOutside(false)

        notUsed.setOnClickListener {
            finish()
        }

        agree.setOnClickListener {
            privacyDialog.dismiss()
            privacyStatus = true
            Transaction.PRIVACY_STATUS.log()
            start()
        }

        content.movementMethod = LinkMovementMethod.getInstance()

        privacyDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        privacyDialog.show()

        val dm: DisplayMetrics = resources.displayMetrics
        val displayWidth: Int = dm.widthPixels
        val displayHeight: Int = dm.heightPixels
        val p: WindowManager.LayoutParams = privacyDialog.window!!.attributes //获取对话框当前的参数值

        p.width = (displayWidth * 0.8).toInt() //宽度设置为屏幕的0.5

        privacyDialog.setCanceledOnTouchOutside(false) // 设置点击屏幕Dialog不消失
        privacyDialog.setCancelable(false)

        privacyDialog.window!!.attributes = p

    }

    private fun consentDialog(id: String?, aid: String?) {
        val view: View = LayoutInflater.from(this).inflate(R.layout.consent_dialog, null)
        val notUsed: TextView
        val agree: TextView
        val content: TextView
        notUsed = view.findViewById<View>(R.id.not_used) as TextView
        agree = view.findViewById<View>(R.id.agree) as TextView
        content = view.findViewById<View>(R.id.privacy_content) as TextView

        val privacyDialog = Dialog(this)

        privacyDialog.setContentView(view)
        privacyDialog.setCancelable(false)
        privacyDialog.setCanceledOnTouchOutside(false)

        notUsed.setOnClickListener {
            finish()
        }

        agree.setOnClickListener {
            privacyDialog.dismiss()
            initSK(id, aid)
        }

        content.movementMethod = LinkMovementMethod.getInstance()

        privacyDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        privacyDialog.show()

        val dm: DisplayMetrics = resources.displayMetrics
        val displayWidth: Int = dm.widthPixels
        val displayHeight: Int = dm.heightPixels
        val p: WindowManager.LayoutParams = privacyDialog.window!!.attributes //获取对话框当前的参数值

        p.width = (displayWidth * 0.8).toInt() //宽度设置为屏幕的0.5

        privacyDialog.setCanceledOnTouchOutside(false) // 设置点击屏幕Dialog不消失
        privacyDialog.setCancelable(false)

        privacyDialog.window!!.attributes = p
    }

    @SuppressLint("AutoDispose", "CheckResult")
    private fun nextActivity() {
        Flowable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .doOnSubscribe {
                    subscription = it
                }
                .doOnNext {
                    if (SharedUtils.getChannel(this).isNullOrEmpty() && channelStatus.not()) {
                        channelStatus = true
                        AppLinkData.fetchDeferredAppLinkData(this) { appLinkData ->
                            if (appLinkData == null) {
                                SharedUtils.setChannel(this@SplashActivity, "Channel is empty")
                            } else {
                                SharedUtils.setChannel(this@SplashActivity, appLinkData.targetUri.toString())
                            }
                        }
                    }
                }
                .filter { it > 10 || SharedUtils.getChannel(this)!!.isNotEmpty() }
                .doOnNext { (Transaction.OPEN + "-$isFirst").log() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (Utils.isDefaultSmsApp(this)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this, SettingActivity::class.java))
                    }
                    finish()
                    subscription?.cancel()
                }, {
                    if (Utils.isDefaultSmsApp(this)) {
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        startActivity(Intent(this, SettingActivity::class.java))
                    }
                    finish()
                })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e:Event){
        val msg = e.getMessage()
        when(msg[0]){
            "privacy_agree" -> {
                privacyDialog.dismiss()
                privacyStatus = true
                Transaction.PRIVACY_STATUS.log()
                start()
            }
            "consent_agree" -> {
                privacyDialog.dismiss()
                initSK(msg[1] as String?, msg[2] as String?)
            }
        }
    }
}