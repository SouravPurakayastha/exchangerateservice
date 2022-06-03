package com.example.exchangerateservice.service

import com.example.exchangerateservice.service.datasource.EcbDataImporter
import com.example.exchangerateservice.util.convertStringToLocalDateTimeFormatyyyyMMdd
import com.example.exchangerateservice.util.getCurrentLocalDateTime
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import java.time.ZoneId

@Service
class EcbDataImporterService(
    val ecbDataImporter: EcbDataImporter
){
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun shouldForceTriggerDataImport(importEcbCurrencyPairReferenceRates: Map<String, Map<String, Double?>>): Boolean {
        val localDateTimeOfLatestEntry = convertStringToLocalDateTimeFormatyyyyMMdd(
            yyyyMMdd = importEcbCurrencyPairReferenceRates.keys.first(),
            separator = "-"
        )?: return false

        val currentLocalDateTime = getCurrentLocalDateTime(ZoneId.of("CET"))

        return if (currentLocalDateTime.hour > 15) {
            !localDateTimeOfLatestEntry.isEqual(currentLocalDateTime)
        } else {
            // to handle weekends or long gaps in service usage
            // should also handle Target closing days here
            return currentLocalDateTime.minusDays(2).isAfter(localDateTimeOfLatestEntry)
        }
    }

    @CacheEvict(cacheNames= ["ecbCurrencyRates"], allEntries=true)
    fun forceImportEcbCurrencyPairReferenceRatesLatest(): Map<String, Map<String, Double?>> {
        return ecbDataImporter.importEcbCurrencyPairReferenceRates()
    }

    fun importEcbCurrencyPairReferenceRates(): Map<String, Map<String, Double?>> {
        val importEcbCurrencyPairReferenceRates = ecbDataImporter.importEcbCurrencyPairReferenceRates()
        return if (shouldForceTriggerDataImport(importEcbCurrencyPairReferenceRates)) {
            forceImportEcbCurrencyPairReferenceRatesLatest()
        } else {
            ecbDataImporter.importEcbCurrencyPairReferenceRates()
        }
    }
}