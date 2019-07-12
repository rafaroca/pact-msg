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
import de.codecentric.pact.fulfillment.FulfillmentHandler
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import io.pactfoundation.consumer.dsl.newObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "order-service", providerType = ProviderType.ASYNCH)
@PactFolder("pacts")
@LocalstackDockerProperties(randomizePorts = true, services = ["sqs"])
class FulfillmentServiceConsumerContractTest {

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())

    private val fulfillmentHandler = FulfillmentHandler(objectMapper)

    private val testCustomerId = "230542"

    @Pact(consumer = "fulfillment-service", provider = "order-service")
    fun exportAnOrderTo(builder: MessagePactBuilder): MessagePact = builder.hasPactWith("order-service")
        .given("An order with three items")
        .expectsToReceive("an order to export")
        .withContent(
            newJsonBody { o ->
                o.stringType("customerId", testCustomerId)
                o.array("items") { a ->
                    a.newObject { item ->
                        item.stringType("name", "A Teddy Bear")
                        item.numberType("price", 1295)
                    }
                    a.newObject { item ->
                        item.stringType("name", "Googly Eyes")
                        item.numberType("price", 59)
                    }
                    a.newObject { item ->
                        item.stringType("name", "Goofy Socks")
                        item.numberType("price", 100)
                    }
                }
            }.build()
        )
        .toPact()

    @Test
    @PactTestFor(pactMethod = "exportAnOrderTo")
    fun testExportAnOrder(messages: List<Message>) {
        for (message in messages) {
            val (customerId, sum) = fulfillmentHandler.handleRequest(message.contents!!.valueAsString())

            assertEquals(testCustomerId, customerId)
            assertEquals(1454, sum)
        }
    }
}