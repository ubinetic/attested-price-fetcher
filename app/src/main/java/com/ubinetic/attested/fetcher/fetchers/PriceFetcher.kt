package com.ubinetic.attested.fetcher.fetchers

import com.ubinetic.attested.fetcher.models.Price
import java.lang.Exception

interface PriceFetcher {
    fun fetchPrice(success:(Price) -> Unit, error:(Exception) -> Unit)
    fun getId():String
}