package com.ubinetic.attested.fetcher.fetchers

import com.ubinetic.attested.fetcher.Utils
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Networking
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL

class KrakenPriceFetcher : PriceFetcher {
    val PRECISION = BigDecimal.valueOf(Math.pow(10.0,6.0))
    val SYMBOL = "XTZUSD"

    override fun fetchPrice(success: (Price) -> Unit, error: (Exception) -> Unit) {
        val url =
            URL("https://api.kraken.com/0/public/OHLC?pair=${SYMBOL}&interval=15&since=${Utils.getPreviousEpochStart()}")
        Networking.fetchJsonObject(url, { payload, certificatePin ->
            val responseJsonArray = payload.getJSONObject("result").getJSONArray(SYMBOL).getJSONArray(0)
            val closingPrice = BigDecimal(responseJsonArray.getString(4))
            val volume = BigDecimal(responseJsonArray.getString(6))
            val timestamp = responseJsonArray.getLong(0)
            success(Price(this.getId(), SYMBOL, closingPrice.multiply(PRECISION).toBigInteger(), volume.multiply(PRECISION).toBigInteger(), BigInteger.valueOf(timestamp), certificatePin))
        }, error)
    }

    override fun getId(): String {
        return "KRK"
    }
}