package de.codecentric.pact

import java.time.Instant

data class Order(val items: List<String>, val customerId: String, val timestamp: Instant)
