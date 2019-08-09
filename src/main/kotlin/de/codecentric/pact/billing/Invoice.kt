package de.codecentric.pact.billing

data class Invoice(val items: List<InvoiceItem>, val customerId: String)