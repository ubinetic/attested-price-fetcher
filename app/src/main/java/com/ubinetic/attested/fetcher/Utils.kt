package com.ubinetic.attested.fetcher

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.safetynet.SafetyNet
import com.rfksystems.blake2b.Blake2b
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Crypto
import com.ubinetic.attested.fetcher.utils.Encoding
import com.ubinetic.attested.fetcher.utils.Networking
import kotlinx.android.synthetic.main.activity_main.*

fun String.hexStringToByteArray() =
    ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
fun ByteArray.toHex() = this.fold("", { str, it -> str + "%02x".format(it) })
fun ByteArray.trim() = this.fold(
    ByteArray(0),
    { result, it -> if (it == 0.toByte() && result.isEmpty()) result else result + it })

class Utils {
    companion object {
        fun scheduleFetchPriceInterval(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, FetcherBroadcastReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }
            val systemNextPeriod =
                (System.currentTimeMillis() / AlarmManager.INTERVAL_FIFTEEN_MINUTES) + 1
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                systemNextPeriod * AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                alarmIntent
            )
        }

        fun getPreviousEpoch(): Long {
            return (System.currentTimeMillis() / AlarmManager.INTERVAL_FIFTEEN_MINUTES)-1
        }

        fun getPreviousEpochStart(): Long{
            return this.getPreviousEpoch()*AlarmManager.INTERVAL_FIFTEEN_MINUTES
        }

        fun getPreviousEpochEnd(): Long{
            return ((this.getPreviousEpoch()+1) * AlarmManager.INTERVAL_FIFTEEN_MINUTES) - 1
        }

        fun calculateNonce(prices: List<Price>): ByteArray{
            val message = Encoding.packList(prices.map { it.pack() })

            val digest = Blake2b(256)
            digest.update(message, 0, message.size)

            val out = ByteArray(32)
            digest.digest(out, 0)

            return out
        }

        fun sendSafetyNetRequest(context: Context, nonce: ByteArray) {
            val client = SafetyNet.getClient(context)
            val task = client.attest(nonce, BuildConfig.SAFETYNET_KEY)
            task.addOnSuccessListener {
                Networking.publishObservation(it.jwsResult)
            }
        }
    }
}