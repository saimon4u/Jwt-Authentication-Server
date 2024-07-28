package example.com.plugins

import example.com.authenticate
import example.com.data.user.UserDataSource
import example.com.getSecretInfo
import example.com.security.hashing.HashingService
import example.com.security.token.TokenConfig
import example.com.security.token.TokenService
import example.com.signIn
import example.com.signUp
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signUp(hashingService, userDataSource)
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        authenticate()
        getSecretInfo()
    }
}
