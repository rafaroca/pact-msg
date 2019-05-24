package de.codecentric.pact.order

data class Item(val name: String, val price: Int)

data class Order(val items: List<Item>, val customerId: String, val referralPartner: String?)
