package de.codecentric.pact

import au.com.dius.pact.consumer.PactFolder
import au.com.dius.pact.model.Interaction
import au.com.dius.pact.model.Pact
import au.com.dius.pact.provider.junit.Provider
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith

@Provider("order-service")
@PactFolder("pacts")
class OrderServiceProviderVerificationTest {

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun testTemplate(pact: Pact<*>, interaction: Interaction, context: PactVerificationContext) {
        println("testTemplate called: " + pact.provider.name + ", " + interaction)
        // Needs Java < 9
        context.verifyInteraction()
    }
}