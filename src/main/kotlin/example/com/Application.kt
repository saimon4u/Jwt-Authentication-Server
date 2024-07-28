package example.com

import example.com.data.user.MongoUserDataSource
import example.com.data.user.User
import example.com.plugins.*
import example.com.security.hashing.Sha512HashingService
import example.com.security.token.JwtTokenService
import example.com.security.token.TokenConfig
import io.ktor.server.application.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val mongoPW = System.getenv("MONGO_PW")
    val dbName = "Ktor-auth-database"
    val db = KMongo.createClient(
        connectionString = "mongodb+srv://saimon4u:$mongoPW@cluster0.ncmtpza.mongodb.net/$dbName?retryWrites=true&w=majority&appName=Cluster0",
    ).coroutine.getDatabase(dbName)
    val userDataSource = MongoUserDataSource(db)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expireAt = 180L * 24 * 3600L * 1000L,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = Sha512HashingService()


    configureSerialization()
    configureSecurity(tokenConfig)
    configureRouting(userDataSource, hashingService, tokenService, tokenConfig)
}
