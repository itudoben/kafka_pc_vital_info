ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.jh"

lazy val hello = (project in file("."))
  .settings(
    name := "VIConsumer",
    libraryDependencies += "com.eed3si9n" %% "gigahorse-okhttp" % "0.3.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka
    libraryDependencies += "org.apache.kafka" %% "kafka" % "0.10.2.2",

    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.1.0"
  )
