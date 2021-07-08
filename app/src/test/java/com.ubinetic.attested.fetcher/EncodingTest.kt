package com.ubinetic.myapplication

import com.ubinetic.attested.fetcher.toHex
import com.ubinetic.attested.fetcher.utils.Encoding
import org.junit.Test

import org.junit.Assert.*
import java.math.BigInteger

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EncodingTest {

    @Test
    fun packNat() {
        val packedData = Encoding.packNat(BigInteger.valueOf(0))
        assertEquals(packedData.toHex(), "0000")
    }

    @Test
    fun packMap() {
        val map = HashMap<ByteArray, ByteArray>()
        map.put(Encoding.packString("0"),Encoding.packNat(BigInteger.valueOf(0)))
        map.put(Encoding.packString("1"),Encoding.packNat(BigInteger.valueOf(0)))
        val packedData = Encoding.packMap(map)
        assertEquals(packedData.toHex(), "02000000140704010000000130000007040100000001310000")
    }

    @Test
    fun packString() {
        val packedData = Encoding.packString("asdf")
        assertEquals(packedData.toHex(), "010000000461736466")
    }

    @Test
    fun packList() {
        val packedData = Encoding.packList(listOf(Encoding.packNat(BigInteger.valueOf(0)),Encoding.packNat(BigInteger.valueOf(0))))
        assertEquals(packedData.toHex(), "020000000400000000")
    }

    @Test
    fun packPair() {
        val packedData = Encoding.packPair(Encoding.packNat(BigInteger.valueOf(0)), Encoding.packNat(BigInteger.valueOf(0)))
        assertEquals(packedData.toHex(), "070700000000")
    }

    @Test
    fun packBytes() {
        val packedData = Encoding.packBytes(ByteArray(3) {1})
        assertEquals(packedData.toHex(), "0a00000003010101")
    }

    @Test
    fun packSignature() {
        val packedData = Encoding.packSignature("p2sigoocoSbrF9aqyf4dCPuL5W8Q1CijkpyfbLpYkExmrdG83a9E2dQ6QBeEzG35bug1HtMCJwzpsoHPYaHLtZs8aH7gW8KDMb")
        assertEquals(packedData.toHex(),"0a00000040ca91963f58660bff08a2db1b29b90e03adf4ed4748edb43b3856c5c7040e8d41f468c2756a8c9324a7317c052833afd3797083e0c009b3c083ea177afbafe5ad")
    }
}