import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        val exchange = ExchangeInMemory()

        class MissingParameterException(parameter: String) : Exception("$parameter parameter expected")

        routing {
            post("/add-company") {
                try {
                    val parameters = call.request.queryParameters
                    val companyName =
                        parameters["companyName"] ?: throw MissingParameterException("companyName")
                    val price = parameters["price"] ?: throw MissingParameterException("price")
                    exchange.addCompany(companyName, price.toDouble())
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
            post("/add-stocks") {
                try {
                    val parameters = call.request.queryParameters
                    val companyName =
                        parameters["companyName"] ?: throw MissingParameterException("companyName")
                    val quantity = parameters["quantity"] ?: throw MissingParameterException("quantity")
                    exchange.addStock(companyName, quantity.toInt())
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
            get("/get-stocks") {
                try {
                    val parameters = call.request.queryParameters
                    val companyName = parameters["companyName"] ?: throw MissingParameterException("companyName")
                    val stock = exchange.getStocksInfo(companyName)
                    call.respond(HttpStatusCode.OK, Klaxon().toJsonString(stock))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
            get("/get-all-stocks") {
                try {
                    val stocks = exchange.getStocks()
                    call.respond(HttpStatusCode.OK, Klaxon().toJsonString(stocks))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
            post("/buy-stocks") {
                try {
                    val parameters = call.request.queryParameters
                    val companyName =
                        parameters["companyName"] ?: throw MissingParameterException("companyName")
                    val quantity =
                        parameters["quantity"] ?: throw MissingParameterException("quantity")
                    val price = exchange.buyStocks(companyName, quantity.toInt())
                    call.respond(HttpStatusCode.OK, price.toString())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
            post("/change-exchange-rate") {
                try {
                    val parameters = call.request.queryParameters
                    val companyName =
                        parameters["companyName"] ?: throw MissingParameterException("companyName")
                    val price = parameters["price"] ?: throw MissingParameterException("price")

                    exchange.changeExchangeRate(companyName, price.toDouble())
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
        }
    }.start(wait = true)
}