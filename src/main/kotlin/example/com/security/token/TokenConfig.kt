package example.com.security.token

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val expireAt: Long,
    val secret: String
)