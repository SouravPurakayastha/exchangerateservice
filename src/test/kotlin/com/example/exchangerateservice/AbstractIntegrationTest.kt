package com.example.exchangerateservice

import org.junit.ClassRule
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [ExchangeRateServiceApplication::class])
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        @get:ClassRule
        @JvmStatic
        val genericContainer = GenericContainer("openjdk:18.0.1")
    }
}