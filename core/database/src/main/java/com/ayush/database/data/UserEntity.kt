package com.ayush.database.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ayush.common.models.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String?,
    val isEmailVerified: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        fullName = fullName,
        avatarUrl = avatarUrl,
        isEmailVerified = isEmailVerified
    )
}