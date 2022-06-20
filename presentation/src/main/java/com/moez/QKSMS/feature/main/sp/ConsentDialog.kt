package com.moez.QKSMS.feature.main.sp

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.flyco.dialog.widget.base.BaseDialog
import com.moez.QKSMS.R
import com.moez.QKSMS.feature.main.event.Event
import org.greenrobot.eventbus.EventBus

class ConsentDialog(context: Context):BaseDialog<ConsentDialog>(context) {
    private var id: String?=null
    private var aid: String?=null
    override fun onCreateView(): View {
        widthScale(0.85f)
        return View.inflate(context, R.layout.consent_dialog, null)
    }

    override fun setUiBeforShow() {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        findViewById<TextView>(R.id.not_used).setOnClickListener {
            (context as AppCompatActivity).finish()
        }
        findViewById<TextView>(R.id.agree).setOnClickListener {
            EventBus.getDefault().post(Event("consent_agree", id, aid))
        }
    }

    fun setIds(id: String?, aid: String?){
        this.id = id
        this.aid = aid
    }

    override fun onBackPressed() {

    }
}