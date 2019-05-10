import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.codecentric.pact.FulfillmentHandler
import de.codecentric.pact.Order
import de.codecentric.pact.OrderService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                        AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "rightHere"))
                .build()

        val objectMapper = ObjectMapper()
                .registerModule(KotlinModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        println("Creating an SQS queue 'fulfillment'")
        val createQueueRequest = CreateQueueRequest("fulfillment")
        val queueUrl = sqs.createQueue(createQueueRequest).queueUrl

        println("Creating the OrderService to send an order")
        val orderService = OrderService(sqs, queueUrl, objectMapper)
        val orderProducer = GlobalScope.launch {
            while (true) {
                val order = Order(listOf("item", "secondItem", "aThirdItem"), "theCustomer", "myReferralPartner")
                orderService.sendOrder(order)
                delay(1000)
            }
        }

        println("Creating the FulfillmentHandler to receive orders")
        val fulfillmentConsumer = GlobalScope.launch {
            val fulfillmentHandler = FulfillmentHandler(objectMapper)

            while (true) {
                val messageResult = sqs.receiveMessage(queueUrl)
                messageResult?.messages?.forEach { message ->
                    message.body?.let {
                        fulfillmentHandler.handleRequest(it)
                    }
                }
                delay(500)
            }
        }

        println("Press <Enter> to exit")
        readLine()
        orderProducer.cancel()
        fulfillmentConsumer.cancel()
        sqs.deleteQueue(queueUrl)
    }
}