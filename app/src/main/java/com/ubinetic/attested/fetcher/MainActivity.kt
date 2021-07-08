package com.ubinetic.attested.fetcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.ubinetic.attested.fetcher.fetchers.BinanceUSPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.BitfinexPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.CoinbaseProPriceFetcher
import com.ubinetic.attested.fetcher.utils.Crypto
import com.ubinetic.attested.fetcher.utils.Encoding
import com.ubinetic.attested.fetcher.utils.Networking
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.scheduleFetchPriceInterval(this)
        Crypto.createIfNotExistsKeyStoreEntry()
        Networking.publishDevice(Crypto.getCertificateChainAsPem())

        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }

        log.setText("public key: ${Crypto.getEncodedPublicKey()}")
    }
}