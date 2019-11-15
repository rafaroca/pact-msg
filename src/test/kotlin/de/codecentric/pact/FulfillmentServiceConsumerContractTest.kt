package de.codecentric.pact

import assertk.assertThat
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isSuccess
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
import de.codecentric.pact.fulfillment.FulfillmentItem
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import io.pactfoundation.consumer.dsl.newObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "checkout-service", providerType = ProviderType.ASYNCH)
@PactFolder("pacts")
@LocalstackDockerProperties(randomizePorts = true, services = ["sqs"])
class FulfillmentServiceConsumerContractTest {

    private val objectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(KotlinModule())

    private val fulfillmentHandler = FulfillmentHandler(objectMapper)

    val jsonBody = newJsonBody { o ->
        o.stringType("customerId", "230542")
        o.eachLike("items", 2) { items ->
            items.stringType("name", "Googly Eyes")
        }
    }.build()

    @Pact(consumer = "fulfillment-service", provider = "checkout-service")
    fun exportAnOrder(builder: MessagePactBuilder): MessagePact =
        builder
            .hasPactWith("checkout-service")
            .expectsToReceive("an order to export")
            .withContent(jsonBody)
            .toPact()

    @Test
    @PactTestFor(pactMethod = "exportAnOrder")
    fun testExportAnOrder(message: Message) {
            assertThat {
                fulfillmentHandler.handleRequest(message.contents!!.valueAsString())
            }.isSuccess()
    }
}