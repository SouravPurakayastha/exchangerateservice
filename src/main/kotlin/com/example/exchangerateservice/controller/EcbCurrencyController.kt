package com.example.exchangerateservice.controller

import com.example.exchangerateservice.dto.*
import com.example.exchangerateservice.service.EcbCurrencyService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/ecb-currency")
class EcbCurrencyController(
    val ecbCurrencyService: EcbCurrencyService
) {

    @GetMapping("/pair-reference/rate")
    fun fetchEcbCurrencyConversionRate(
        @RequestParam(value = "base-currency", required = true) baseCurrency: String,
        @RequestParam(value = "converted-currency", required = true) convertedCurrency: String,
        @RequestParam(value = "number-of-days-in-the-past", required = false) numberOfDaysInThePast: Int?,
        @RequestParam(value = "latest", required = false, defaultValue = "true") latest: Boolean
    ): EcbCurrencyPairReferenceRateResponse {
        val ecbCurrencyPairReferenceRatePerDays = ecbCurrencyService.fetchEcbCurrencyConversionRate(
            baseCurrency = baseCurrency,
            convertedCurrency = convertedCurrency,
            numberOfDaysInThePast = numberOfDaysInThePast,
            latest = latest
        )
        return EcbCurrencyPairReferenceRateResponse(ecbCurrencyPairReferenceRatePerDayList = ecbCurrencyPairReferenceRatePerDays)
    }

    @GetMapping
    fun fetchEcbCurrencySupported(
        @RequestParam("asOnDate", required = false) asOnDate: String?,
    ): EcbSupportedCurrencyResponse {
        val fetchEcbCurrencySupported = ecbCurrencyService.fetchEcbCurrencySupported(asOnDate)
        return EcbSupportedCurrencyResponse(supportedCurrencyList = fetchEcbCurrencySupported.first, asOnDate = fetchEcbCurrencySupported.second)
    }

    @GetMapping("/pair-reference/graph/rate")
    fun fetchEcbCurrencyPairReferenceRateHistoricalGraph(
        @RequestParam("base-currency", required = false, defaultValue = "EUR") baseCurrency: String,
        @RequestParam("converted-currency", required = true) convertedCurrency: String
    ): EcbCurrencyPairReferenceHistoricalRateGraphResponse {
        return ecbCurrencyService.fetchEcbCurrencyPairReferenceRateHistoricalGraph(
            baseCurrency = baseCurrency,
            convertedCurrency = convertedCurrency
        )
    }

    @PostMapping("/action/conversion")
    fun convert(
        @RequestBody ecbCurrencyConversionRequest: EcbCurrencyConversionRequest,
    ): EcbCurrencyConversionResponse {
        return ecbCurrencyService.convert(
            ecbCurrencyConversionRequest
        )
    }
}