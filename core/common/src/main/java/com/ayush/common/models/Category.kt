package com.ayush.common.models

data class Category(
    val id: Long,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = true
)
