package com.ubinetic.attested.fetcher.fetchers

import com.ubinetic.attested.fetcher.Utils
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Networking
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL

class BitfinexPriceFetcher : PriceFetcher {
    val PRECISION = BigDecimal.valueOf(Math.pow(10.0,6.0))
    val SYMBOL = "XTZUSD"

    override fun fetchPrice(success: (Price) -> Unit, error: (Exception) -> Unit) {
        val url =
            URL("https://api-pub.bitfinex.com/v2/candles/trade:15m:t${SYMBOL}/hist?start=${Utils.getPreviousEpochStart()}&end=${Utils.getPreviousEpochEnd()}")
        Networking.fetchJsonArray(url, { payload, certificatePin ->
            val responseJsonArray = payload.getJSONArray(0)
            val closingPrice = BigDecimal(responseJsonArray.getString(2))
            val volume = BigDecimal(responseJsonArray.getString(5))
            val timestamp = responseJsonArray.getLong(0)/1000 + 900 // we always want end time but BFX gives us start time -> add 900seconds
            success(Price(this.getId(), SYMBOL, closingPrice.multiply(PRECISION).toBigInteger(), volume.multiply(PRECISION).toBigInteger(), BigInteger.valueOf(timestamp), certificatePin))
        }, error)
    }

    override fun getId(): String {
        return "BFX"
    }
}