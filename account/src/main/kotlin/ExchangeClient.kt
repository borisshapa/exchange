import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class ExchangeClient(private val url: String, val account: Account) {
    private val client = HttpClient(CIO)

    suspend fun getStocksInfo(companyName: String): Stock {
        val json = client.get<String>("$url/get-stocks?companyName=$companyName")
        return Klaxon().parse<Stock>(json)!!
    }

    suspend fun buy(userId: Int, companyName: String, quantity: Int) {
        val stocksInfo = getStocksInfo(companyName)
        if (quantity <= 0) {
            throw AccountException("Stocks quantity must be a positive number")
        }
        if (stocksInfo.quantity < quantity) {
            throw AccountException("Only ${stocksInfo.quantity} stocks available")
        }
        account.withdrawMoney(userId, stocksInfo.price * quantity)
        client.post<HttpResponse>("$url/buy-stocks?companyName=$companyName&quantity=$quantity")
        account.addStocks(userId, companyName, quantity)
    }

    suspend fun sell(userId: Int, companyName: String, quantity: Int) {
        val stocksInfo = getStocksInfo(companyName)
        if (quantity <= 0) {
            throw AccountException("Stocks quantity must be a positive number")
        }
        account.deductStocks(userId, companyName, quantity)
        client.post<HttpResponse>("$url/add-stocks?companyName=$companyName&quantity=$quantity")
        account.addMoney(userId, stocksInfo.price * quantity)
    }
}