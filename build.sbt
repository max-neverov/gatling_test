import dependencies._

lazy val commonSettings = Seq(
  name := "gatling_test",
  version := "0.1",
  scalaVersion := "2.12.7",

  // do not include version in top level directory inside zip to cope with filenames in Dockerfile
  topLevelDirectory := Some(name.value)
)

lazy val foo = (project in file("foo"))
  .settings(commonSettings)

lazy val sample = (project in file("sample"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStreams,
      akkaHttpSprayJson,
      gatling,
      gatlingCharts,
      typeSafeConfig,
      scalike,
      scalikeSqlSyntaxSupport,
      scalikeConfig,
      postgres,
      logback
    )
  )
  .enablePlugins(GatlingPlugin)
  .settings(
    target in GatlingIt := baseDirectory.value / "gatling_reports"
  )

// skip packageDoc to improve compile time
// see https://sbt-native-packager.readthedocs.io/en/stable/formats/universal.html#skip-packagedoc-task-on-stage
mappings in(Compile, packageDoc) := Seq()