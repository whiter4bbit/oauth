package info.whiter4bbit.oauth.scalatra

import info.whiter4bbit.oauth._
import org.slf4j.LoggerFactory
import scala.util.DynamicVariable
import OAuthHeader._

import org.scalatra._
import java.net.URL

trait OAuthProviderFilter extends ScalatraFilter with ScalatraHelpers { 
   private val logger = LoggerFactory.getLogger(getClass.getName)    
   def storage: OAuthStandardStorage 

   val provider = new OAuthProvider(storage) 

   val requestTokenPath = "/oauth/request_token"
   val accessTokenPath = "/oauth/access_token"
   val authenticationPath = "/oauth/authenticate"

   val _oauthRequest = new DynamicVariable[ConsumerRequest](null) 
   def oauthRequest = _oauthRequest value
   
   post(requestTokenPath) { 
      header("Authorization").map((header) => {
           val url = request.getRequestURL.toString
	   val bundle = RequestBundle("POST", url, header)	     
	   provider.getToken(bundle).fold((error) => {
	      halt(401)
	   }, (request) => {
	      val token = (request.token, request.tokenSecret)
	      "oauth_token=%s&oauth_token_secret=%s".format(token._1, token._2)
	   })
      }).getOrElse({
         halt(400)
      })
   }   

   post(accessTokenPath) {
      header("Authorization").map((header) => {
           val url = request.getRequestURL.toString
	   val bundle = RequestBundle("POST", url, header)	     
	   provider.getAccessToken(bundle).fold((error) => {
	      halt(401)
	   }, (request) => {
	      "oauth_token=%s&oauth_token_secret=%s".format(request.token, request.tokenSecret)
	   })
      }).getOrElse({
         halt(400)
      })
   }

   post("/authenticate") { 
      params.get("token").map( (token) => {
         provider.getVerifier(token).fold((e) => {
	    halt(401)
	 }, (verifier) => {
	    if (verifier.callback == "oob") {
	       <p>verifier: {{verifier.verifier}}</p>
	    } else {
	       redirect(verifier.callback + "&oauth_token=%s&oauth_verifier=%s".format(verifier.token, verifier.verifier))
	    }
	 })
      }).getOrElse({
        halt(400)	
      })
   }   

   get(authenticationPath) { 
      val res = for (token <- params.get("oauth_token");
         message <- provider.getTokenRequest(token)) yield { 
         <form action="/authenticate" method="POST"> 
             <input type="hidden" name="token" value={{token}}/>
             User name: <input type="text" name="username"/> <br/>
             Password: <input type="password" name="password"/> <br/>
             <input type="submit" value="Login"/> 
          </form>
      }
      res.getOrElse(halt(400))
   }

   def protectedAction(f: => Any) =  {
      header("Authorization").map((header) => {
         val url = request.getRequestURL.toString
	 val bundle = RequestBundle("GET", url, header)
	 provider.getResource(bundle).fold((error) => {
	     halt(401)
	 }, (request) => {
	     _oauthRequest.withValue(request)(f)
	 })
      }).getOrElse({
         halt(400) 
      })
   }

   def protectedGet(route: String)(f: => Any) = get(route)(protectedAction(f))
   def protectedPost(route: String)(f: => Any) = post(route)(protectedAction(f))
}
