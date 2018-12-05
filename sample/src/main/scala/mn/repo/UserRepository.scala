package mn.repo

import akka.Done
import mn.model.model.User
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

trait UserRepository {

  def getUser(name: String)(implicit ec: ExecutionContext): Future[Option[User]]

  def saveUser(user: User)(implicit ec: ExecutionContext): Future[Either[String, User]]

  def updateUser(name: String, user: User)(implicit ec: ExecutionContext): Future[Either[String, Done]]

}

object UserRepository {

  object Users extends SQLSyntaxSupport[User] {
    override val tableName: String = "users"

    override def columns: Seq[String] = autoColumns[User]()

    def namedValues(user: User): Map[scalikejdbc.SQLSyntax, ParameterBinder] =
      autoNamedValues[User](user, column)

    def apply(s: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(s.resultName)(rs)

    def apply(u: ResultName[User])(rs: WrappedResultSet): User = autoConstruct(rs, u)
  }

}

