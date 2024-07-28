package example.com.data.user

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertNewUser(user: User): Boolean
}