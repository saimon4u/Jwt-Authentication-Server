package example.com.data.user

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.eq

class MongoUserDataSource(
    private val db: CoroutineDatabase
): UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::userName eq username)
    }

    override suspend fun insertNewUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}