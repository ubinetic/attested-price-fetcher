package com.ubinetic.attested.fetcher

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

fun String.hexStringToByteArray() =
    ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
fun ByteArray.toHex() = this.fold("", { str, it -> str + "%02x".format(it) })
fun ByteArray.trim() = this.fold(
    ByteArray(0),
    { result, it -> if (it == 0.toByte() && result.isEmpty()) result else result + it })

class Utils {
    companion object {

        fun scheduleAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, FetcherBroadcastReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }
            val systemNextPeriod =
                (System.currentTimeMillis() / MainActivity.MILLIS_PER_INTERVAL) + 1
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                systemNextPeriod * MainActivity.MILLIS_PER_INTERVAL,
                alarmIntent
            )
        }
    }
}