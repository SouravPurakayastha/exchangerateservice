package com.example.exchangerateservice.entity

import java.util.*

class EcbCurrencyPairReference (
    val baseCurrency: Currency,
    val convertedCurrency: Currency,
    val conversionRate: Double,
    val ecbPublishedDate: Date
        )