package de.codecentric.pact

import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.Pact
import au.com.dius.pact.provider.PactVerifyProvider
import au.com.dius.pact.provider.junit.Provider
import au.com.dius.pact.provider.junit.State
import au.com.dius.pact.provider.junit.loader.PactFolder
import au.com.dius.pact.provider.junit5.AmpqTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import com.amazonaws.services.sqs.AmazonSQS
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.codecentric.pact.order.Item
import de.codecentric.pact.order.Order
import de.codecentric.pact.order.OrderService
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Collections

@Provider("order-service")
@PactFolder("pacts")
class OrderServiceProviderVerificationTest {

    private val sqs: AmazonSQS = mockk()
    private val objectMapper = jacksonObjectMapper()

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun testTemplate(pact: Pact<*>, interaction: Interaction, context: PactVerificationContext) {
        println("testTemplate called: " + pact.provider.name + ", " + interaction)
        context.verifyInteraction()
    }

    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.target = AmpqTestTarget(Collections.emptyList())
    }

    @PactVerifyProvider("an order event")
    fun anOrderToExport(): String? {
        val orderService = OrderService(sqs, "localhost", objectMapper)
        val order = Order(
            listOf(
                Item("A secret machine", 1559),
                Item("A riddle", 9990),
                Item("A hidden room", 3330)
            ), "customerId"
            , "referralPartner"
        )

        val message = orderService.createSqsMessage(order)

        return message.messageBody
    }
}