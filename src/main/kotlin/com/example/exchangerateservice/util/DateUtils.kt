package com.example.exchangerateservice.util

import org.apache.commons.lang3.StringUtils.upperCase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

fun isDateBeforeXDays(date: String, numberOfDays: Int): Boolean {
    val now = getCurrentLocalDateTime(ZoneId.of("CET"))
    val dateToCompare = LocalDate.parse(date)

    return now.minusDays(numberOfDays.toLong()).isBefore(dateToCompare.atStartOfDay())
}

fun getCurrentLocalDateTime(zoneId: ZoneId): LocalDateTime {
    return LocalDateTime.now(zoneId)
}

fun convertStringToLocalDateTimeFormatddMMyyyy(ddMMyyyy: String, separator: String): LocalDateTime? {
    val splitStrings = ddMMyyyy.split(separator)
    return LocalDateTime.of(splitStrings[2].toInt(), Month.valueOf(upperCase(splitStrings[1])), splitStrings[0].toInt(), 0, 0, 0, 0)
}

fun convertStringToLocalDateTimeFormatyyyyMMdd(yyyyMMdd: String, separator: String): LocalDateTime? {
    val splitStrings = yyyyMMdd.split(separator)
    return LocalDateTime.of(splitStrings[0].toInt(), Month.of(splitStrings[1].toInt()), splitStrings[2].toInt(), 0, 0, 0, 0)
}

fun convertLocalDateTimeToString(localDateTime: LocalDateTime): String {
    return localDateTime.toString().substringBefore("T")
}