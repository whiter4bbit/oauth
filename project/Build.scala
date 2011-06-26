import sbt._
import Keys._
import com.github.siasia.WebPlugin._

object Dependencies {
   val scalatraVersion = "2.0.0-SNAPSHOT"

   val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.1"
   val commonsLang = "commons-lang" % "commons-lang" % "2.3"
   val specs2 = "org.specs2" %% "specs2" % "1.3" % "test"
   val casbah = "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"      

   val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion   
   val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"
   val scalatraSpecs2 = "org.scalatra" %% "scalatra-specs" % scalatraVersion
   val scalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
   val scalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
   val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"
}

object Resolvers {
   lazy val snapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
   lazy val sonatype = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
   lazy val fuse = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"
   lazy val whiter4bbit = "whiter4bbit's repository" at "http://whiter4bbit.github.com/maven/"
   lazy val home = Resolver.file("home repo", file("/home/whiter4bbit/documents/whiter4bbit.github.com/maven/")) transactional()

   val allResolvers = Seq(home/*, snapshots*/, sonatype, fuse, whiter4bbit)   
}

object Publish {
   val publishRepo = Some(
      Resolver.file("github", 
                    new File("/home/whiter4bbit/documents/whiter4bbit.github.com/maven"))(
		    Patterns(true, Resolver.mavenStyleBasePattern)
      )      
   )
}

object OAuthBuild extends Build {
  import Dependencies._
  import Resolvers._
  import Publish._

  val buildOrganization = "info.whiter4bbit"
  val buildVersion = "1.0"
  val buildScalaVersion = "2.9.0-1"
  
  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    publishTo	 := publishRepo
  )

  val coreSettings = Seq (
    resolvers := allResolvers,
    libraryDependencies := Seq(scalaz, commonsLang, specs2),
    publishTo := publishRepo
  )

  val scalatraSettings = Seq (
    resolvers := allResolvers,
    libraryDependencies := Seq(scalatra, servletApi, slf4jBinding, specs2),
    publishTo := publishRepo
  )

  val mongoDBStorageSettings = Seq (
    resolvers := allResolvers,
    libraryDependencies := Seq(casbah),
    publishTo := publishRepo
  )

  lazy val oauth = Project(
     "oauth", 
     file("."), 
     settings = buildSettings
  ) aggregate (core, scalatraBindings, mongoDBStorage)

  lazy val core = Project(
     "oauth-core", 
     file("core") , 
     settings = buildSettings ++ coreSettings
  )

  lazy val scalatraBindings = Project(
     "oauth-scalatra", 
     file("scalatra"), 
     settings = buildSettings ++ scalatraSettings
  ) dependsOn(core)

  lazy val mongoDBStorage = Project(
     "oauth-mongodb", 
     file("mongodb"), 
     settings = buildSettings ++ mongoDBStorageSettings
  ) dependsOn(core) 

}