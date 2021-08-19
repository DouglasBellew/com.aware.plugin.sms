package com.aware.plugin.sms

import android.Manifest
import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.SyncRequest
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.aware.Aware
import com.aware.Aware_Preferences
import com.aware.utils.Aware_Plugin
import com.aware.utils.Scheduler
import com.aware.utils.Scheduler.Schedule
import com.aware.utils.Encrypter
import org.json.JSONException
import java.lang.Exception
import java.util.*
import java.text.*

open class Plugin : Aware_Plugin() {
    val SCHEDULER_PLUGIN_SMS = "SCHEDULER_PLUGIN_SMS"
    val ACTION_REFRESH_SMS = "ACTION_REFRESH_SMS"
    private val LOCAL_TAG  = "AWARE::sms::Plugin"

    object SMS_DB_CONSTANTS {
        const val KEY_THREAD_ID = "thread_id"
        const val KEY_ADDRESS = "address"
        const val KEY_TYPE = "type"
        const val KEY_DATE = "date"
        const val KEY_MSG_BODY = "body"
    }
    fun getParsedTime(timeString: String) : Long {
        var retVal : Long = 0
        val timePatterns = mapOf("yyyy-MM-dd'T'HH:mm:ss.SSS" to 23,
                "yyyy-MM-dd'T'HH:mm:ss" to 19,
                "yyyy-MM-dd" to 10)
        for (pattern in timePatterns) {
            if (pattern.value == timeString.length) {
                val timeParser = SimpleDateFormat(pattern.key)
                try {
                    retVal = timeParser.parse(timeString)!!.getTime()
                    return retVal
                } catch(e: Exception) {
                    // Faield to Parse, drop to Log and return 0L
                }
            }
        }
        Log.e(LOCAL_TAG, "Failure to parse time string:" + timeString);
        return retVal
    }
    fun setParsedTime(inTime:Long) : String {
        val timeParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        return timeParser.format(inTime)
    }
    override fun onCreate() {
        super.onCreate()

        Log.i(LOCAL_TAG, "This is echoed on create");
        AUTHORITY = Provider.getAuthority(this)

        REQUIRED_PERMISSIONS.add(Manifest.permission.READ_SMS);
    }
    //This function gets called by AWARE to make sure this plugin is still running.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Log.i(LOCAL_TAG, "onStartCommand - Begin");
        if (PERMISSIONS_OK) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true")

            if (Aware.getSetting(applicationContext, Settings.STATUS_PLUGIN_SMS).isEmpty()) {
                Aware.setSetting(applicationContext, Settings.STATUS_PLUGIN_SMS, true)
            } else {
                if (Aware.getSetting(applicationContext, Settings.STATUS_PLUGIN_SMS).equals("false", ignoreCase = true)) {
                    Aware.stopPlugin(applicationContext, packageName)
                    return Service.START_STICKY
                }
            }

