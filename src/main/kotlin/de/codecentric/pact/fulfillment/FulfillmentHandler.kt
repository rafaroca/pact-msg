package de.codecentric.pact.fulfillment

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FulfillmentHandler(private val mapper: ObjectMapper) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun handleRequest(message: String): FulfillmentOrder {
        val fulfillmentOrder = mapper.readValue(message, FulfillmentOrder::class.java)
        log.info("Received a FulfillmentOrder: $fulfillmentOrder")
        return fulfillmentOrder
    }
}