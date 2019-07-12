package de.codecentric.pact.order

data class Order(val items: List<Item>, val customerId: String, val referralPartner: String?)
