package example.com.data.request

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val userName: String,
    val password: String
)