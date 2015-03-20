import sbt._
import sbt.Keys._

object LogicBuild extends Build {

  val features = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",

    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:reflectiveCalls"
  )

  val target = "2.11.6"
  val orgName = "com.vngrs"
  val projectName = "proto-ragnar"

  lazy val buildSettings = sbt.Defaults.coreDefaultSettings ++ Seq(
    scalaVersion := target,
    organization := orgName,
    libraryDependencies ++= (Scala.all ++ Akka.all ++ Specs.all ++ Metrics.all ++ Logging.all),
    scalacOptions ++= features,
    name := projectName
  )

  lazy val root = Project(id = projectName, base = new File("."), settings = buildSettings)

  object Scala {

    val group = "org.scala-lang"

    val scaLib = group % "scala-library" % target
    val scaRef = group % "scala-reflect" % target

    val all = Seq(scaLib, scaRef)

  }

  object Akka {

    val group = "com.typesafe.akka"
    val akkaVersion = "2.3.8"

    val akAct = group %% "akka-actor"   % akkaVersion
    val akClt = group %% "akka-cluster" % akkaVersion
    val akTst = group %% "akka-testkit" % akkaVersion % Test

    val all = Seq(akAct, akClt, akTst)

  }

  object Logging {

    val logbckVersion = "1.0.13"
    val logbck = "ch.qos.logback" % "logback-classic" % logbckVersion
    val all = Seq(logbck)

  }

  object Metrics {

    val scalaMetrics  = "nl.grons" %% "metrics-scala" % "3.3.0_a2.3"
    val metricsStatsd = "com.bealetech" % "metrics-statsd" % "2.3.0"

    val all = Seq(scalaMetrics, metricsStatsd)
  }

  object Specs {

    val specs2Version = "2.4.14"
    val mockitoVersion = "1.10.8"
    val hamcrestVersion = "1.3"

    val specs2 = "org.specs2"  %% "specs2" % specs2Version % Test
    val mockto = "org.mockito"  % "mockito-core" % mockitoVersion
    val hamLib = "org.hamcrest" % "hamcrest-library" % hamcrestVersion

    val all = Seq(specs2, mockto, hamLib)

  }
}
