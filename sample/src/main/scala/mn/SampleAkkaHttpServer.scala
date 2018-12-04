package mn

import java.util.concurrent.atomic.AtomicLong

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

  private var saveAcc = new AtomicLong(0)
  private var saveCnt = new AtomicLong(0)

  private var getAcc = new AtomicLong(0)
  private var getCnt = new AtomicLong(0)

  def saveUser(user: User): Future[Either[String, Done]] = {
    val start = System.nanoTime()
    try {
      db.putIfAbsent(user.name, user)
      Future.successful(Right(Done))
    } catch {
      case NonFatal(e) => Future.successful(Left(e.getMessage))
    } finally {
      saveCnt.incrementAndGet()
      saveAcc.addAndGet(System.nanoTime() - start)
    }
  }

  def getUser(name: String): Future[Option[User]] = {
    val start = System.nanoTime()

    try {
      val user = db.get(name)
      if (user == null) Future.successful(None) else Future.successful(Some(user))
    } finally {
      getCnt.incrementAndGet()
      getAcc.addAndGet(System.nanoTime() - start)
    }
  }

  def printTimes(): String = {
    var res = s"Save median (${saveAcc.get}; ${saveCnt.get}): " + (1.0 * saveAcc.get() / saveCnt.get) / 1000000
    res += s"\n Get median (${getAcc.get}; ${getCnt.get}): " + (1.0 * getAcc.get() / getCnt.get) / 1000000

    println(res)
    res
  }

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("sample")
    // materialize stream blueprints as running streams
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher


    val route = concat(
      pathPrefix("user") {
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
      },
      pathPrefix("stat") {
        get {
          pathEndOrSingleSlash {
            complete(printTimes())
          }
        }
      }
    )

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
