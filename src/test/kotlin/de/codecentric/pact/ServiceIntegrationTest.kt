import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.codecentric.pact.SQSHelper
import de.codecentric.pact.billing.BillingHandler
import de.codecentric.pact.fulfillment.FulfillmentHandler
import de.codecentric.pact.fulfillment.FulfillmentItem
import de.codecentric.pact.checkout.Item
import de.codecentric.pact.checkout.Order
import de.codecentric.pact.checkout.CheckoutService
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ServiceIntegrationTest {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        @JvmStatic
        @Container
        val localstack: LocalStackContainer = LocalStackContainer().withServices(LocalStackContainer.Service.SQS)

        val sqsContainer by lazy { SQSHelper.setupSQSTestcontainer(localstack) }
        val sqsClient by lazy { sqsContainer.first }
        val queueUrl by lazy { sqsContainer.second }
        val objectMapper = ObjectMapper()
            .registerModule(KotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Test
    fun testFulfillmentService() {
        sendOrderToQueue(
            Order(
                listOf(
                    Item("item", 650),
                    Item("secondItem", 111),
                    Item("aThirdItem", 222)
                ),
                "theCustomer",
                "myReferralPartner"
            )
        )

        log.info("Creating the FulfillmentHandler to receive orders")
        val fulfillmentHandler = FulfillmentHandler(objectMapper)

        val messageResult = sqsClient.receiveMessage(queueUrl)

        assertThat(messageResult).isNotNull()

        messageResult.messages.forEach { message ->
            val (items, customerId) = fulfillmentHandler.handleRequest(message.body)
            assertThat(items).hasSize(3)
            items.forEach(::println)
            assertThat(items).isEqualTo(
                listOf("item", "secondItem", "aThirdItem").map { FulfillmentItem(it) }
            )
            assertThat(customerId).isEqualTo("theCustomer")
        }
    }

    @Test
    fun testInvoiceService() {
        sendOrderToQueue(
            Order(
                listOf(
                    Item("item", 650),
                    Item("secondItem", 111),
                    Item("aThirdItem", 222)
                ),
                "theCustomer",
                "myReferralPartner"
            )
        )
        log.info("Creating the BillingHandler to receive orders")
        val billingHandler = BillingHandler(objectMapper)

        val messageResult = sqsClient.receiveMessage(queueUrl)

        assertThat(messageResult.messages).isNotNull()
        messageResult.messages.forEach { message ->
            val (items, customerId) = billingHandler.handleRequest(message.body)
            assertThat(items.sumBy { it.price }).isEqualTo(983)
            assertThat(customerId).isEqualTo("theCustomer")
        }
    }

    fun sendOrderToQueue(order: Order) {
        val checkoutService = CheckoutService(sqsClient, queueUrl, objectMapper)
        checkoutService.sendOrder(order)
    }
}