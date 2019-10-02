package de.codecentric.pact.billing

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BillingHandler(private val mapper: ObjectMapper) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun handleRequest(message: String): Invoice {
        val invoice = mapper.readValue(message, Invoice::class.java)
        log.info("Received an invoice: $invoice")
        return invoice
    }
}