package info.whiter4bbit.oauth.scalatra.example 

import scalaz._
import Scalaz._

import org.slf4j.LoggerFactory
import org.scalatra._
import java.net.URL
import scalate.ScalateSupport
import info.whiter4bbit.oauth.scalatra._
import info.whiter4bbit.oauth._
import mongodb._

import net.liftweb.json.scalaz.JsonScalaz._
import net.liftweb.json.Serialization.write
import net.liftweb.json._

class ProviderTest extends ScalatraFilter with OAuthProviderFilter with Services with ServicesImpl with Scalatraz {
  val logger = LoggerFactory.getLogger(getClass) 

  def storage: OAuthMongoStorage = new java.lang.Object with OAuthMongoStorage with MongoDBCollections  

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

  protectedGet("/api/user/info") {
     userService.find(oauthRequest.consumer.consumerKey).map((user) => {
        write(user)
     })
  }   

  protectedPost("/api/events/add") {  
     for {
        raw <- paramz("event");
	proto <- Function.uncurried(Event.curried(None)).success;
	event <- proto.applyJSON(field("name"), field("description"), field("startDate"), field("endDate"))(parse(raw));
	inserted <- eventService.add(event, oauthRequest.consumer.consumerKey)	
     } yield {
        write(inserted)
     }
  }

  protectedPost("/api/events/attend") {
     for {
        raw <- paramz("event");
	id <- field[String]("eventId")(parse(raw));
	updated <- eventService.attend(id, oauthRequest.consumer.consumerKey)
     } yield {
        updated 
     }
  }

  protectedGet("/api/events/mine") {  
     for {
        events <- eventService.mine(oauthRequest.consumer.consumerKey)
     } yield {
        write(events)
     }
  }

}
