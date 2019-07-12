package de.codecentric.pact.fulfillment

data class FulfillmentOrder(val items: List<FulfillmentItem>, val customerId: String)
