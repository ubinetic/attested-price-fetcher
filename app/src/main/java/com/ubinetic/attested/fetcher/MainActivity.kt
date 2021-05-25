package com.ubinetic.attested.fetcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.google.android.gms.safetynet.SafetyNet
import com.ubinetic.attested.fetcher.fetchers.BinanceUSPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.BitfinexPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.CoinbaseProPriceFetcher
import com.ubinetic.attested.fetcher.fetchers.KrakenPriceFetcher
import com.ubinetic.attested.fetcher.utils.Crypto
import com.ubinetic.attested.fetcher.utils.Networking
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    companion object {
        val MILLIS_PER_INTERVAL = 1000 * 60 * 15
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.scheduleAlarm(this)
        Crypto.createIfNotExistsKeyStoreEntry()
        sendSafetyNetRequest()

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

        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    fun sendSafetyNetRequest() {
        val client = SafetyNet.getClient(this);
        val task = client.attest(Crypto.getPublicKey(), BuildConfig.SAFETYNET_KEY)
        Log.d("ATT", "doing")
        task.addOnSuccessListener(this) {
            Log.d("ATT", "jwt: '${it.jwsResult}'")
            Networking.publishDevice(it.jwsResult, Crypto.getCertificateChainAsPem())
            log.setText(
                """
                public key:     ${Crypto.getEncodedPublicKey()}
                """
            )
        }.addOnFailureListener(this) {
            Log.e("ATT", "exception: '${it.stackTrace}'")
        }
    }
}