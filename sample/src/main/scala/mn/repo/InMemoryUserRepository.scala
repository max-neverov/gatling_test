package mn.repo

import akka.Done
import mn.model.model.User

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class InMemoryUserRepository extends UserRepository {

  private val db = new java.util.concurrent.ConcurrentHashMap[String, User]()

  override def getUser(name: String)(implicit ec: ExecutionContext): Future[Option[User]] = {
    val user = db.get(name)
    if (user == null) Future.successful(None) else Future.successful(Some(user))
  }

  override def saveUser(user: User)(implicit ec: ExecutionContext): Future[Either[String, User]] = {
    try {
      val prev = db.putIfAbsent(user.name, user)
      if (prev == null) Future.successful(Right(user))
      else Future.successful(Left("User " + user + " is already existed"))
    } catch {
      case NonFatal(e) => Future.successful(Left(e.getMessage))
    }
  }

  override def updateUser(name: String, user: User)(implicit ec: ExecutionContext): Future[Either[String, Done]] = {
    try {
      db.put(name, user)
      Future.successful(Right(Done))
    } catch {
      case NonFatal(e) => Future.successful(Left(e.getMessage))
    }
  }
}

