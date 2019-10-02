package de.codecentric.pact.checkout

data class Order(val items: List<Item>, val customerId: String, val referralPartner: String?)
