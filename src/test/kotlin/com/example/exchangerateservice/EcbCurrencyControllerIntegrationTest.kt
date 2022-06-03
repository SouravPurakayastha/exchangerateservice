package com.example.exchangerateservice

import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class EcbCurrencyControllerIntegrationTest : AbstractIntegrationTest() {

    @Test
    fun `should fetch ecb supported currency list for latest date if the date entered does not have an content`() {

        // WHEN
        val mvcResult = this.mockMvc.perform(
            get("/v1/ecb-currency?asOnDate=2050-05-06")
        )

        // THEN
        Assertions.assertNotNull(mvcResult)
        mvcResult
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect { jsonPath("$.supportedCurrencyList").isNotEmpty }
    }

    @Test
    fun `should fetch ecb currency rates for a given base and conversion currency for a given period`() {

        // WHEN
        val mvcResult = this.mockMvc.perform(
            get("/v1/ecb-currency/pair-reference/rate?base-currency=GBP&converted-currency=INR&number-of-days-in-the-past=10&latest=false")
        )

        // THEN
        Assertions.assertNotNull(mvcResult)
        mvcResult
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect { jsonPath("$.ecbCurrencyPairReferenceRatePerDayList").isNotEmpty }
    }

    @Test
    fun `should fetch ecb currency rates historical for a given base and conversion currency`() {

        // WHEN
        val mvcResult = this.mockMvc.perform(
            get("/v1/ecb-currency/pair-reference/graph/rate?base-currency=EUR&converted-currency=GBP")
        )

        // THEN
        Assertions.assertNotNull(mvcResult)
        mvcResult
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect { jsonPath("$.baseCurrency").isNotEmpty }
            .andExpect { jsonPath("$.convertedCurrency").isNotEmpty }
            .andExpect { jsonPath("$.type").isNotEmpty }
            .andExpect { jsonPath("$.uri").isNotEmpty }
    }
}