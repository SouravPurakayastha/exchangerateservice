package com.example.exchangerateservice.service.datasource

import com.example.exchangerateservice.exception.GenericServerCustomException
import com.example.exchangerateservice.util.convertStringToLocalDateTimeFormatddMMyyyy
import com.example.exchangerateservice.util.isDateBeforeXDays
import com.opencsv.CSVReader
import mu.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.channels.Channels
import java.util.zip.ZipInputStream

@Service
@EnableScheduling
class EcbDataImporter {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ECB_CURRENCY_RATE_HISTORICAL_CSV_DOWNLOAD_LINK = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.zip?c79d92aed8d734d317a4a2e646009729"
        private const val LOCAL_FILENAME_HISTORICAL = "./ecbCurrencyPairReferenceRatesHistorical.zip"
        private const val DATE = "Date"
    }

    @Cacheable(value = ["ecbCurrencyRates"])
    fun importEcbCurrencyPairReferenceRates(): Map<String, Map<String, Double?>> {
        logger.info ("Downloading zip file... | LocalFilename: [$LOCAL_FILENAME_HISTORICAL]")
        downloadZip(
            fileURL = ECB_CURRENCY_RATE_HISTORICAL_CSV_DOWNLOAD_LINK,
            localFilename = LOCAL_FILENAME_HISTORICAL
        )
        logger.info ("Downloaded zip file. | LocalFilename: [$LOCAL_FILENAME_HISTORICAL]")

        val file = File(LOCAL_FILENAME_HISTORICAL)

        logger.info ("Unzipping and Reading zip file and contents | LocalFilename: [$LOCAL_FILENAME_HISTORICAL]")
        return unzipAndParsedData(file = file, processingForLatest = false)
    }

    fun downloadZip(fileURL: String, localFilename: String) {
        val url = URL(fileURL)
        try {
            val readableByteChannel = Channels.newChannel(url.openStream())
            val fileOutputStream = FileOutputStream(localFilename)
            val fileChannel = fileOutputStream.channel
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
            fileOutputStream.close()
        } catch (ex: Exception) {
            logger.error("Exception while downloading file. | Message: [${ex.message}] | Cause: [${ex.cause}]")
            throw GenericServerCustomException(
                "Exception while downloading file. | Message: [${ex.message}] | Cause: [${ex.cause}]"
            )
        }
    }

    fun unzipAndParsedData(file: File, processingForLatest: Boolean): Map<String, Map<String, Double?>> {
        val csvData = mutableMapOf<String, Map<String, Double?>>()
        val headers = mutableSetOf<String>()
        val rates = mutableListOf<Double?>()
        try {
            ZipInputStream(FileInputStream(file))
                .use { zipInputStream ->
                    generateSequence { zipInputStream.nextEntry }
                        .filterNot { it.isDirectory }
                        .forEach { fileToProcess ->
                            logger.info("Reading file. | Filename: [${fileToProcess.name}] | Size: [${fileToProcess.size}]")

                            val inputStreamReader = InputStreamReader(zipInputStream)
                            val csvReader = CSVReader(inputStreamReader)
                            var values: Array<String>?
                            do {
                                values = csvReader.readNext()
                                rates.clear()
                                if (values != null) {
                                    if (values[0].equals(DATE, true)) {
                                        headers.addAll(values.drop(1))
                                        logger.info("Reading file. | Filename: [${fileToProcess.name}] | Headers parsed")
                                    } else {
                                        if (!processingForLatest && !isDateBeforeXDays(values[0], 90)) {
                                            logger.warn("Reading file. | Filename: [${fileToProcess.name}] | Returning and terminating read as file contains data which is more than 90 days old.")

                                            logger.info("Deleting file. | Filename: [${file.name}]")
                                            deleteFile(file)
                                            return csvData
                                        }
                                        rates.addAll(values.drop(1).map { it.toDoubleOrNull() })
                                        val processedHeaderAndRates = if (processingForLatest) {
                                            processHeaderAndRates(
                                                header = headers,
                                                rates = rates.toList(),
                                                entryDate = convertStringToLocalDateTimeFormatddMMyyyy(values[0], " ").toString().substringBefore("T")
                                            )
                                        } else {
                                            processHeaderAndRates(
                                                header = headers,
                                                rates = rates.toList(),
                                                entryDate = values[0]
                                            )
                                        }
                                        csvData[processedHeaderAndRates.first] = processedHeaderAndRates.second
                                        logger.info("Reading file. | Filename: [${fileToProcess.name}] | Content parsed")
                                    }
                                }
                            } while (values != null)
                        }
                }
        } catch (ex: Exception) {
            logger.error("Exception while unzipping and/or reading file. | Message: [${ex.message}] | Cause: [${ex.cause}]")
            throw GenericServerCustomException("Exception while unzipping and/or reading file. | Message: [${ex.message}] | Cause: [${ex.cause}]")
        }

        logger.info("Deleting file. | Filename: [${file.name}]")
        deleteFile(file)
        return csvData
    }

    fun deleteFile(file: File) {
        file.delete()
    }

    fun processHeaderAndRates(header: Set<String>, rates: List<Double?>, entryDate: String): Pair<String, Map<String, Double?>> {
        if (header.size != rates.size) {
            throw GenericServerCustomException("Exception while processing headers and content due to unequal sizes. | Headersize: [${header.size}] | Contentsize: [${rates.size}]")
        }
        val mapOfCurrencyAndExchangeRates = mutableMapOf<String, Double?>()
        for (keyIndex in header.indices) {
            mapOfCurrencyAndExchangeRates[header.elementAt(keyIndex)] = rates[keyIndex]
        }
        return Pair(entryDate, mapOfCurrencyAndExchangeRates)
    }

    @CacheEvict(cacheNames= ["ecbCurrencyRates"], allEntries=true)
    @Scheduled(fixedDelay = 1000 * 60 * 2,  initialDelay = 1000 * 60 * 2)
    fun evictCacheForEcbCurrencyRates() {
        logger.info("Evicting cache. | Cache name: [ecbCurrencyRates]")
        importEcbCurrencyPairReferenceRates()
    }
}
