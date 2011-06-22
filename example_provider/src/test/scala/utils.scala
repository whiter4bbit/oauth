package info.whiter4bbit.oauth.scalatra.example

import scala.xml._
import org.specs._
import org.scalatra.test.specs._
import com.mongodb.casbah.Imports._
import info.whiter4bbit.oauth.mongodb._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{write => jwrite}
import info.whiter4bbit.chttp.oauth._
import info.whiter4bbit.chttp.oauth.util._
import java.util.Date

trait TestDBCollections extends OAuthCollections with UserMongoCollection with EventMongoCollection {
  override val requests = MongoConnection()("test_oauth")("requests")
  override val nonces = MongoConnection()("test_oauth")("nonces")
  override val consumers = MongoConnection()("test_oauth")("users")
  override val users = MongoConnection()("test_oauth")("users")
  override val events = MongoConnection()("test_oauth")("events")
}

class MockProviderTest extends ProviderTest {
  override def storage = new java.lang.Object with OAuthMongoStorage with TestDBCollections  
  override val userService = new java.lang.Object with UserService with TestDBCollections   
  override val eventService = new java.lang.Object with EventService with TestDBCollections
}

case class User(val login: String, val password: String, val consumerKey: String, val consumerSecret: String)
case class Event(val id: Option[String], val name: String, val description: String, val startDate: Date, val endDate: Date)

trait EventsAPISpec extends ScalatraSpecification {    
   implicit val formats = DefaultFormats
   val localURL = "http://127.0.0.1"
   val collections = new TestDBCollections{}

   def cleanCollections = {
      collections.events.drop
      collections.users.drop	
   }

   def cleanEvents = {
      collections.events.drop
   }

   private def oauth[A](method: String)(uri: String, user: User, token: Option[Token] = None, pin: Option[String] = None, params: List[(String, String)] = List())(f: => A): A = {
       val header = new OAuthHeader()	 
              .setConsumerKey(user.consumerKey)
              .setConsumerSecret(user.consumerSecret) 
	      .setToken(token.map(_.token))
	      .setTokenSecret(token.map(_.tokenSecret))
	      .setPin(pin)
              .setURL(localURL + uri)
              .setMethod(method).build
       var captured: A = null.asInstanceOf[A] 
       def capture(f: => A) { captured = f }
       method match {
          case "POST" =>  post(uri, params, headers = Map("Authorization" -> header))(capture(f))
	  case "GET" => get(uri, params, headers = Map("Authorization" -> header))(capture(f))
	  case _ => throw new Error("Unknown method: %s" format method)
       }
       captured       
   }

   def oauthPost[A](uri: String, user: User, token: Option[Token] = None, pin: Option[String] = None, params: List[(String, String)] = List())(f: => A): A = oauth[A]("POST")(uri, user, token, pin, params)(f) 
   def oauthGet[A](uri: String, user: User, token: Option[Token] = None, pin: Option[String] = None, params: List[(String, String)] = List())(f: => A): A = oauth[A]("GET")(uri, user, token, pin, params)(f)

   def getOAuthToken(user: User) = {	 
      val token = oauthPost("/oauth/request_token", user) {	   	    
        status must ==(200)
        val parameters = OAuthUtil.bodyParameters(body)
        Token(parameters("oauth_token"), parameters("oauth_token_secret"))
      }	 
      var pin: String = null
      post("/authenticate", params = List(("token", token.token))) {
        status must ==(200)
        pin = body
      }
      oauthPost("/oauth/access_token", user, Some(token), Some(pin.toString)) {
	 status must ==(200)
	 val parameters = OAuthUtil.bodyParameters(body)
	 Token(parameters("oauth_token"), parameters("oauth_token_secret"))
      }
   }

}
