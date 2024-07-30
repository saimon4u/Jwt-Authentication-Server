package example.com

import example.com.data.request.AuthRequest
import example.com.data.response.AuthResponse
import example.com.data.user.User
import example.com.data.user.UserDataSource
import example.com.security.hashing.HashingService
import example.com.security.hashing.SaltedHash
import example.com.security.token.TokenClaim
import example.com.security.token.TokenConfig
import example.com.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
){
    post("/signup"){
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val areFieldsBlank = request.userName.isBlank() || request.password.isBlank()
        val isPasswordShort = request.password.length < 6
        if(areFieldsBlank) {
            call.respond(HttpStatusCode.BadRequest, "Username or password can't be blank.")
            return@post
        }
        if(isPasswordShort){
            call.respond(HttpStatusCode.NotAcceptable, "Password must be 6 character long.")
            return@post
        }
        val storedUser = userDataSource.getUserByUsername(request.userName)
        if(storedUser != null){
            call.respond(HttpStatusCode.Conflict, "Username already in use.")
            return@post
        }
        val saltedHash = hashingService.generateSaltedHash(
            request.password
        )
        val user = User(
            userName = request.userName,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledge = userDataSource.insertNewUser(user)
        if(!wasAcknowledge){
            call.respond(HttpStatusCode.InternalServerError, "An unknown error occurred.")
            return@post
        }
        call.respond(HttpStatusCode.OK, "User created with credential.")
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
){
    post("signin"){
        val request = kotlin.runCatching { call.receiveNullable<AuthRequest>() }.getOrNull()?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val user = userDataSource.getUserByUsername(request.userName)
        if(user == null){
            call.respond(HttpStatusCode.NotFound, "Incorrect username!!")
            return@post
        }
        val isValidPassword = hashingService.verifyHash(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword){
            call.respond(HttpStatusCode.Unauthorized, "Incorrect Password!!")
            return@post
        }
        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )
        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate(){
    authenticate {
        get("authenticate"){
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo(){
    authenticate{
        get("secret"){
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, userId.toString())
        }
    }
}