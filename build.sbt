import dependencies._

lazy val commonSettings = Seq(
  name := "gatling_test",
  version := "0.1",
  scalaVersion := "2.12.7"
)

lazy val foo = (project in file("foo"))
  .settings(commonSettings)

lazy val sample = (project in file("sample"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStreams,
      akkaHttpSprayJson
    )
  )