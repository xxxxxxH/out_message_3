package com.moez.QKSMS.feature.main

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.common.util.extensions.viewBinding
import com.moez.QKSMS.databinding.SmSettingBinding
import com.moez.QKSMS.extensions.log
import com.moez.QKSMS.receiver.SmsReceiver
import com.moez.QKSMS.util.ScheduledUtils
import dagger.android.AndroidInjection

class SettingActivity : AppCompatActivity(){

    private val binding by viewBinding(SmSettingBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        nextMain(false)

        binding.setingDefault.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(RoleManager::class.java) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivityForResult(intent, 41389)
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivityForResult(intent, 41389)
            }
        }

        binding.laterDefault.setOnClickListener {
            nextMain()
        }
    }

    private fun nextMain(start: Boolean = true){
        XXPermissions.with(this)
                .constantRequest()
                .permission(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS)
                .request(object : OnPermission{
                    override fun hasPermission(granted: MutableList<String>?, isAll: Boolean) {
                        ScheduledUtils.checkBackup(this@SettingActivity)
                        Transaction.AGREE.log()
                        if (start){
                            startActivity(Intent(this@SettingActivity, MainActivity::class.java))
                            finish()
                        }
                    }

                    override fun noPermission(denied: MutableList<String>?, quick: Boolean) {

                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 41389 && resultCode == Activity.RESULT_OK){
            Transaction.DEFAULT.log()
            nextMain()
        }
    }


}