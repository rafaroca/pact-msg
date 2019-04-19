package de.codecentric.pact

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import de.codecentric.pact.Order

class OrderService(val sqs: AmazonSQS) {
    fun receiveOrder(order: Order) {
        try {
            println("Creating an SQS queue 'fulfillment'")
            val createQueueRequest = CreateQueueRequest("fulfillment")
            val myQueueUrl = sqs.createQueue(createQueueRequest).queueUrl

            println("Sending a message on the fulfillment queue")
            sqs.sendMessage(SendMessageRequest(myQueueUrl, order.toString()))

        } catch (e: AmazonServiceException) {
            println("The request was rejected by SQS with http status ${e.statusCode} and aws error ${e.errorCode}.")
            println("exception type ${e.errorType} with message ${e.message}")

        } catch (e: AmazonClientException) {
            println("The request could not be executed with message ${e.message}")
        }

    }

}