package com.ubinetic.attested.fetcher.models

import android.util.Log
import com.ubinetic.attested.fetcher.toHex
import com.ubinetic.attested.fetcher.utils.Encoding
import java.math.BigInteger

data class Price(val exchangeId:String, val symbol:String, val price:BigInteger, val volume: BigInteger, val timestamp: BigInteger){
    fun print(){
        Log.d("PRICE","${this.exchangeId}: ${this.symbol}, ${this.price}, ${this.volume}, ${this.timestamp}")
        Log.d("PACK","${this.pack().toHex()}")
    }
    fun pack():ByteArray{
        return Encoding.packPrice(this.exchangeId, this.symbol, this.price, this.volume, this.timestamp)
    }
}