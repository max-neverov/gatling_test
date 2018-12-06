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

  def apply(offset: Int, limit: Int): SourceFeederBuilder[User] = getUsers(offset, limit).circular


  def getUsers(offset: Int, limit: Int): Array[Map[String, User]] = {
    db.selectAll(offset, limit).foldLeft(mutable.Seq.empty[Map[String, User]]) { (acc, u) =>
      acc :+ Map("user" -> u)
    }.toArray
  }

}
