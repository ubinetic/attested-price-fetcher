package com.ubinetic.attested.fetcher.utils

import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.toHex
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class Networking {
    companion object {
        val BASE_URL = "https://price-broadcaster-backend.dev.gke.papers.tech"

        fun publishDevice(attestation: String, certificateChain: String) {
            val jsonPayload = JSONObject()
            jsonPayload.put("attestation", attestation)
            jsonPayload.put("certificate_chain", certificateChain)
            postJsonObject(jsonPayload, "/api/devices/")
        }

        fun publishPrice(price: Price) {
            val jsonPayload = JSONObject()
            jsonPayload.put("packed_payload", price.pack().toHex())
            jsonPayload.put("signature", Crypto.priceSigner(price))
            jsonPayload.put("public_key", Crypto.getEncodedPublicKey())
            postJsonObject(jsonPayload, "/api/prices/")
        }

        fun postJsonObject(jsonPayload: JSONObject, endPoint: String) {
            Thread {
                val url =
                    URL("$BASE_URL${endPoint}")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                try {
                    val jsonString = jsonPayload.toString()
                    urlConnection.setRequestMethod("POST")
                    urlConnection.setRequestProperty("Content-Type", "application/json; utf-8")
                    urlConnection.setDoOutput(true)
                    urlConnection.outputStream.write(jsonString.toByteArray(), 0, jsonString.toByteArray().size)
                    urlConnection.responseCode
                } finally {
                    urlConnection.disconnect()
                }
            }.start()
        }
    }
}