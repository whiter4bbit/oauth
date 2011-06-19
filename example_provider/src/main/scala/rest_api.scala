package info.whiter4bbit.oauth.scalatra.example 

import org.slf4j.LoggerFactory
import org.scalatra._
import java.net.URL
import scalate.ScalateSupport
import info.whiter4bbit.oauth.scalatra._
import info.whiter4bbit.oauth._
import mongodb._

import scalaz._
import Scalaz._
import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import net.liftweb.json._

class ProviderTest extends ScalatraFilter with OAuthProviderFilter with ScalatraHelpers with Scalatraz {
  val logger = LoggerFactory.getLogger(getClass) 

  object storage extends OAuthMongoStorage with MongoDBCollections

  object userService extends UserService with MongoDBCollections 

  implicit val formats = DefaultFormats

  postz("/api/users/add") {     
     for {
         raw <- paramz("user");
         json <- parse(raw).success;
	 login <- field[String]("login")(json);
	 password <- field[String]("password")(json); 
	 created <- userService.create(login, password)
     } yield {
         write(created)
     }
  }

  get("/api/version") {
     (<events-api>
        <version>0.1</version>
     </events-api>).success
  }

  get("/get/some.:format") {
     "format is %s" format params("format")
  }

  get("/index") {
     "Test header is %s".format(header("test"))
  }
}
