package com.ayush.transactions.domain.models

sealed interface TransactionListItem {
    data class Header(val dateLabel: String) : TransactionListItem
    data class Item(val transaction: Transaction) : TransactionListItem
}
