package com.ubinetic.attested.fetcher.fetchers

import android.util.Log
import com.ubinetic.attested.fetcher.MainActivity
import com.ubinetic.attested.fetcher.models.Price
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.util.*
import javax.net.ssl.HttpsURLConnection



class KrakenPriceFetcher : PriceFetcher {
    val PRECISION = BigDecimal.valueOf(Math.pow(10.0,6.0))
    val SYMBOL = "XTZUSD"

    override fun fetchPrice(success: (Price) -> Unit, error: (Exception) -> Unit) {
        Thread {
            val systemPreviousPeriod = (System.currentTimeMillis() / MainActivity.MILLIS_PER_INTERVAL) - 1
            val systemPreviousPeriodStart =systemPreviousPeriod * MainActivity.MILLIS_PER_INTERVAL
            val url =
                URL("https://api.kraken.com/0/public/OHLC?pair=${SYMBOL}&interval=15&since=${systemPreviousPeriodStart}")

            val urlConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            try {
                val responseJsonArray = JSONObject(urlConnection.inputStream.bufferedReader().readLine()).getJSONObject("result").getJSONArray(SYMBOL).getJSONArray(0)
                val closingPrice = BigDecimal(responseJsonArray.getString(4))
                val volume = BigDecimal(responseJsonArray.getString(6))
                val timestamp = responseJsonArray.getLong(0)
                success(Price(this.getId(), SYMBOL, closingPrice.multiply(PRECISION).toBigInteger(), volume.multiply(PRECISION).toBigInteger(), BigInteger.valueOf(timestamp)))

                Log.d("ATT", "response ${this.getId()}: '${responseJsonArray.toString()}'")
                val md = MessageDigest.getInstance("SHA-256")
                val digest =
                    md.digest(urlConnection.serverCertificates.get(0).publicKey.encoded)
                Log.d("ATT", digest.fold("", { str, it -> str + "%02x".format(it) }))
                Log.d("ATT", Base64.getEncoder().encodeToString(digest))
            } finally {
                urlConnection.disconnect()
            }
        }.start()
    }

    override fun getId(): String {
        return "KRK"
    }
}