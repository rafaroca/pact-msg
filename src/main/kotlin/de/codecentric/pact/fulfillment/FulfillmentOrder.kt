package de.codecentric.pact.fulfillment

data class FulfillmentItem(val price: Int, val name: String)

data class FulfillmentOrder(val items: List<FulfillmentItem>, val customerId: String)
