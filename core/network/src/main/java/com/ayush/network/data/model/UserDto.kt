package com.ayush.network.data.model

import com.ayush.common.models.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("email_confirmed_at") val emailConfirmedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("user_metadata") val userMetadata: UserMetadataDto? = null,
    @SerialName("last_sign_in_at") val lastSignInAt: String? = null
) {
    fun toDomain(): User = User(
        id = id.orEmpty(),
        email = email.orEmpty(),
        fullName = userMetadata?.fullName ?: "User",
        avatarUrl = userMetadata?.avatarUrl,
        isEmailVerified = emailConfirmedAt != null
    )
}
@Serializable
data class UserMetadataDto(
    @SerialName("full_name")  val fullName: String?,
    @SerialName("avatar_url") val avatarUrl: String?
)
