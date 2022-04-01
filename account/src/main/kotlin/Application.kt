import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") { module(testing = false) }.start(wait = true)
}

fun Application.module(testing: Boolean = false) {
    val account = Account()
    val exchangeClient = ExchangeClient("http://0.0.0.0:8080", account)

    class MissingParameterException(parameter: String) : Exception("$parameter parameter expected")

    suspend fun getUserStocks(user: User): List<Stock> {
        val userStocks = user.stocks.map {
            val stocksInfo = exchangeClient.getStocksInfo(it.key)
            stocksInfo.quantity = it.value
            stocksInfo
        }
        return userStocks
    }

    routing {
        post("/add-user") {
            try {
                val parameters = call.request.queryParameters

                val name = parameters["name"] ?: throw MissingParameterException("name")
                val id = account.createUser(name)
                call.respond(HttpStatusCode.OK, id.toString())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
        post("/add-money") {
            try {
                val parameters = call.request.queryParameters

                val userId = parameters["userId"] ?: throw MissingParameterException("userId")
                val amount = parameters["amount"] ?: throw MissingParameterException("amount")
                account.addMoney(userId.toInt(), amount.toDouble())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
        get("/get-stocks") {
            try {
                val parameters = call.request.queryParameters
                val userId = parameters["userId"] ?: throw MissingParameterException("userId")
                val stocks = getUserStocks(account.getUser(userId.toInt()))
                call.respond(HttpStatusCode.OK, Klaxon().toJsonString(stocks))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
        get("/get-total-money") {
            try {
                val parameters = call.request.queryParameters
                val userId = parameters["userId"] ?: throw MissingParameterException("userId")
                val user = account.getUser(userId.toInt())
                val stocks = getUserStocks(user)
                val sum = stocks.sumOf { it.quantity * it.price }
                call.respond(HttpStatusCode.OK, (sum + user.money).toString())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
        post("/buy") {
            try {
                val parameters = call.request.queryParameters
                val userId = parameters["userId"] ?: throw MissingParameterException("userId")
                val companyName = parameters["companyName"] ?: throw MissingParameterException("companyName")
                val quantity = parameters["quantity"] ?: throw MissingParameterException("quantity")

                exchangeClient.buy(userId.toInt(), companyName, quantity.toInt())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
        post("/sell") {
            try {
                val parameters = call.request.queryParameters
                val userId = parameters["userId"] ?: throw MissingParameterException("userId")
                val companyName = parameters["companyName"] ?: throw MissingParameterException("companyName")
                val quantity = parameters["quantity"] ?: throw MissingParameterException("quantity")

                exchangeClient.sell(userId.toInt(), companyName, quantity.toInt())
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
    }
}