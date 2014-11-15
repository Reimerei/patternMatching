import sbt._
import Keys._
import play.Play._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._
import com.typesafe.sbt.packager.universal.UniversalKeys
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build with UniversalKeys {


  val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

  override def rootProject = Some(scalajvm)

  val sharedSrcDir = "scala"

  lazy val scalajvm = Project(
    id = "scalajvm",
    base = file("scalajvm")
  ) enablePlugins (play.PlayScala) settings (scalajvmSettings: _*) aggregate (scalajs) aggregate (sharedScala)

  lazy val scalajs = Project(
    id   = "scalajs",
    base = file("scalajs")
  ) settings (scalajsSettings: _*) aggregate (sharedScala)

  lazy val sharedScala = Project(
    id = "scala",
    base = file(sharedSrcDir)
  ) settings (sharedScalaSettings: _*) 

  lazy val scalajvmSettings =
    Seq(
      name := """patternMatching""",
      version := Versions.app,
      scalaVersion := Versions.scala,
      scalacOptions ++= Seq("-feature"),
      routesImport += "config.Routes._",
      scalajsOutputDir := (classDirectory in Compile).value / "public" / "javascripts",
      compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (scalajs, Compile)) dependsOn copySourceMapsTask,
      dist <<= dist dependsOn (fullOptJS in (scalajs, Compile)),
      stage <<= stage dependsOn (fullOptJS in (scalajs, Compile)),
      libraryDependencies ++= Dependencies.scalajvm.value,
      resolvers += Resolver.url("scala-js-releases", url("http://dl.bintray.com/content/scala-js/scala-js-releases"))( Resolver.ivyStylePatterns),  
      EclipseKeys.skipParents in ThisBuild := false,
      commands += preStartCommand
    ) ++ (
      // ask scalajs project to put its outputs in scalajsOutputDir
      Seq(packageExternalDepsJS, packageInternalDepsJS, packageExportedProductsJS, packageLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
        crossTarget in (scalajs, Compile, packageJSKey) := scalajsOutputDir.value
      }
    ) ++ sharedDirectorySettings

  lazy val scalajsSettings =
    scalaJSSettings ++ Seq(
      name := "scalajs-set",
      version := Versions.app,
      scalaVersion := Versions.scala,
      persistLauncher := true,
      persistLauncher in Test := false,
      relativeSourceMaps := true,
      libraryDependencies ++= Dependencies.scalajs.value
    ) ++ sharedDirectorySettings

  lazy val sharedScalaSettings =
    Seq(
      name := "shared-scala-example",
      scalaVersion := Versions.scala,
      libraryDependencies ++= Dependencies.shared.value
    )

  lazy val sharedDirectorySettings = Seq(
    unmanagedSourceDirectories in Compile += new File((file(".") / sharedSrcDir / "src" / "main" / "scala").getCanonicalPath),
    unmanagedSourceDirectories in Test += new File((file(".") / sharedSrcDir / "src" / "test" / "scala").getCanonicalPath),
    unmanagedResourceDirectories in Compile += file(".") / sharedSrcDir / "src" / "main" / "resources",
    unmanagedResourceDirectories in Test += file(".") / sharedSrcDir / "src" / "test" / "resources"
  )

  val copySourceMapsTask = Def.task {
    val scalaFiles = (Seq(sharedScala.base, scalajs.base) ** ("*.scala")).get
    for (scalaFile <- scalaFiles) {
      val target = new File((classDirectory in Compile).value, scalaFile.getPath)
      IO.copyFile(scalaFile, target)
    }
  }

  // Use reflection to rename the 'start' command to 'play-start'
  Option(play.Play.playStartCommand.getClass.getDeclaredField("name")) map { field =>
    field.setAccessible(true)
    field.set(playStartCommand, "play-start")
  }

  // The new 'start' command optimises the JS before calling the Play 'start' renamed 'play-start'
  val preStartCommand = Command.args("start", "<port>") { (state: State, args: Seq[String]) =>
    Project.runTask(fullOptJS in (scalajs, Compile), state)
    state.copy(remainingCommands = ("play-start " + args.mkString(" ")) +: state.remainingCommands)
  }
}

object Dependencies {
  val shared = Def.setting(Seq())

  val scalajvm = Def.setting(shared.value ++ Seq(
    filters,
    jdbc,
    anorm,
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.typesafe.play" %% "play-slick" % "0.8.0",
    "com.lihaoyi" %% "upickle" % "0.2.4",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "jquery" % "2.1.1",
    "org.webjars" % "codemirror" % "4.3",
    "org.webjars" % "bootstrap" % "3.2.0",
    "org.webjars" % "font-awesome" % "4.1.0",
    "org.scalajs" %% "scalajs-pickling-play-json" % "0.3.1",
    "com.typesafe.akka" %% "akka-persistence-experimental" % "2.3.4"
  ) )

  val scalajs = Def.setting(shared.value ++ Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % Versions.scalajsDom,
    "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test",
    "com.lihaoyi" %%% "upickle" % "0.2.4",
    "com.scalatags" %%% "scalatags" % "0.4.0",
    "com.scalarx" %%% "scalarx" % "0.2.6",
    "org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6",
    "org.scalajs" %%% "scalajs-pickling" % "0.3.1"
  ))
}

object Versions {
  val app = "0.1.0-SNAPSHOT"
  val scala = "2.11.2"
  val scalajsDom = "0.6"
}
