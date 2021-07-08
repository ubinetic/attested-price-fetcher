package com.ubinetic.attested.fetcher.fetchers

import com.ubinetic.attested.fetcher.Utils
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Networking
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter

class CoinbaseProPriceFetcher : PriceFetcher {
    val PRECISION = BigDecimal.valueOf(Math.pow(10.0,6.0))
    val SYMBOL = "XTZUSD"

    override fun fetchPrice(success: (Price) -> Unit, error: (Exception) -> Unit) {
        val startISO =
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(Utils.getPreviousEpochStart()))
        val endISO =
            DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(Utils.getPreviousEpochEnd()))
        val url = URL("https://api.pro.coinbase.com/products/XTZ-USD/candles?granularity=900&start=${startISO}&end=${endISO}")

        Networking.fetchJsonArray(url, { payload, certificatePin ->
            val responseJsonArray = payload.getJSONArray(0)
            val closingPrice = BigDecimal(responseJsonArray.getString(3))
            val volume = BigDecimal(responseJsonArray.getString(5))
            val timestamp =
                responseJsonArray.getLong(0) + 900 // we always want end time but CBP gives us start time -> add 900seconds
            success(
                Price(
                    this.getId(),
                    SYMBOL,
                    closingPrice.multiply(PRECISION).toBigInteger(),
                    volume.multiply(PRECISION).toBigInteger(),
                    BigInteger.valueOf(timestamp),
                    certificatePin
                )
            )
        }, error)
    }

    override fun getId(): String {
        return "CBP"
    }
}