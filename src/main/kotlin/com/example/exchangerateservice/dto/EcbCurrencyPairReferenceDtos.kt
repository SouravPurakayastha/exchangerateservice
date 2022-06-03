package com.example.exchangerateservice.dto

import java.util.*

data class EcbCurrencyPairReferenceRatePerDay(
    val baseCurrency: Currency,
    val convertedCurrency: Currency,
    val conversionRate: Double?,
    val asOnDate: String
)

data class EcbCurrencyPairReferenceRateResponse(
    val ecbCurrencyPairReferenceRatePerDayList: List<EcbCurrencyPairReferenceRatePerDay>
)

data class EcbCurrencyPairReferenceHistoricalRateGraphResponse(
    val baseCurrency: Currency ?= null,
    val convertedCurrency: Currency ?= null,
    val type: String,
    val uri: String
)

data class EcbSupportedCurrencyResponse(
    val supportedCurrencyList: List<String>,
    val asOnDate: String
)

data class EcbCurrencyConversionRequest(
    val sourceCurrency: String,
    val convertedCurrency: String,
    val sourceCurrencyAmount: Double
)

data class EcbCurrencyConversionResponse(
    val sourceCurrency: String,
    val convertedCurrency: String,
    val sourceCurrencyAmount: Double,
    val convertedCurrencyAmount: Double?,
    val conversionRate: Double?,
    val asOnDate: String?
)


