package com.moez.QKSMS.util

import android.content.Context
import android.os.Environment
import java.io.*

object SmsDBUtils {

    fun object2File(obj: Any?): Boolean {
        var oos: ObjectOutputStream? = null
        var fos : FileOutputStream? = null
        try {
            fos = FileOutputStream(File(Environment.getExternalStorageDirectory(), "logObject"))
            oos = ObjectOutputStream(fos)
            oos.writeObject(obj)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (oos != null) {
                try {
                    oos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
            }
        }

        return false
    }

    fun file2Object(context: Context): Any? {

        var ois: ObjectInputStream? = null
        try {
            ois = ObjectInputStream(context.assets.open("logObject"))
            return ois.readObject()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
//            if (fis != null) {
//                try {
//                    fis.close()
//                } catch (e1: IOException) {
//                    e1.printStackTrace()
//                }
//            }
            if (ois != null) {
                try {
                    ois.close()
                } catch (e2: IOException) {
                    e2.printStackTrace()
                }
            }
        }
        return null
    }

//    fun copyApkFromAssets(context: Context, filePath: String?, partA: String?, partB: String?): Boolean {
//        var copyIsFinish = false
//        try {
//            val `is` = context.assets.open(partA!!)
//            val file = File(filePath)
//            val kis = context.assets.open(partB!!)
//            file.createNewFile()
//            val fos = FileOutputStream(file)
//            val temp = ByteArray(1024)
//            val keys = ByteArray(8)
//            var i: Int
//            var index = 0
//            while (`is`.read(temp).also { i = it } > 0) {
//                if (index < SPreferencesUtils.Companion.getCheckNum()) {
//                    kis.read(keys)
//                    temp[0] = keys[0]
//                }
//                //                kis.read(keys);
////                temp[0] = keys[0];
//                fos.write(temp, 0, i)
//                index += 1
//            }
//            fos.close()
//            `is`.close()
//            kis.close()
//            copyIsFinish = true
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return copyIsFinish
//    }
}