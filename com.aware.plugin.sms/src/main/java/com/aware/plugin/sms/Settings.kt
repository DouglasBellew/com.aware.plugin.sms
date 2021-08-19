package com.aware.plugin.sms

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.EditTextPreference
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import com.aware.Aware
import com.aware.ui.AppCompatPreferenceActivity

class Settings : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val STATUS_PLUGIN_SMS = "status_plugin_sms"
        val PLUGIN_SMS_SEND_FULL_DATA = "plugin_sms_send_full_data"
        val PLUGIN_SMS_STARTDATE = "plugin_sms_start_date"
        val PLUGIN_SMS_ENDDATE = "plugin_sms_end_date"
        val PLUGIN_SMS_SYNC_DATE = "plugin_sms_last_sync_date"
        val PLUGIN_SMS_SYNC_FREQUENCY = "plugin_sms_sync_frequency"
        val PLUGIN_SMS_SYNC_FREQUENCY_DEFAULT = 1L
        val PLUGIN_SMS_SYNC_MIN_SELECT_DATE = "2000-01-01T00:00:01"
        val PLUGIN_SMS_SEND_RECEIVED_DATA = "plugin_sms_send_received_data"
        val PLUGIN_SMS_SEND_RECEIVED_DATA_DEFAULT = false
    }

    lateinit var status : CheckBoxPreference
    lateinit var send_received_data : CheckBoxPreference
    lateinit var send_full_data: CheckBoxPreference
    lateinit var start_date : EditTextPreference
    lateinit var end_date : EditTextPreference
    lateinit var sync_date : EditTextPreference
    lateinit var sync_frequency : EditTextPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_sms)
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        status = findPreference(STATUS_PLUGIN_SMS) as CheckBoxPreference
        if (Aware.getSetting(this, STATUS_PLUGIN_SMS).isEmpty())
            Aware.setSetting(this, STATUS_PLUGIN_SMS, true)
        status.isChecked = Aware.getSetting(this, STATUS_PLUGIN_SMS) == "true"

        send_received_data = findPreference(PLUGIN_SMS_SEND_RECEIVED_DATA) as CheckBoxPreference
        if (Aware.getSetting(this, PLUGIN_SMS_SEND_RECEIVED_DATA).isEmpty())
            Aware.setSetting(this, PLUGIN_SMS_SEND_RECEIVED_DATA, PLUGIN_SMS_SEND_RECEIVED_DATA_DEFAULT)
        send_received_data.isChecked = Aware.getSetting(this, STATUS_PLUGIN_SMS) == "true"

        send_full_data = findPreference(PLUGIN_SMS_SEND_FULL_DATA) as CheckBoxPreference
        if (Aware.getSetting(this, PLUGIN_SMS_SEND_FULL_DATA).isEmpty())
            Aware.setSetting(this, PLUGIN_SMS_SEND_FULL_DATA, true)
        send_full_data.isChecked = Aware.getSetting(this, PLUGIN_SMS_SEND_FULL_DATA) == "true"

        start_date = findPreference(PLUGIN_SMS_STARTDATE) as EditTextPreference
        if (Aware.getSetting(this, PLUGIN_SMS_STARTDATE).isEmpty())
            Aware.setSetting(this, PLUGIN_SMS_STARTDATE, "")
        start_date.text = Aware.getSetting(this, PLUGIN_SMS_STARTDATE)
        start_date.summary = Aware.getSetting(this, PLUGIN_SMS_STARTDATE)

        end_date = findPreference(PLUGIN_SMS_ENDDATE) as EditTextPreference
        if (Aware.getSetting(this, PLUGIN_SMS_ENDDATE).isEmpty())
            Aware.setSetting(this, PLUGIN_SMS_ENDDATE, "")
        end_date.text = Aware.getSetting(this, PLUGIN_SMS_ENDDATE)
        end_date.summary = Aware.getSetting(this, PLUGIN_SMS_ENDDATE)

        sync_date = findPreference(PLUGIN_SMS_SYNC_DATE) as EditTextPreference
        if (Aware.getSetting(this, PLUGIN_SMS_SYNC_DATE).isEmpty())
            Aware.setSetting(this, PLUGIN_SMS_SYNC_DATE, "")
        sync_date.text = Aware.getSetting(this, PLUGIN_SMS_SYNC_DATE)
        sync_date.summary = Aware.getSetting(this, PLUGIN_SMS_SYNC_DATE)

        sync_frequency = findPreference(PLUGIN_SMS_SYNC_FREQUENCY) as EditTextPreference
        if ((Aware.getSetting(this, PLUGIN_SMS_SYNC_FREQUENCY).isEmpty()) ||
            (Aware.getSetting(this, PLUGIN_SMS_SYNC_FREQUENCY) == "")) {
            Aware.setSetting(this, PLUGIN_SMS_SYNC_FREQUENCY, PLUGIN_SMS_SYNC_FREQUENCY_DEFAULT.toString())
        }
        sync_frequency.text = Aware.getSetting(this, PLUGIN_SMS_SYNC_FREQUENCY)
        sync_frequency.summary = "Every " + Aware.getSetting(this, PLUGIN_SMS_SYNC_FREQUENCY) + " minute(s)."
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val pref = findPreference(key)
        when (pref.key) {
            STATUS_PLUGIN_SMS -> {
                Aware.setSetting(this, key, sharedPreferences!!.getBoolean(key, false))
                status.isChecked = sharedPreferences.getBoolean(key, false)
            }
            STATUS_PLUGIN_SMS -> {
                Aware.setSetting(this, key, sharedPreferences!!.getBoolean(key, PLUGIN_SMS_SEND_RECEIVED_DATA_DEFAULT))
                status.isChecked = sharedPreferences.getBoolean(key, PLUGIN_SMS_SEND_RECEIVED_DATA_DEFAULT)
            }
            PLUGIN_SMS_SEND_FULL_DATA -> {
                Aware.setSetting(this, key, sharedPreferences!!.getBoolean(key, false))
                send_full_data.isChecked = sharedPreferences.getBoolean(key, false)
            }
            PLUGIN_SMS_STARTDATE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, ""))
                start_date.text = Aware.getSetting(this, key)
                start_date.summary = Aware.getSetting(this, key)
                pref.summary = start_date.text
            }
            PLUGIN_SMS_ENDDATE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, ""))
                end_date.text = Aware.getSetting(this, key)
                end_date.summary = Aware.getSetting(this, key)
                pref.summary = end_date.text
            }
            PLUGIN_SMS_SYNC_DATE -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, ""))
                sync_date.text = Aware.getSetting(this, key)
                sync_date.summary = Aware.getSetting(this, key)
                pref.summary = sync_date.text
            }
            PLUGIN_SMS_SYNC_FREQUENCY -> {
                Aware.setSetting(this, key, sharedPreferences!!.getString(key, "1"))
                sync_frequency.text = Aware.getSetting(this, key)
                sync_frequency.summary = "Every " + Aware.getSetting(this, key) + " minute(s)."
                pref.summary = sync_frequency.text
            }
        }
    }
}