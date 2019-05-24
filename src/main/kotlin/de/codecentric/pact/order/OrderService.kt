package de.codecentric.pact.order

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper

class OrderService(private val sqs: AmazonSQS, private val queueUrl: String, private val mapper: ObjectMapper) {

    fun sendOrder(order: Order) {
        try {
            println("Sending an order on to fulfillment queue")
            sqs.sendMessage(SendMessageRequest(queueUrl, mapper.writeValueAsString(order)))

        } catch (e: AmazonServiceException) {
            println("The request was rejected by SQS with http status '${e.statusCode}' aws error '${e.errorCode}' exception type '${e.errorType}' message '${e.message}'")

        } catch (e: AmazonClientException) {
            println("The request could not be executed with message '${e.message}'")
        }
    }
}