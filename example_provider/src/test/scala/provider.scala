package info.whiter4bbit.oauth.scalatra.example

import org.specs._
import org.scalatra.test.specs._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{write => jwrite}
import java.util.Date

class RestAPISpec extends EventsAPISpec {

   addFilter(classOf[MockProviderTest], "/*")   

   "events rest public api" should {
      doBefore {
         cleanCollections
      }
      "create user if all parameters is correct" in {         
         val json = compact(render(Map("login" -> "pasha", 
	                               "password" -> "12345")))				       
         post("/api/users/add", params = ("user", json)) {
	    status must ==(200)
	    val user = parse(body).extract[User]
	    user.login must ==("pasha")
	    user.password must ==("12345")	    
	    user.consumerKey must notBeNull
	    user.consumerSecret must notBeNull
	 }
      }
      "do not create user with login, that already exists" in {
         val json = compact(render(Map("login" -> "pasha", 
	                               "password" -> "54321")))				       
         post("/api/users/add", params = ("user", json)) {} 				       
         post("/api/users/add", params = ("user", json)) {
	    status must ==(400)
	 } 
      }
   }

   "events rest protected api" should {   
      var user: User = null
      doFirst {
         cleanCollections	          
	 val json = compact(render(Map("login" -> "pasha", 	 
	                               "password" -> "54321")))				       
         post("/api/users/add", params = ("user", json)) {
	    status must ==(200)
	    user = parse(body).extract[User]
	 }
      }
      "present user information" in {
         val token = getOAuthToken(user)
	 val loaded = oauthGet("/api/user/info", user, Some(token)) {
	    status must ==(200)
	    parse(body).extract[User]
	 }
	 loaded must ==(User(user.login, user.password, user.consumerKey, user.consumerSecret))
      }
      "add event" in {
         val token = getOAuthToken(user)
	 val json = jwrite(Event(None, "event1", "event description", new Date, new Date))
	 val event = oauthPost("/api/events/add", user, Some(token), params = List(("event", json))) {
	    status must ==(200)
	    parse(body).extract[Event]
	 }
	 event.id must beSome[String]
      }
      "do not allow to add event with same name twice" in {         
         val token = getOAuthToken(user)      
	 val json = jwrite(Event(None, "event2", "event description", new Date, new Date))
	 oauthPost("/api/events/add", user, Some(token), params = List(("event", json))) {
	    status must ==(200)
	 }
         oauthPost("/api/events/add", user, Some(token), params = List(("event", json))) {
	    status must ==(400)
	 }
      }
   }
}
