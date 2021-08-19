AWARE Plugin: SMS
==========================

[![Release](https://jitpack.io/v/denzilferreira/com.aware.plugin.sms.svg)](https://jitpack.io/#denzilferreira/com.aware.plugin.sms)

This plugin pulls SMS data from the user's phone in two ways (once set to active):
* If the "Send Full Data" flag is set, it will pull all SMS data from the user's phone between the Start Date and the End Date
* If the "Send Full Data" flag is not set, it will pull data from the "Last Sync Date"

Note: After doing a full data pull, the "Send Full Data" flag will automatically unset.

# Settings

Parameters adjustable on the dashboard and client:
- **status_plugin_sms**: (boolean) activate/deactivate plugin
- **plugin_sms_send_full_data**: (boolean) Fetch date for the full date range (T) or just ongoing data (F)
- **plugin_sms_start_date**: (String) YYYY-MM-DD ... If left empty, it will pull all sms mms data on the phone from before the end date. If Both start and end dates are left empty, it will pull all data from the phone.
- **plugin_sms_end_date**: (String) YYYY-MM-DD ... If left empty, it will pull all sms and mms data on the phone after the start date. If Both start and end dates are left empty, it will pull all data from the phone.
- **plugin_sms_last_sync_date**: (String) yyyy-MM-ddTHH:MM:ss.SSS ... The timestamp (w/ ms) of the last phone data pull
- **plugin_sms_sync_frequency**: (String) How many minutes between each SMS data pull. (Default 1)

# Datastore:
SMS
- content://com.aware.plugin.sms.provider.sms/plugin_sms
Content Storage for SMS data pulled from the users phone.

- Database fields:
1. _ID - integer primary key autoincrement
2. TIMESTAMP - real (default 0) - Unix Timestamp of message (in ms)
3. DEVICE_ID - text - Alphanumeric identifier for individual device
4. MSG_TYPE - text - Type of Message (Sent / Received)
5. MSG_THREAD_ID - text - Numeric ID to link messages together
6. MSG_ADDRESS - text - Alphanumeric Hash of the sender of the message
7. MSG_BODY - text - Contents of the SMS message.

# Credits

This plugin was developed by the U.S. National Institutes of Health for use in their studies.
- Plugin code based on standard AWARE Plugin Code by Denzil Ferreira (and possibly others)
- SMS export algorithm code based on "text-export" by WWBP (World Well-Being Project https://www.wwbp.org/)