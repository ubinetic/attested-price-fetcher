package com.ubinetic.attested.fetcher.fetchers

import com.ubinetic.attested.fetcher.Utils
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Networking
import java.lang.Exception
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL

class BinancePriceFetcher : PriceFetcher {
    val PRECISION = BigDecimal.valueOf(Math.pow(10.0,6.0))
    val SYMBOL = "XTZUSDT"

    override fun fetchPrice(success: (Price) -> Unit, error: (Exception) -> Unit) {
        val url =
            URL("https://api.binance.com/api/v3/klines?symbol=${SYMBOL}&interval=15m&startTime=${Utils.getPreviousEpochStart()}&endTime=${Utils.getPreviousEpochEnd()}")
        Networking.fetchJsonArray(url,{ payload, certificatePin ->
            val responseJsonArray = payload.getJSONArray(0)
            val closingPrice = BigDecimal(responseJsonArray.getString(4))
            val volume = BigDecimal(responseJsonArray.getString(5))
            val timestamp = responseJsonArray.getLong(6)/1000
            success(Price(this.getId(), SYMBOL, closingPrice.multiply(PRECISION).toBigInteger(), volume.multiply(PRECISION).toBigInteger(), BigInteger.valueOf(timestamp), certificatePin))
        },error)
    }

    override fun getId(): String {
        return "BNN"
    }
}