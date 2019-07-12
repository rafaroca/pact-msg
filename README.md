# An example project to demonstrate asynchronous contract testing

This is an example of how to use [Pact](https://pact.io) for contract testing between two services that are separated by a queue. The version 3 of the [Pact Specification](https://github.com/pact-foundation/pact-specification/tree/version-3#introduces-messages-for-services-that-communicate-via-event-streams-and-message-queues) introduces interactions for services that communicate via message queues.

The project consists of two services. The first service is the *order-service* which produces orders and sends them to a queue. The second service is the fulfilment-service which is able to consume orders.

Message passing is done via [Testcontainers](https://www.testcontainers.org/) which provides a mock for Amazon's Simple Queueing Service (SQS). 

![Conceptual drawing](drawing.png)