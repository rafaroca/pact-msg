import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import de.codecentric.pact.Order
import de.codecentric.pact.OrderService
import java.time.Instant.now

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                        AwsClientBuilder.EndpointConfiguration("http://localhost:4576", "rightHere"))
                .build()

        val orderService = OrderService(sqs)
        orderService.receiveOrder(Order(listOf("anItem"), "theCustomer", now()))
    }
}