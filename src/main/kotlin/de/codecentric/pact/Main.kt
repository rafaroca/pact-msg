import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.codecentric.pact.billing.BillingHandler
import de.codecentric.pact.fulfillment.FulfillmentHandler
import de.codecentric.pact.order.Item
import de.codecentric.pact.order.Order
import de.codecentric.pact.order.OrderService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val sqs = AmazonSQSClientBuilder.standard()
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "rightHere")
            )
            .build()

        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        log.info("Creating an SQS queue 'fulfillment'")
        val createQueueRequest = CreateQueueRequest("fulfillment")
        val queueUrl = sqs.createQueue(createQueueRequest).queueUrl

        log.info("Creating the OrderService to send an order")
        val orderService = OrderService(sqs, queueUrl, objectMapper)
        val orderProducer = GlobalScope.launch {
            while (true) {
                val order = Order(
                    listOf(
                        Item("item", 650),
                        Item("secondItem", 111),
                        Item("aThirdItem", 222)
                    ),
                    "theCustomer",
                    "myReferralPartner"
                )
                orderService.sendOrder(order)
                delay(1000)
            }
        }

        log.info("Creating the FulfillmentHandler to receive orders")
        val fulfillmentConsumer = GlobalScope.launch {
            val fulfillmentHandler = FulfillmentHandler(objectMapper)

            while (true) {
                val messageResult = sqs.receiveMessage(queueUrl)
                messageResult?.messages?.forEach { message ->
                    message.body?.let {
                        val (items, customerId) = fulfillmentHandler.handleRequest(it)
                        log.info("Shipping ${items.size} items to customer $customerId")
                    }
                }
                delay(500)
            }
        }

        log.info("Creating the BillingHandler to receive orders")
        val billingConsumer = GlobalScope.launch {
            val billingHandler = BillingHandler(objectMapper)

            while (true) {
                val messageResult = sqs.receiveMessage(queueUrl)
                messageResult?.messages?.forEach { message ->
                    message.body?.let {
                        val (items, customerId) = billingHandler.handleRequest(it)
                        log.info("Sending invoice with total € ${items.sumBy { it.price }} items to customer $customerId")
                    }
                }
                delay(500)
            }
        }

        log.info("Press <Enter> to exit")
        readLine()
        orderProducer.cancel()
        fulfillmentConsumer.cancel()
        billingConsumer.cancel()
        sqs.deleteQueue(queueUrl)
    }
}