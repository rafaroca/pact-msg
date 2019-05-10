package de.codecentric.pact

data class Order(val items: List<String>, val customerId: String, val referralPartner: String)
