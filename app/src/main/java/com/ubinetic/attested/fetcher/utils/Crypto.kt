package com.ubinetic.attested.fetcher.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.rfksystems.blake2b.Blake2b
import com.ubinetic.attested.fetcher.Constants
import com.ubinetic.attested.fetcher.hexStringToByteArray
import com.ubinetic.attested.fetcher.models.Price
import com.ubinetic.attested.fetcher.trim
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.ASN1Sequence
import org.spongycastle.jce.ECNamedCurveTable
import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.cert.Certificate
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec

class Crypto {
    companion object {

        val SPSIG_PREFIX = "36f02c34".hexStringToByteArray()
        val P2PK_PREFIX = "03b28b7f".hexStringToByteArray()

        val BEGIN_CERT = "-----BEGIN CERTIFICATE-----"
        val END_CERT = "-----END CERTIFICATE-----"

        fun createIfNotExistsKeyStoreEntry() {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            if (!keyStore.containsAlias(Constants.SIGNER_KEY_ALIAS)) {
                val keyPairGenerator: KeyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore"
                )
                keyPairGenerator.initialize(
                    KeyGenParameterSpec.Builder(
                        Constants.SIGNER_KEY_ALIAS,
                        KeyProperties.PURPOSE_SIGN
                    ).setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                        .setDigests(
                            KeyProperties.DIGEST_NONE
                        ).setAttestationChallenge("hello world".toByteArray(charset("UTF-8")))
                        .build()
                )
                keyPairGenerator.generateKeyPair()
            }
        }

        fun priceSigner(price: Price): String {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val entry = keyStore.getEntry(Constants.SIGNER_KEY_ALIAS, null)
            val privateKey = (entry as KeyStore.PrivateKeyEntry).privateKey

            val signature = Signature.getInstance("NONEwithECDSA")
            signature.initSign(privateKey)

            val message = price.pack()

            val digest = Blake2b(256)
            digest.update(message, 0, message.size)

            val out = ByteArray(32)
            digest.digest(out, 0)
            signature.update(out)

            val payload = signature.sign()

            val asN1InputStream = ASN1InputStream(ByteArrayInputStream(payload))
            val asn1Sequence = asN1InputStream.readObject() as ASN1Sequence

            val r = asn1Sequence.getObjectAt(0) as ASN1Integer
            val s = asn1Sequence.getObjectAt(1) as ASN1Integer

            val rawSignature =
                SPSIG_PREFIX + r.value.toByteArray().trim() + s.value.toByteArray().trim()

            return Encoding.base58CheckEncode(rawSignature)
        }

        fun getPublicKey(): ByteArray {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val publicKey =
                keyStore.getCertificate(Constants.SIGNER_KEY_ALIAS).publicKey as ECPublicKey
            val ecNamedCurveTable = ECNamedCurveTable.getParameterSpec("secp256r1")
            val ecPoint =
                ecNamedCurveTable.curve.createPoint(publicKey.w.affineX, publicKey.w.affineY)
            return ecPoint.getEncoded(true)
        }

        fun getEncodedPublicKey(): String {
            return Encoding.base58CheckEncode(P2PK_PREFIX + getPublicKey())
        }

        fun getCertificateChainAsPem(): String {
            val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val certs: Array<Certificate> = keyStore.getCertificateChain(Constants.SIGNER_KEY_ALIAS)

            var result = ""
            for (cert in certs) {
                result += """
                    $BEGIN_CERT
                    ${Base64.encodeToString(cert.encoded, Base64.NO_WRAP)}
                    $END_CERT
                    
                """.trimIndent()
            }

            return result
        }
    }
}