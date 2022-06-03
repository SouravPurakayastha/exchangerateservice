package com.example.exchangerateservice.controller

import com.example.exchangerateservice.exception.GenericServerCustomException
import com.example.exchangerateservice.exception.InvalidInputDataCustomException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandler {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @ExceptionHandler(GenericServerCustomException::class)
    fun handleGenericCustomException(ex: GenericServerCustomException): ResponseEntity<Any> {
        logger.error("Exception encountered | Message: [${ex.message}]")
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)

    }

    @ExceptionHandler(InvalidInputDataCustomException::class)
    fun handleInvalidInputDataCustomException(ex: InvalidInputDataCustomException): ResponseEntity<Any> {
        logger.error("Exception encountered | Message: [${ex.message}]")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ex.message)

    }
}