import sbt._

object dependencies {
  private val akkaHttpVersion = "10.1.5"
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  lazy val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

  lazy val akkaStreams = "com.typesafe.akka" %% "akka-stream" % "2.5.18"

  lazy val gatlingCharts = "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.1" % "test,it"
  lazy val gatling = "io.gatling" % "gatling-test-framework" % "3.0.1" % "test,it"

  lazy val typeSafeConfig = "com.typesafe" % "config" % "1.3.3"

  lazy val scalike = "org.scalikejdbc" %% "scalikejdbc" % "3.3.1"
  lazy val scalikeSqlSyntaxSupport = "org.scalikejdbc" %% "scalikejdbc-syntax-support-macro" % "3.3.1"
  lazy val scalikeConfig = "org.scalikejdbc" %% "scalikejdbc-config" % "3.3.1"

  lazy val postgres = "org.postgresql" % "postgresql" % "42.2.5"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

}