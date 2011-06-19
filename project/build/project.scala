import sbt._

class OAuthProvider(info: ProjectInfo) extends ParentProject(info) {
   lazy val core = project("core", "oauth-core", new CoreProject(_))
   lazy val scalatra = project("scalatra", "oauth-scalatra", new ScalatraProject(_), core)
   lazy val mongodb_storage = project("mongodb", "mongo-db-storage", new MongoDBStorageProject(_), core)
   lazy val example_provider = project("example_provider", "oauth-example-provider", new ExampleProviderProject(_), scalatra, mongodb_storage) 

   class CoreProject(info: ProjectInfo) extends DefaultProject(info) {
      val scalaz = "org.scalaz" %% "scalaz-core" % "6.0.1"
      val commonsLang = "commons-lang" % "commons-lang" % "2.3"
      //specs
      val specs2 = "org.specs2" %% "specs2" % "1.3" % "test"
      def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
      override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
   }

   class ScalatraProject(info: ProjectInfo) extends DefaultProject(info) {
      val scalatraVersion = "2.0.0-SNAPSHOT"
      val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
      val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"
      val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"

      val specs2 = "org.specs2" %% "specs2" % "1.3" % "test"
      def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")
 
      override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
   }

   class MongoDBStorageProject(info: ProjectInfo) extends DefaultProject(info) {
      val casbah = "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"      
   }

   class ExampleProviderProject(info: ProjectInfo) extends DefaultWebProject(info) {
      val scalatraVersion = "2.0.0-SNAPSHOT"
      val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
      val scalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
      val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"
      val scalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
      val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.25" % "runtime"
      val casbah = "com.mongodb.casbah" % "casbah_2.9.0-1" % "2.1.5.0"      
      val liftJson = "net.liftweb" %% "lift-json-scalaz" % "2.4-SNAPSHOT"

      val specs2 = "org.specs2" %% "specs2" % "1.3" % "test"
      def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")		   
 
      override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
   }
   
   //repos
   lazy val scalaToolsSnapshots = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
   lazy val releases  = "releases" at "http://scala-tools.org/repo-releases/"
   lazy val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
   lazy val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"
}
