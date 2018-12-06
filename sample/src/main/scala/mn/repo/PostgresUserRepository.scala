package mn.repo

import akka.Done
import mn.model.model
import mn.model.model.User
import scalikejdbc.{NamedDB, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class PostgresUserRepository extends UserRepository {

  import UserRepository._

  val u = Users.syntax("u")

  override def getUser(name: String)(implicit ec: ExecutionContext): Future[Option[model.User]] = {
    Future {
      NamedDB('gtlng).readOnly { implicit session =>
        val selectQuery: scalikejdbc.ConditionSQLBuilder[User] =
          select(u.result.*)
            .from(Users as u)
            .where(sqls.eq(u.name, name))

        withSQL(selectQuery)
          .map(rs => Users.apply(u)(rs))
          .single
          .apply()
      }
    }
  }

  override def saveUser(user: model.User)(implicit ec: ExecutionContext): Future[Either[String, User]] = {
    Future[Either[String, User]] {
      NamedDB('gtlng).autoCommit { implicit session =>
        withSQL(insert.into(Users).namedValues(Users.namedValues(user)))
          .update().apply()
      }

      Right(user)
    }.recoverWith {
      case NonFatal(e) =>
        Future.successful(Left(e.getMessage))
    }
  }

  override def updateUser(name: String, user: model.User)(implicit ec: ExecutionContext): Future[Either[String, Done]] = {
    Future[Either[String, Done]] {
      NamedDB('gtlng).autoCommit { implicit session =>
        withSQL(
          update(Users).set(
            Users.column.name -> user.name,
            Users.column.whatever -> user.whatever
          ).where.eq(Users.column.name, name)
        ).update().apply()
      }
      Right(Done)
    }.recoverWith {
      case NonFatal(e) =>
        Future.successful(Left(e.getMessage))
    }
  }

  def selectAll()(implicit ec: ExecutionContext): Seq[User] = {
    NamedDB('gtlng).readOnly { implicit session =>
      val selectQuery: scalikejdbc.PagingSQLBuilder[User] =
        select(u.result.*)
          .from(Users as u)
          .limit(20000)

      withSQL(selectQuery)
        .map(rs => Users.apply(u)(rs))
        .list
        .apply()
    }
  }

}
