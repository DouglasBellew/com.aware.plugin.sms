package com.aware.plugin.sms

import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

open class SMSMsg(threadId: String?, address: String, type: String, message_date: String?, retrieval_date: String?, msg: String?) {

    var TAG = "AWARE::sms"

//    var id: String? = null
    var threadId: String? = null
    var address: String? = null
        private set
    var type: String? = null
        private set
    var defaultType: String? = null
        private set
    var message_date: String? = null
    var retrieval_date: String? = null
    var msg: String? = null

    fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }


    fun setAddress(address: String) {
        var mod_address = address
        try {
            mod_address = md5(mod_address)
        } catch (e: NullPointerException) {
            Log.i(this.TAG, "SMS::setAddress: is null");
        }
        this.address = address
    }

    fun setType(type: String) {
        defaultType = type
        this.type = if (type == "1") "received message" else "sent message"
    }

    init {
       // this.id = id
        this.threadId = threadId
        setAddress(address)
        setType(type)
        this.message_date = message_date;
        this.retrieval_date = retrieval_date;
        this.msg = msg
    }
}
