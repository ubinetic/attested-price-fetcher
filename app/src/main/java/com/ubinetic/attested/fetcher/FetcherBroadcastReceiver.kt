package com.ubinetic.attested.fetcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ubinetic.attested.fetcher.fetchers.BinanceUSPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.BitfinexPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.CoinbaseProPriceFetcher
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.utils.Crypto
import com.ubinetic.attested.fetcher.utils.Encoding
import com.ubinetic.attested.fetcher.utils.Networking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CountDownLatch

class FetcherBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Utils.scheduleFetchPriceInterval(context)

        try {
            val concurrentMap = ConcurrentHashMap<ByteArray, ByteArray>()
            val priceFetchers = listOf(
                BinanceUSPriceFetcher(), BitfinexPriceFetcher(), CoinbaseProPriceFetcher()
            )
            val countDownLatch = CountDownLatch(priceFetchers.size)
            priceFetchers.map {
                it.fetchPrice({
                    concurrentMap.put(Encoding.packBytes(it.pack()), Encoding.packSignature(Crypto.priceSigner(it)))
                    countDownLatch.countDown()
                }, {
                    countDownLatch.countDown()
                })
            }
            countDownLatch.await()
            Utils.sendSafetyNetRequest(context, Encoding.packMessage(Encoding.packMap(concurrentMap)))
        } catch (exception: Exception) {

        }
    }
}