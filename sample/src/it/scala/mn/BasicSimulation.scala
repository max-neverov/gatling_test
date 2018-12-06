package mn

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import mn.model.model.User
import mn.repo.PostgresUserRepository
import scalikejdbc.config.DBs

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  private val config = ConfigFactory.load("gatling.conf").getConfig("testConfig")

  private val baseUrl = config.getString("baseUrl")

  DBs.setupAll()
  private val db = new PostgresUserRepository
  private val sampleUser = User("asdf", 42)

  before {
    import scala.concurrent.ExecutionContext.Implicits.global
    db.saveUser(sampleUser)
  }

  after {
    DBs.closeAll()
  }

  val httpProtocol = http.baseUrl(baseUrl)
    .contentTypeHeader("application/json")

  val basicGetScn = scenario("Basic get scenario")
    .group("GET") {
      exec(
        http("Get user asdf")
          .get(s"$baseUrl/user/asdf")
          .check(status.is(200))
      )
    }

  val get404Scn = scenario("Get non existent user scenario")
    .group("GET") {
      exec(
        http("Get nonexistent user")
          .get(s"$baseUrl/user/nonExistent")
          .check(status.is(404))
      )
    }

  setUp(
    basicGetScn.inject(
      atOnceUsers(10),
      constantUsersPerSec(60) during (10 seconds), // the same as rampUsers(600) during (10 seconds)
      rampUsersPerSec(50) to 350 during (10 seconds),
      heavisideUsers(3000) during (60 seconds)
    ).protocols(httpProtocol),
    get404Scn.inject(
      nothingFor(10 seconds),
      constantUsersPerSec(60) during (10 seconds)
    ).throttle(
      reachRps(10) in (20 seconds),
      holdFor(20 seconds),
      jumpToRps(20),
      holdFor(40 seconds)
    ),
  ).assertions(
    global.successfulRequests.percent.gt(99),
    details("GET").responseTime.percentile4.lt(100)
  )

}
