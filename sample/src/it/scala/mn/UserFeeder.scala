package mn

import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import mn.model.model.User
import mn.repo.PostgresUserRepository
import scalikejdbc.config.DBs

import scala.collection.mutable

object UserFeeder {

  import scala.concurrent.ExecutionContext.Implicits.global

  DBs.setupAll()

  private val db = new PostgresUserRepository

  def apply(): SourceFeederBuilder[User] = getUsers.circular


  def getUsers: Array[Map[String, User]] = {
    db.selectAll().foldLeft(mutable.Seq.empty[Map[String, User]]) { (acc, u) =>
      acc :+ Map("user" -> u)
    }.toArray
  }

}
