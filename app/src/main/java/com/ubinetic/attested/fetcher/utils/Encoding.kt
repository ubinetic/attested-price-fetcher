package com.ubinetic.attested.fetcher.utils

import com.ubinetic.attested.fetcher.trim
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import java.math.BigInteger

class Encoding {
    companion object {
        enum class Tag(val value: Byte) {
            Integer(0),
            String(1),
            Prim2(7),
            Message(5)
        }

        fun base58CheckEncode(payload: ByteArray): String {
            val checksum = Sha256Hash.hashTwice(payload, 0, payload.size)
            return Base58.encode(payload + checksum.sliceArray(0..3))
        }

        fun zarithEncode(value: BigInteger): String {
            var result = ""
            var runnerBigInteger = value

            while (runnerBigInteger >= BigInteger.valueOf(128)) {
                var part = runnerBigInteger % BigInteger.valueOf(128)
                runnerBigInteger -= part
                runnerBigInteger /= BigInteger.valueOf(128)
                part += BigInteger.valueOf(128)
                result += part.toString(16).padStart(2, '0')
            }

            result += runnerBigInteger.toString(16).padStart(2, '0')
            return result
        }

        fun packPrice(
            exchangeId: String,
            symbol: String,
            price: BigInteger,
            volume: BigInteger,
            timestamp: BigInteger
        ): ByteArray {
            return packMessage(
                packPair(
                    packString(exchangeId),
                    packPair(
                        packString(symbol),
                        packPair(
                            packNat(price),
                            packPair(packNat(volume), packNat(timestamp))
                        )
                    )
                )
            )
        }

        fun packNat(value: BigInteger): ByteArray {
            var payload = ByteArray(1) { Tag.Integer.value }
            var runner = value
            var i = 0
            do {
                val bits = if (i == 0) 6 else 7
                var byte =
                    runner.and(BigInteger.valueOf(1).shiftLeft(bits).subtract(BigInteger.ONE))
                runner = runner.shiftRight(bits)
                if (runner > BigInteger.ZERO) {
                    byte = byte.or(BigInteger.valueOf(128))
                }
                payload += byte.toByteArray().trim()
                i++;
            } while (runner > BigInteger.ZERO)
            return payload
        }

        fun packString(value: String): ByteArray {
            var payload = ByteArray(1) { Tag.String.value }

            val bytePayload = value.toByteArray(Charsets.US_ASCII) // TODO check 7bit ascii
            var length32Bits = bytePayload.size.toBigInteger().toByteArray().trim()
            while (length32Bits.size < 4) {
                length32Bits = ByteArray(1) + length32Bits
            }

            return payload + length32Bits + bytePayload
        }

        fun packPair(first: ByteArray, second: ByteArray): ByteArray {
            var payload = ByteArray(2) { Tag.Prim2.value }
            return payload + first + second
        }

        fun packMessage(value: ByteArray): ByteArray {
            return ByteArray(1) { Tag.Message.value } + value
        }
    }
}