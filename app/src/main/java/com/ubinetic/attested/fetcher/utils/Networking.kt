package com.ubinetic.attested.fetcher.utils

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.cert.Certificate
import javax.net.ssl.HttpsURLConnection

class Networking {
    companion object {
        val BASE_URL = "https://price-broadcaster-backend.dev.gke.papers.tech"

        fun publishDevice(certificateChain: String) {
            val jsonPayload = JSONObject()
            jsonPayload.put("certificate_chain", certificateChain)
            postJsonObject(jsonPayload, "/api/devices/")
        }

        fun publishObservation(attestation: String) {
            val jsonPayload = JSONObject()
            jsonPayload.put("attestation", attestation)
            jsonPayload.put("device", Crypto.getEncodedPublicKey())
            postJsonObject(jsonPayload, "/api/observations/")
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
                    urlConnection.outputStream.write(
                        jsonString.toByteArray(),
                        0,
                        jsonString.toByteArray().size
                    )
                    urlConnection.responseCode
                } finally {
                    urlConnection.disconnect()
                }
            }.start()
        }

        fun getCertificatePin(certificate: Certificate): ByteArray {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(certificate.publicKey.encoded)
            return digest
        }

        fun fetchString(
            url: URL,
            success: (String, ByteArray) -> Unit,
            error: (Exception) -> Unit
        ) {
            val urlConnection: HttpsURLConnection = url.openConnection() as HttpsURLConnection
            Thread {
                try {
                    val payload = urlConnection.inputStream.bufferedReader().readLine()
                    val certificateSha256 =
                        getCertificatePin(urlConnection.serverCertificates.first())
                    success(payload, certificateSha256)
                } catch (exception: Exception) {
                    error(exception)
                } finally {
                    urlConnection.disconnect()
                }
            }.start()
        }

        fun fetchJsonArray(
            url: URL,
            success: (JSONArray, ByteArray) -> Unit,
            error: (Exception) -> Unit
        ) {
            fetchString(
                url,
                { payload, certificatePin -> success(JSONArray(payload), certificatePin) },
                error
            )
        }

        fun fetchJsonObject(
            url: URL,
            success: (JSONObject, ByteArray) -> Unit,
            error: (Exception) -> Unit
        ) {
            fetchString(
                url,
                { payload, certificatePin -> success(JSONObject(payload), certificatePin) },
                error
            )
        }
    }
}