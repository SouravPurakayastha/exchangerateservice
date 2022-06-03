package com.example.exchangerateservice

import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(
	classes = [ExchangeRateServiceApplication::class],
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Configuration
@ComponentScan(basePackages = ["com.example.exchangerateservice"])
class ExchangeRateServiceApplicationTests {

	@Test
	fun contextLoads() {
	}
}
