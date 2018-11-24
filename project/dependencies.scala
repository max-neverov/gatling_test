import sbt._

object dependencies {
  private val akkaHttpVersion = "10.1.5"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  lazy val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  lazy val akkaStreams = "com.typesafe.akka" %% "akka-stream" % "2.5.18"
}