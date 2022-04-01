import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.testcontainers.containers.FixedHostPortGenericContainer
import kotlin.test.*

class IntegrationTests {
    private val container = FixedHostPortGenericContainer("exchange-emulator:1.0-SNAPSHOT")
        .withFixedExposedPort(8080, 8080)
        .withExposedPorts(8080)
    private val EXCHANGE_URL = "http://localhost:8080"
    private val client = HttpClient(CIO)

    @BeforeTest
    fun setUp() {
        container.start()
        runBlocking {
            client.post<HttpResponse>("$EXCHANGE_URL/add-company?companyName=VK&price=567")
            client.post<HttpResponse>("$EXCHANGE_URL/add-stocks?companyName=VK&quantity=1000")

            client.post<HttpResponse>("$EXCHANGE_URL/add-company?companyName=YNDX&price=2259")
            client.post<HttpResponse>("$EXCHANGE_URL/add-stocks?companyName=YNDX&quantity=500")

            client.post<HttpResponse>("$EXCHANGE_URL/add-company?companyName=TCS&price=2943")
            client.post<HttpResponse>("$EXCHANGE_URL/add-stocks?companyName=TCS&quantity=100")
        }
    }

    private fun TestApplicationEngine.addUser(money: Double = 0.0): Int {
        var userId: Int
        handleRequest(HttpMethod.Post, "/add-user?name=Ivan").apply {
            userId = response.content!!.toInt()
        }
        handleRequest(HttpMethod.Post, "/add-money?userId=$userId&amount=$money")
        return userId
    }

    private fun TestApplicationEngine.checkUserStocks(userId: Int, expected: List<Stock>) {
        handleRequest(HttpMethod.Get, "/get-stocks?userId=$userId").apply {
            val userStocks = Klaxon().parseArray<Stock>(response.content!!)!!
            assertEquals(expected.size, userStocks.size);
            assertTrue(expected.containsAll(userStocks));
            assertTrue(userStocks.containsAll(expected));
        }
    }

    private fun TestApplicationEngine.checkUserMoney(userId: Int, expected: Double) {
        handleRequest(HttpMethod.Get, "/get-total-money?userId=$userId").apply {
            assertEquals(expected, response.content!!.toDouble())
        }
    }

    @AfterTest
    fun tearDown() {
        container.stop()
    }

    @Test
    fun userHasNoMoney() {
        withTestApplication(Application::module) {
            val userId = addUser()
            checkUserMoney(userId, 0.0)
        }
    }

    @Test
    fun userHasNoStocks() {
        withTestApplication(Application::module) {
            val userId = addUser()
            checkUserStocks(userId, listOf())
        }
    }

    @Test
    fun buyStock() {
        withTestApplication(Application::module) {
            val userId = addUser(1000.0)
            handleRequest(HttpMethod.Post, "/buy?userId=$userId&companyName=VK&quantity=1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            checkUserStocks(userId, listOf(Stock("VK", 567.0, 1)))
            checkUserMoney(userId, 1000.0)
        }
    }

    @Test
    fun buySeveralStocks() {
        withTestApplication(Application::module) {
            val userId = addUser(4000.0)
            handleRequest(HttpMethod.Post, "/buy?userId=$userId&companyName=VK&quantity=2").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Post, "/buy?userId=$userId&companyName=YNDX&quantity=1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            checkUserStocks(userId, listOf(Stock("VK", 567.0, 2), Stock("YNDX", 2259.0, 1)))
            checkUserMoney(userId, 4000.0)
        }
    }

    @Test
    fun buyStockAndChangePrice() {
        withTestApplication({ module(testing = true) }) {
            runBlocking {
                val userId = addUser(1000.0)
                handleRequest(HttpMethod.Post, "/buy?userId=$userId&companyName=VK&quantity=1").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                checkUserStocks(userId, listOf(Stock("VK", 567.0, 1)))
                checkUserMoney(userId, 1000.0)
                HttpClient(CIO).post<String>("$EXCHANGE_URL/change-exchange-rate?companyName=VK&price=20")
                checkUserStocks(userId, listOf(Stock("VK", 20.0, 1)))
                checkUserMoney(userId, 453.0)
            }
        }
    }

    private fun checkStock(companyName: String, expected: Stock) {
        runBlocking {
            val stockJson = HttpClient(CIO).get<String>("$EXCHANGE_URL/get-stocks?companyName=$companyName")
            val stock = Klaxon().parse<Stock>(stockJson)
            assertEquals(expected, stock)
        }
    }

    @Test
    fun sellStocks() {
        withTestApplication(Application::module) {
            runBlocking {
                val userId = addUser(10000000.0)
                checkStock("VK", Stock("VK", 567.0, 1000))

                handleRequest(HttpMethod.Post, "/buy?userId=$userId&companyName=VK&quantity=250").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                checkUserStocks(userId, listOf(Stock("VK", 567.0, 250)))
                checkStock("VK", Stock("VK", 567.0, 750))

                handleRequest(HttpMethod.Post, "/sell?userId=$userId&companyName=VK&quantity=100").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }

                checkUserStocks(userId, listOf(Stock("VK", 567.0, 150)))
                checkStock("VK", Stock("VK", 567.0, 850))
            }
        }
    }
}