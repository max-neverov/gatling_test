package mn

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  private val baseUrl = "http://127.0.0.1:8080"

  val httpProtocol = http.baseUrl(baseUrl)
    .contentTypeHeader("application/json")

  val scn = scenario("Basic get simulation")
    .exec(
      http("Get user asdf")
        .get(s"$baseUrl/user/asdf"))
    .exec(
      http("Get nonexistent user")
        .get(s"$baseUrl/user/123")
        .check(status.is(404))
    )
    .exec(
      http("Post users")
        .post(s"$baseUrl/user")
        .body(StringBody("""{"name":"asdf","whatever":42}"""))
    )

  setUp(
    scn.inject(rampUsers(20) during (5 seconds))
  ).protocols(httpProtocol)

  before {
    println("Simulation is about to start!")
  }

  after {
    println("Simulation is finished!")
  }

}
