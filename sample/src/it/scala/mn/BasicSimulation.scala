package mn

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import mn.model.model.User
import scalikejdbc.config.DBs

import scala.concurrent.duration._

class BasicSimulation extends Simulation {

  import rest.SampleJsonProtocols._
  import spray.json._

  private val config = ConfigFactory.load("gatling.conf").getConfig("testConfig")

  private val baseUrl = config.getString("baseUrl")

  val getUsersFeeder = UserFeeder(0, 20000)

  after {
    DBs.closeAll()
  }

  val httpProtocol = http.baseUrl(baseUrl)
    .contentTypeHeader("application/json")

  val basicGetScn = scenario("Basic get scenario")
    .feed(getUsersFeeder)
    .group("GET") {
      exec(
        http("Get users")
          .get(session => "/user/" + session("user").as[User].name)
          .check(status.is(200))
          .check(jsonPath("$.whatever").is("${user._2}"))
      )
    }

  val get404Scn = scenario("Get non existent user scenario")
    .group("GET") {
      exec(
        http("Get nonexistent user")
          .get("/user/nonExistent")
          .check(status.is(404))
      )
    }

  val updateSuccessUsersFeeder = UserFeeder(20000, 1000)

  val basicUpdateScn = scenario("Basic update scenario")
    .feed(getUsersFeeder)
    .group("UPDATE") {
      exec(
        http("Update users")
          .put(session => "/user/" + session("user").as[User].name)
          .body(StringBody(session => session("user").as[User].toJson.toString())).asJson
          .check(status.is(202))
        // fail every 40-th update
      ).doIf(session => session("user").as[User].whatever % 40 == 0) {
        exec(
          http("Update nonexistent users")
            .put(session => "/user/" + session("user").as[User].name + "nonexistent")
            .body(StringBody(session => {
              val user = session("user").as[User]
              user.copy(name = user.name + "nonexistent").toJson.toString()
            })).asJson
            .check(status.is(500))
        )
      }
    }

  val updateInterfereWithGetUsersFeeder = UserFeeder(2000, 200)

  val updateInGetRangeScn = scenario("One more update")
    .feed(updateInterfereWithGetUsersFeeder)
    .group("UPDATE") {
      exec(
        http("Update interfere users")
          .put(session => "/user/" + session("user").as[User].name)
          .body(StringBody(session => session("user").as[User].toJson.toString())).asJson
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
    ).protocols(httpProtocol),
    basicUpdateScn.inject(
      nothingFor(5 seconds),
      heavisideUsers(800) during (20 seconds)
    ).protocols(httpProtocol),
    updateInGetRangeScn.inject(
      constantUsersPerSec(60) during (10 seconds)
    ).protocols(httpProtocol)
  ).assertions(
    global.successfulRequests.percent.gt(99),
    details("GET").responseTime.percentile4.lt(200),
    details("GET").failedRequests.percent.lt(2),
    details("UPDATE").responseTime.percentile4.lt(350),
    details("UPDATE").failedRequests.percent.lt(2)
  )

}
