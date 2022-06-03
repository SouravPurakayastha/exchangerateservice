package com.example.exchangerateservice.service

import com.example.exchangerateservice.service.datasource.EcbDataImporter
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ApplicationStartUpEventListener(
    val ecbDataImporter: EcbDataImporter
) {

    @EventListener
    fun onApplicationReady(readyEvent: ApplicationReadyEvent) {
        ecbDataImporter.importEcbCurrencyPairReferenceRates()
    }
}