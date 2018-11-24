package mn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object SampleAkkaHttpServer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem("sample")
    // materialize stream blueprints as running streams
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route = path("user") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, """{"foo":"bar"}"""))
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    StdIn.readLine() // let it run until user presses return
    bindingFuture.flatMap(_.unbind())
      .onComplete(_ => system.terminate)
  }

}
