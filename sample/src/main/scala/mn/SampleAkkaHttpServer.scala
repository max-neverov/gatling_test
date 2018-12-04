package mn

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import mn.model.model.User
import mn.repo.InMemoryUserRepository
import mn.rest.Responses.ResponseError

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

class SampleAkkaHttpServer extends HttpApp {

  import rest.SampleJsonProtocols._

  implicit val system = ActorSystem("sample")
  // materialize stream blueprints as running streams
  implicit val materializer = ActorMaterializer()
  implicit val nonBlockingContext = system.dispatcher

  private val shutdownPromise = Promise[Done]

  override protected def waitForShutdownSignal(system: ActorSystem)(implicit ec: ExecutionContext): Future[Done] = {
    shutdownPromise.future
  }

  private lazy val db = new InMemoryUserRepository()

  override protected def routes: Route = concat(
    pathPrefix("user") {
      concat(
        path(Segment) { name =>
          pathEndOrSingleSlash {
            concat(
              get {
                onComplete(db.getUser(name)) {
                  case Success(None) => complete(StatusCodes.NotFound, ResponseError(123, s"User $name not found"))
                  case Success(Some(user)) => complete(user)
                  case Failure(e) => complete(StatusCodes.InternalServerError,
                    ResponseError(234, s"Failed to retrieve user [$name]: ${e.getMessage}"))
                }
              },
              put {
                entity(as[User]) { user =>
                  onComplete(db.updateUser(name, user)) {
                    case Success(Right(_)) => complete(StatusCodes.Accepted)
                    case Success(Left(msg)) => complete(StatusCodes.InternalServerError,
                      ResponseError(234, s"Failed to update user: $user"))
                    case Failure(e) => complete(StatusCodes.InternalServerError,
                      ResponseError(234, s"Failed to update user $user: ${e.getMessage}"))
                  }
                }
              }
            )
          }
        },
        post {
          entity(as[User]) { user =>
            onComplete(db.saveUser(user)) {
              case Success(Right(_)) => complete(StatusCodes.Created, user)
              case Success(Left(msg)) => complete(StatusCodes.InternalServerError,
                ResponseError(234, s"Failed to save user: $user"))
              case Failure(e) => complete(StatusCodes.InternalServerError,
                ResponseError(234, s"Failed to save user $user: ${e.getMessage}"))
            }
          }
        }
      )
    }
  )

  def terminate(): Unit = {
    shutdownPromise.complete(Success(Done))
  }

}

object SampleAkkaHttpServer extends App {
  val server = new SampleAkkaHttpServer()

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      System.out.println("Got SIGINT or SIGTERM. Shutdown server")
      server.terminate()
    }
  })

  server.startServer("0.0.0.0", 8080)
}