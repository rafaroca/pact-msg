package de.codecentric.pact

import com.fasterxml.jackson.databind.ObjectMapper

class FulfillmentHandler(private val mapper: ObjectMapper) {

    fun handleRequest(message: String) {
        val fulfillmentOrder = mapper.readValue(message, FulfillmentOrder::class.java)
        println("Received a FulfillmentOrder: $fulfillmentOrder")
    }
}