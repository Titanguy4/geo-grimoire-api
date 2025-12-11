name := "geo-grimoire-api"

version := "0.1.0"

scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.19",
  "dev.zio" %% "zio-http" % "3.0.0-RC4",
  "dev.zio" %% "zio-json" % "0.6.2",
  "dev.zio" %% "zio-logging" % "2.5.2",
  "io.getquill" %% "quill-jdbc-zio" % "4.8.0",
  "org.postgresql" % "postgresql" % "42.7.1",
  "com.zaxxer" % "HikariCP" % "5.1.0",
  "dev.zio" %% "zio-test" % "2.0.19" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.0.19" % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
