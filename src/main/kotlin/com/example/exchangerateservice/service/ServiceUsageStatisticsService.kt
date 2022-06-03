package com.example.exchangerateservice.service

import org.springframework.stereotype.Service

@Service
class ServiceUsageStatisticsService(
    private val currencyStats: MutableMap<String, Int>
)  {
    fun incrementCurrencyStats(currency: String) {
        if (currencyStats.containsKey(currency)) {
            val currencyCounter = currencyStats[currency]
            if (currencyCounter != null) {
                currencyStats[currency] = currencyCounter+1
            }
        } else {
            currencyStats[currency] = 0
        }
    }

    fun fetchCurrencyStats(currency: String): Int? {
        return currencyStats[currency]
    }
}