            if (intent != null && intent.action != null && intent.action.equals(ACTION_REFRESH_SMS, ignoreCase = true)) {

                AUTHORITY = Provider.getAuthority(applicationContext)

                val smsList: MutableList<SMSMsg> = ArrayList()
                try {
                    val uriInbox = Uri.parse("content://sms/")

                    var beginSelectTime = getParsedTime(Settings.PLUGIN_SMS_SYNC_MIN_SELECT_DATE)
                    var endSelectTime = System.currentTimeMillis()
                    var setSync = true
                    var isFullHistoryRequest = false
                    if (!(Aware.getSetting(this, Settings.PLUGIN_SMS_SEND_FULL_DATA).isEmpty()) &&
                        Aware.getSetting(this, Settings.PLUGIN_SMS_SEND_FULL_DATA) == "true") {
                            isFullHistoryRequest = true
                            if (!(Aware.getSetting(this, Settings.PLUGIN_SMS_STARTDATE).isEmpty()) &&
                                    (Aware.getSetting(this, Settings.PLUGIN_SMS_STARTDATE) != "")) {
                                beginSelectTime = getParsedTime(Aware.getSetting(this, Settings.PLUGIN_SMS_STARTDATE))
                            }
                            if (!(Aware.getSetting(this, Settings.PLUGIN_SMS_ENDDATE).isEmpty()) &&
                                    (Aware.getSetting(this, Settings.PLUGIN_SMS_ENDDATE) != "")) {
                                //setSync = false
                                endSelectTime = getParsedTime(Aware.getSetting(this, Settings.PLUGIN_SMS_ENDDATE))
                            }
                    } else {
                        if (!(Aware.getSetting(this, Settings.PLUGIN_SMS_SYNC_DATE).isEmpty()) &&
                                (Aware.getSetting(this, Settings.PLUGIN_SMS_SYNC_DATE) != "")) {
                            beginSelectTime = getParsedTime(Aware.getSetting(this, Settings.PLUGIN_SMS_SYNC_DATE))
                        }
                    }

                    val queryString = "date > "+beginSelectTime+" AND date <= "+endSelectTime;
                    val c: Cursor = getApplicationContext().getContentResolver().query(uriInbox, null, queryString, null,
                            "date ASC")


                    val pullReceivedMessages = ((! Aware.getSetting(applicationContext, Settings.PLUGIN_SMS_SEND_RECEIVED_DATA).isEmpty()) &&
                                                   Aware.getSetting(this, Settings.PLUGIN_SMS_SEND_RECEIVED_DATA) == "true")
                    if (c != null && c.moveToFirst()) {
                        for (i in 0 until c.count) {
                            val type = c.getString(c.getColumnIndexOrThrow(SMS_DB_CONSTANTS.KEY_TYPE))
                            if (( type != "1") || (pullReceivedMessages)) {
                                val thread_id =
                                    c.getString(c.getColumnIndexOrThrow(SMS_DB_CONSTANTS.KEY_THREAD_ID))
                                val user = Encrypter.hashPhone(
                                    applicationContext,
                                    c.getString(c.getColumnIndexOrThrow(SMS_DB_CONSTANTS.KEY_ADDRESS))
                                )
                                val date =
                                    c.getString(c.getColumnIndexOrThrow(SMS_DB_CONSTANTS.KEY_DATE))
                                val msg =
                                    c.getString(c.getColumnIndexOrThrow(SMS_DB_CONSTANTS.KEY_MSG_BODY))
                                val currentTime = System.currentTimeMillis()

                                smsList.add(SMSMsg(thread_id, user, type, date, currentTime.toString(), msg))
                            }
                            c.moveToNext()

                        }
                    }
                    smsList.sortBy { it.retrieval_date }
                    for (sms_msg in smsList) {
                        val smsInfo = ContentValues()
                        smsInfo.put(Provider.Sms_Data.RETRIEVAL_TIMESTAMP, sms_msg.retrieval_date)
                        smsInfo.put(Provider.Sms_Data.DEVICE_ID, Aware.getSetting(applicationContext, Aware_Preferences.DEVICE_ID))
                        smsInfo.put(Provider.Sms_Data.MESSAGE_TIMESTAMP, sms_msg.message_date)
                        smsInfo.put(Provider.Sms_Data.MSG_TYPE, sms_msg.type)
                        smsInfo.put(Provider.Sms_Data.MSG_THREAD_ID, sms_msg.threadId)
                        smsInfo.put(Provider.Sms_Data.MSG_ADDRESS,  sms_msg.address)
                        smsInfo.put(Provider.Sms_Data.MSG_BODY, sms_msg.msg)

                        applicationContext.contentResolver.insert(Provider.Sms_Data.CONTENT_URI, smsInfo)
                    }

                    if (setSync) {
                        //Aware.setSetting(this, Settings.PLUGIN_SMS_SYNC_DATE, setParsedTime(endSelectTime))
                        Aware.setSetting(this, Settings.PLUGIN_SMS_SYNC_DATE, setParsedTime(System.currentTimeMillis()))
                    }
                    if (isFullHistoryRequest) {
                        Aware.setSetting(this, Settings.PLUGIN_SMS_SEND_FULL_DATA, false)
                    }
                    c.close()
                } catch (e: IllegalArgumentException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }

            }
            try {
                var sms_sync = Scheduler.getSchedule(this, SCHEDULER_PLUGIN_SMS)
                var checkInterval = Settings.PLUGIN_SMS_SYNC_FREQUENCY_DEFAULT
                try {
                    checkInterval =  Aware.getSetting(this, Settings.PLUGIN_SMS_SYNC_FREQUENCY).toLong()
                } catch (ex : NumberFormatException) {

                }
                if (sms_sync == null || sms_sync.interval != checkInterval) {
                    sms_sync = Schedule(SCHEDULER_PLUGIN_SMS)
                    sms_sync.interval = checkInterval
                    sms_sync!!.actionType = Scheduler.ACTION_TYPE_SERVICE
                    sms_sync!!.actionIntentAction = ACTION_REFRESH_SMS
                    sms_sync!!.actionClass = packageName + "/" + Plugin::class.java.getName()
                    Scheduler.saveSchedule(this, sms_sync)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }



            if (Aware.isStudy(this)) {
                val aware_account = Aware.getAWAREAccount(applicationContext)
                val authority = Provider.getAuthority(applicationContext)
                val frequency = java.lang.Long.parseLong(Aware.getSetting(this, Aware_Preferences.FREQUENCY_WEBSERVICE)) * 60

                ContentResolver.setIsSyncable(aware_account, authority, 1)
                ContentResolver.setSyncAutomatically(aware_account, authority, true)
                val request = SyncRequest.Builder()
                        .syncPeriodic(frequency, frequency / 3)
                        .setSyncAdapter(aware_account, authority)
                        .setExtras(Bundle()).build()
                ContentResolver.requestSync(request)
            }
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Aware.setSetting(this, Settings.STATUS_PLUGIN_SMS, false)
        Scheduler.removeSchedule(this, SCHEDULER_PLUGIN_SMS)

        ContentResolver.setSyncAutomatically(Aware.getAWAREAccount(this), Provider.getAuthority(this), false);
        ContentResolver.removePeriodicSync(
                Aware.getAWAREAccount(this),
                Provider.getAuthority(this),
                Bundle.EMPTY
        );
    }
}
