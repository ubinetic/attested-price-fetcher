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
            Elt(4),
            Prim2(7),
            Message(5),
            List(2),
            Bytes(10),
        }

        fun base58CheckEncode(payload: ByteArray): String {
            val checksum = Sha256Hash.hashTwice(payload, 0, payload.size)
            return Base58.encode(payload + checksum.sliceArray(0..3))
        }

        fun base58CheckDecode(payload: String): ByteArray {
            val checkedPayload = Base58.decode(payload)
            val checksum =
                checkedPayload.sliceArray(checkedPayload.size - 4 until checkedPayload.size)
            val payload = checkedPayload.sliceArray(0..checkedPayload.size - 5)
            val calculatedChecksum = Sha256Hash.hashTwice(payload, 0, payload.size)

            if (checksum.contentEquals(calculatedChecksum.sliceArray(0..3))) {
                return payload
            } else {
                return ByteArray(0)
            }
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
            timestamp: BigInteger,
            certificateSha256: ByteArray
        ): ByteArray {
            return packMessage(
                packPair(
                    packString(exchangeId),
                    packPair(
                        packString(symbol),
                        packPair(
                            packNat(price),
                            packPair(
                                packNat(volume),
                                packPair(packNat(timestamp), packBytes(certificateSha256))
                            )
                        )
                    )
                )
            )

        }

        fun packNat(value: BigInteger): ByteArray {
            var payload = ByteArray(0)
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
                i++
            } while (runner > BigInteger.ZERO)

            if (payload.isEmpty()) {
                return ByteArray(2) { Tag.Integer.value }
            } else {
                return ByteArray(1) { Tag.Integer.value } + payload
            }
        }

        fun forgeArray(bytePayload: ByteArray): ByteArray {
            var length32Bits = bytePayload.size.toBigInteger().toByteArray().trim()
            while (length32Bits.size < 4) {
                length32Bits = ByteArray(1) + length32Bits
            }
            return length32Bits + bytePayload
        }

        fun packBytes(value: ByteArray): ByteArray {
            return ByteArray(1) { Tag.Bytes.value } + forgeArray(value)
        }

        fun packString(value: String): ByteArray {
            val bytePayload = value.toByteArray(Charsets.US_ASCII) // TODO check 7bit ascii
            return ByteArray(1) { Tag.String.value } + forgeArray(bytePayload)
        }

        fun packList(values: List<ByteArray>): ByteArray {
            var listPayload = ByteArray(0)
            for (value in values) {
                listPayload += value
            }

            var length32Bits = listPayload.size.toBigInteger().toByteArray().trim()
            while (length32Bits.size < 4) {
                length32Bits = ByteArray(1) + length32Bits
            }

            return ByteArray(1) { Tag.List.value } + length32Bits + listPayload
        }

        fun packMap(map: Map<ByteArray, ByteArray>): ByteArray {
            var listPayload = arrayListOf<ByteArray>()
            for (entry in map) {
                listPayload.add(packMapPair(entry.key, entry.value))
            }
            return packList(listPayload)
        }

        fun packSignature(signature: String): ByteArray {
            val payload = Encoding.base58CheckDecode(signature)
            return packBytes(payload.sliceArray(4 until payload.size))
        }

        fun packMapPair(first: ByteArray, second: ByteArray): ByteArray {
            var payload = ByteArray(1) { Tag.Prim2.value } + ByteArray(1) { Tag.Elt.value }
            return payload + first + second
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