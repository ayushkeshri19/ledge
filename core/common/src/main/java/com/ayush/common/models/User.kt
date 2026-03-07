package com.ayush.common.models

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val isEmailVerified: Boolean
)
