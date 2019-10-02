package de.codecentric.pact.checkout

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CheckoutService(private val sqs: AmazonSQS, private val queueUrl: String, private val mapper: ObjectMapper) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendOrder(order: Order) {
        try {
            log.info("Sending an order to the queue $queueUrl")
            sqs.sendMessage(createSqsMessage(order))

        } catch (e: AmazonServiceException) {
            log.info("The request was rejected by SQS with http status '${e.statusCode}' aws error '${e.errorCode}' exception type '${e.errorType}' message '${e.message}'")

        } catch (e: AmazonClientException) {
            log.info("The request could not be executed with message '${e.message}'")
        }
    }

    fun createSqsMessage(order: Order) =
        SendMessageRequest(queueUrl, mapper.writeValueAsString(order))
}