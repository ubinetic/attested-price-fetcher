package com.ubinetic.attested.fetcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ubinetic.attested.fetcher.fetchers.BinanceUSPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.BitfinexPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.CoinbaseProPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.KrakenPriceFetcher
import com.ubinetic.attested.fetcher.utils.Networking

class FetcherBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Utils.scheduleAlarm(context)
        BinanceUSPriceFetcher().fetchPrice({
            it.print()
            Networking.publishPrice(it)
        }, { Log.d("FBR", "error ${it.message}") })
        BitfinexPriceFetcher().fetchPrice({
            it.print()
            Networking.publishPrice(it)
        }, { Log.d("FBR", "error ${it.message}") })
        CoinbaseProPriceFetcher().fetchPrice({
            it.print()
            Networking.publishPrice(it)
        }, { Log.d("FBR", "error ${it.message}") })
        KrakenPriceFetcher().fetchPrice({
            it.print()
            Networking.publishPrice(it)
        }, { Log.d("FBR", "error ${it.message}") })
    }
}