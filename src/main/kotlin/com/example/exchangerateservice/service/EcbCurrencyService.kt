package com.example.exchangerateservice.service

import com.example.exchangerateservice.dto.EcbCurrencyConversionRequest
import com.example.exchangerateservice.dto.EcbCurrencyConversionResponse
import com.example.exchangerateservice.dto.EcbCurrencyPairReferenceHistoricalRateGraphResponse
import com.example.exchangerateservice.dto.EcbCurrencyPairReferenceRatePerDay
import com.example.exchangerateservice.exception.InvalidInputDataCustomException
import com.example.exchangerateservice.util.convertLocalDateTimeToString
import com.example.exchangerateservice.util.getCurrentLocalDateTime
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils.lowerCase
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

@Service
class EcbCurrencyService(
    val ecbDataImporterService: EcbDataImporterService,
    val serviceUsageStatisticsService: ServiceUsageStatisticsService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ECB_CURRENCY_RATE_HISTORICAL_GRAPH_URI_TEMPLATE = "https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/eurofxref-graph-CURRENCY.en.html"
    }

    fun fetchEcbCurrencyConversionRate(
        baseCurrency: String,
        convertedCurrency: String,
        numberOfDaysInThePast: Int?,
        latest: Boolean
    ): List<EcbCurrencyPairReferenceRatePerDay> {
        validateCurrency(baseCurrency, convertedCurrency)
        isCurrencySame(baseCurrency, convertedCurrency)
        updateCurrencyStats(baseCurrency, convertedCurrency)

        val baseISOCurrency = Currency.getInstance(baseCurrency)
        val convertedISOCurrency = Currency.getInstance(convertedCurrency)

        val ecbCurrencyRates = ecbDataImporterService.importEcbCurrencyPairReferenceRates()

        val dateKeys = mutableListOf<String>()
        val ecbCurrencyRatesForRequestedDates = mutableMapOf<String, Map<String, Double?>>()
        if (!latest && numberOfDaysInThePast!=null) {
            dateKeys.addAll(computeDatesForNumberOfDays(numberOfDaysInThePast = numberOfDaysInThePast))
            ecbCurrencyRatesForRequestedDates.putAll(ecbCurrencyRates.filter {
                dateKeys.contains(it.key)
            })
        } else {
            val latestDateEntry = ecbCurrencyRates.keys.first()
            dateKeys.addAll(listOf(latestDateEntry))
            val latestRates = ecbCurrencyRates.entries.firstOrNull {
                it.key.equals(
                    latestDateEntry, true
                )
            }
            if (latestRates != null) {
                ecbCurrencyRatesForRequestedDates.putAll(mapOf(latestRates.toPair()))
            }
        }

        if (baseCurrency.equals("EUR", true)) {
            return filterRatesForRequestedCurrency (
                ecbCurrencyRatesForRequestedDates = ecbCurrencyRatesForRequestedDates,
                baseISOCurrency = baseISOCurrency,
                convertedISOCurrency = convertedISOCurrency,
                convertedCurrency = convertedCurrency
            )
        } else if (convertedCurrency.equals("EUR", true)) {
            return filterRatesForRequestedCurrency (
                ecbCurrencyRatesForRequestedDates = ecbCurrencyRatesForRequestedDates,
                baseISOCurrency = convertedISOCurrency,
                convertedISOCurrency = baseISOCurrency,
                convertedCurrency = baseCurrency,
                invertRate = true
            )
        } else {
            return computeRatesForNonEuroBase(
                ecbCurrencyRatesForRequestedDates = ecbCurrencyRatesForRequestedDates,
                baseISOCurrency = baseISOCurrency,
                baseCurrency = baseCurrency,
                convertedISOCurrency = convertedISOCurrency,
                convertedCurrency = convertedCurrency
            )
        }
    }

    private fun updateCurrencyStats(baseCurrency: String, convertedCurrency: String) {
        serviceUsageStatisticsService.incrementCurrencyStats(baseCurrency)
        serviceUsageStatisticsService.incrementCurrencyStats(convertedCurrency)
    }

    fun fetchEcbCurrencySupported(asOnDate: String?): Pair<List<String>, String> {
        val ecbCurrencyRates = ecbDataImporterService.importEcbCurrencyPairReferenceRates()
        val latestDate = ecbCurrencyRates.keys.first()
        val date = asOnDate ?: latestDate
        val ratesAsOnDate = ecbCurrencyRates.entries.firstOrNull {
            it.key.equals(
                date, true
            )
        }
        return if (ratesAsOnDate == null) {
            val ratesLatest = ecbCurrencyRates.entries.firstOrNull {
                it.key.equals(
                    latestDate, true
                )
            }
            val stringCurrencyList = ratesLatest?.value?.entries?.map { it.key }?: emptyList()

            logger.info("No currencies found for the date requested [$date] | " +
                    "Instead fetching the latest currencies for latest date [$latestDate]")
            Pair(stringCurrencyList.filter { it.isNotBlank() }, latestDate)
        } else {
            val stringCurrencyList = ratesAsOnDate.value.entries.map { it.key } ?: emptyList()
            Pair(stringCurrencyList.filter { it.isNotBlank() }, date)
        }
    }

    fun fetchEcbCurrencyPairReferenceRateHistoricalGraph(
        baseCurrency: String,
        convertedCurrency: String
    ): EcbCurrencyPairReferenceHistoricalRateGraphResponse {
        validateCurrency(baseCurrency, convertedCurrency)
        updateCurrencyStats(baseCurrency, convertedCurrency)

        return EcbCurrencyPairReferenceHistoricalRateGraphResponse(
            baseCurrency = Currency.getInstance(baseCurrency),
            convertedCurrency = Currency.getInstance(convertedCurrency),
            type = "LINK",
            uri = ECB_CURRENCY_RATE_HISTORICAL_GRAPH_URI_TEMPLATE.replace("CURRENCY", lowerCase(convertedCurrency))
        )
    }

    fun convert(ecbCurrencyConversionRequest: EcbCurrencyConversionRequest): EcbCurrencyConversionResponse {
        validateCurrency(
            baseCurrency = ecbCurrencyConversionRequest.sourceCurrency,
            convertedCurrency = ecbCurrencyConversionRequest.convertedCurrency
        )
        isCurrencySame(
            baseCurrency = ecbCurrencyConversionRequest.sourceCurrency,
            convertedCurrency = ecbCurrencyConversionRequest.convertedCurrency
        )
        updateCurrencyStats(ecbCurrencyConversionRequest.sourceCurrency, ecbCurrencyConversionRequest.convertedCurrency)

        val ecbCurrencyConversionRatePerDay = fetchEcbCurrencyConversionRate(
            baseCurrency = ecbCurrencyConversionRequest.sourceCurrency,
            convertedCurrency = ecbCurrencyConversionRequest.convertedCurrency,
            numberOfDaysInThePast = null,
            latest = true
        ).firstOrNull()

        return EcbCurrencyConversionResponse(
            sourceCurrency = ecbCurrencyConversionRequest.sourceCurrency,
            convertedCurrency = ecbCurrencyConversionRequest.convertedCurrency,
            sourceCurrencyAmount = ecbCurrencyConversionRequest.sourceCurrencyAmount,
            convertedCurrencyAmount = if (ecbCurrencyConversionRatePerDay?.conversionRate != null)
                ecbCurrencyConversionRequest.sourceCurrencyAmount * ecbCurrencyConversionRatePerDay.conversionRate
            else
                null,
            conversionRate = ecbCurrencyConversionRatePerDay?.conversionRate,
            asOnDate = ecbCurrencyConversionRatePerDay?.asOnDate
        )
    }

    private fun computeRatesForNonEuroBase(
        ecbCurrencyRatesForRequestedDates: Map<String, Map<String, Double?>>,
        baseISOCurrency: Currency,
        baseCurrency: String,
        convertedISOCurrency: Currency,
        convertedCurrency: String,
    ): List<EcbCurrencyPairReferenceRatePerDay> {
        val currencyRatesBaseCurrencyVsEuro = filterRatesForRequestedCurrency(
            ecbCurrencyRatesForRequestedDates = ecbCurrencyRatesForRequestedDates,
            baseISOCurrency = Currency.getInstance("EUR"),
            convertedISOCurrency = baseISOCurrency,
            convertedCurrency = baseCurrency
        )

        val currencyRatesConvertedCurrencyVsEuro = filterRatesForRequestedCurrency(
            ecbCurrencyRatesForRequestedDates = ecbCurrencyRatesForRequestedDates,
            baseISOCurrency = Currency.getInstance("EUR"),
            convertedISOCurrency = convertedISOCurrency,
            convertedCurrency = convertedCurrency
        )

        val ecbCurrencyPairReferenceRatePerDayList = currencyRatesConvertedCurrencyVsEuro.map { currencyRatesConvertedCurrencyVsEuroPerDay ->
            val conversionRateForConvertedCurrencyVsEuro = currencyRatesConvertedCurrencyVsEuroPerDay.conversionRate

            val conversionRateForBaseCurrencyVsEuro =
                currencyRatesBaseCurrencyVsEuro.first { currencyRatesBaseCurrencyVsEuroPerDay ->
                    currencyRatesConvertedCurrencyVsEuroPerDay.asOnDate.equals(
                        currencyRatesBaseCurrencyVsEuroPerDay.asOnDate,
                        true
                    )
                }.conversionRate

            if (conversionRateForConvertedCurrencyVsEuro != null &&
                conversionRateForBaseCurrencyVsEuro != null
            ) {
                val conversionRateForConvertedCurrencyVsBaseCurrency =
                    conversionRateForConvertedCurrencyVsEuro * (1.0 / conversionRateForBaseCurrencyVsEuro)

                EcbCurrencyPairReferenceRatePerDay(
                    baseCurrency = baseISOCurrency,
                    convertedCurrency = convertedISOCurrency,
                    conversionRate = conversionRateForConvertedCurrencyVsBaseCurrency,
                    asOnDate = currencyRatesConvertedCurrencyVsEuroPerDay.asOnDate
                )
            } else {
                EcbCurrencyPairReferenceRatePerDay(
                    baseCurrency = baseISOCurrency,
                    convertedCurrency = convertedISOCurrency,
                    conversionRate = null,
                    asOnDate = currencyRatesConvertedCurrencyVsEuroPerDay.asOnDate
                )
            }
        }
        return ecbCurrencyPairReferenceRatePerDayList
    }

    private fun filterRatesForRequestedCurrency(
        ecbCurrencyRatesForRequestedDates: Map<String, Map<String, Double?>>,
        baseISOCurrency: Currency,
        convertedISOCurrency: Currency,
        convertedCurrency: String,
        invertRate: Boolean ?= false
    ): MutableList<EcbCurrencyPairReferenceRatePerDay> {
        val ecbCurrencyPairReferenceRatePerDayList = mutableListOf<EcbCurrencyPairReferenceRatePerDay>()
        ecbCurrencyRatesForRequestedDates.entries.forEach { perDayRates ->
            val conversionRate = perDayRates.value.entries.firstOrNull { it.key.equals(convertedCurrency, true) }?.value
            if (invertRate == false || invertRate == null) {
                ecbCurrencyPairReferenceRatePerDayList.add(
                    EcbCurrencyPairReferenceRatePerDay(
                        baseCurrency = baseISOCurrency,
                        convertedCurrency = convertedISOCurrency,
                        conversionRate = conversionRate,
                        asOnDate = perDayRates.key
                    )
                )
            } else {
                ecbCurrencyPairReferenceRatePerDayList.add(
                    EcbCurrencyPairReferenceRatePerDay(
                        baseCurrency = convertedISOCurrency,
                        convertedCurrency = baseISOCurrency,
                        conversionRate = if (conversionRate != null) {
                            (1.0/conversionRate)
                        } else {
                            null
                        },
                        asOnDate = perDayRates.key
                    )
                )
            }
        }
        return ecbCurrencyPairReferenceRatePerDayList
    }

    private fun computeDatesForNumberOfDays(numberOfDaysInThePast: Int): List<String> {
        val currentLocalDateTime = getCurrentLocalDateTime(ZoneId.of("CET"))
        val listOfStringDates = mutableListOf<String>()
        for (day in 1..numberOfDaysInThePast) {
            listOfStringDates.add(convertLocalDateTimeToString(currentLocalDateTime.minusDays(day.toLong())))
        }
        return listOfStringDates
    }

    private fun validateCurrency(baseCurrency: String, convertedCurrency: String) {
        try {
            Currency.getInstance(baseCurrency)
            Currency.getInstance(convertedCurrency)
        } catch (ex: Exception) {
            logger.error("Exception while processing currency input | " +
                    "Input base currency: [$baseCurrency] and converted currency: [$convertedCurrency] | Exception message: [${ex.message}]")
            throw InvalidInputDataCustomException(
                "Exception while processing currency input | " +
            "Input base currency: [$baseCurrency] and converted currency: [$convertedCurrency] | Exception message: [${ex.message}]")
        }
    }

    private fun isCurrencySame(baseCurrency: String, convertedCurrency: String) {
        if (baseCurrency.equals(convertedCurrency, true)) {
            throw InvalidInputDataCustomException("Exception while processing currency input | " +
            "base currency: [$baseCurrency] and converted currency: [$convertedCurrency] are same]")
        }
    }
}