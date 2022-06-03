package com.example.exchangerateservice.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class BeanConfig {
    @Bean
    fun currencyStats(): MutableMap<String, Int> {
        return mutableMapOf()
    }
}