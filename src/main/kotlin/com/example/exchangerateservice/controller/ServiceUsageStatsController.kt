package com.example.exchangerateservice.controller

import com.example.exchangerateservice.dto.CurrencyStatsResponse
import com.example.exchangerateservice.service.ServiceUsageStatisticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/service-usage")
class ServiceUsageStatsController(
    val serviceUsageStatisticsService: ServiceUsageStatisticsService
) {
    @GetMapping("/currency-stats")
    fun fetchCurrencyStats(
        @RequestParam(value = "currency", required = true) currency: String
    ): CurrencyStatsResponse {
        return CurrencyStatsResponse(
            currency = currency,
            usageCount = serviceUsageStatisticsService.fetchCurrencyStats(currency)?: 0
        )
    }
}