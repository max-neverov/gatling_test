package mn

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import mn.model.model.User
import mn.rest.Responses.ResponseError

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object SampleAkkaHttpServer {

  import rest.SampleJsonProtocols._

  private val db = new java.util.concurrent.ConcurrentHashMap[String, User]()

  def saveUser(user: User): Future[Either[String, Done]] = {
    try {
      db.putIfAbsent(user.name, user)
      Future.successful(Right(Done))
    } catch {
      case NonFatal(e) => Future.successful(Left(e.getMessage))
    }
  }

  def getUser(name: String): Future[Option[User]] = {
    val user = db.get(name)
    if (user == null) Future.successful(None) else Future.successful(Some(user))
  }

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("sample")
    // materialize stream blueprints as running streams
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher


    val route = pathPrefix("user") {
      concat(
        get {
          path(Segment) { name =>
            pathEndOrSingleSlash {
              onComplete(getUser(name)) {
                case Success(None) => complete(StatusCodes.NotFound, ResponseError(123, s"User $name not found"))
                case Success(Some(user)) => complete(user)
                case Failure(e) => complete(StatusCodes.InternalServerError,
                  ResponseError(234, s"Failed to retrieve user [$name]: ${e.getMessage}"))
              }
            }
          }
        },
        post {
          entity(as[User]) { user =>
            onComplete(saveUser(user)) {
              case Success(Left(msg)) => complete(StatusCodes.InternalServerError,
                ResponseError(234, s"Failed to save user: $user"))
              case Success(Right(_)) => complete(StatusCodes.Created)
              case Failure(e) => complete(StatusCodes.InternalServerError,
                ResponseError(234, s"Failed to save user $user: ${e.getMessage}"))
            }
          }
        }
      )
    }

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        System.out.println("Got SIGINT or SIGTERM. Shutdown server")
        bindingFuture.flatMap(_.unbind())
          .onComplete(_ => system.terminate)
      }
    })
  }

}
