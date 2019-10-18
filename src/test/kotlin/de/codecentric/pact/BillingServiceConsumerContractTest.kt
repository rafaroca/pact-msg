package de.codecentric.pact

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.consumer.Pact
import au.com.dius.pact.consumer.PactFolder
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.consumer.junit5.ProviderType
import au.com.dius.pact.model.v3.messaging.Message
import au.com.dius.pact.model.v3.messaging.MessagePact
import cloud.localstack.docker.annotation.LocalstackDockerProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.codecentric.pact.billing.BillingHandler
import de.codecentric.pact.billing.InvoiceItem
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "checkout-service", providerType = ProviderType.ASYNCH)
@PactFolder("pacts")
@LocalstackDockerProperties(randomizePorts = true, services = ["sqs"])
class BillingServiceConsumerContractTest {

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())

    private val billingHandler = BillingHandler(objectMapper)

    private val billingJsonBody = newJsonBody { o ->
        o.stringType("customerId", "230542")
        o.eachLike("items", 2) { items ->
            items.numberType("price", 512)
        }
    }.build()

    @Pact(consumer = "billing-service", provider = "checkout-service")
    fun exportAnOrder(builder: MessagePactBuilder): MessagePact =
        builder
            .hasPactWith("checkout-service")
            .expectsToReceive("an order to export")
            .withContent(billingJsonBody)
            .toPact()

    @Test
    @PactTestFor(pactMethod = "exportAnOrder")
    fun testExportAnOrder(messages: List<Message>) {
        for (message in messages) {
            val invoice = billingHandler.handleRequest(message.contents!!.valueAsString())

            assertEquals("230542", invoice.customerId)
            assertTrue(invoice.items.contains(InvoiceItem(512)))
            assertEquals(1024, invoice.items.sumBy { it.price })
        }
    }
}