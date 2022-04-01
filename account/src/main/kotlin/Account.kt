import java.util.concurrent.ConcurrentHashMap

class Account {
    private val users: MutableMap<Int, User> = ConcurrentHashMap()

    fun createUser(userName: String): Int {
        val maxId = users.keys.maxOrNull() ?: -1
        val newId = maxId + 1
        users[newId] = User(newId, userName, 0.0, mutableMapOf())
        return newId
    }

    fun addMoney(userId: Int, amount: Double) {
        if (amount <= 0) {
            throw AccountException("The amount of money added must be a positive number")
        }
        val user = users[userId] ?: throw AccountException("There is no user with id $userId")
        user.money += amount
    }

    fun withdrawMoney(userId: Int, amount: Double) {
        val user = users[userId] ?: throw AccountException("There is no user with id $userId")
        if (amount <= 0 || amount > user.money) {
            throw AccountException("The user does not have enough funds")
        }
        user.money -= amount
    }

    fun addStocks(userId: Int, companyName: String, quantity: Int) {
        if (quantity <= 0) {
            throw AccountException("The amount of stocks added must be a positive number")
        }
        val user = users[userId] ?: throw AccountException("There is no user with id $userId")
        user.stocks[companyName] = user.stocks.getOrDefault(companyName, 0) + quantity
    }

    fun deductStocks(userId: Int, companyName: String, quantity: Int) {
        if (quantity <= 0) {
            throw AccountException("The amount of stocks added must be a positive number")
        }
        val user = users[userId] ?: throw AccountException("There is no user with id $userId")
        if (user.stocks.getOrDefault(companyName, 0) < quantity) {
            throw AccountException("The user does not have enough stocks")
        }

        user.stocks[companyName] = user.stocks[companyName]!! - quantity
    }

    fun getUser(userId: Int): User {
        return users[userId] ?: throw AccountException("There is no user with id $userId")
    }
}