package info.whiter4bbit.oauth.scalatra

import info.whiter4bbit.oauth._
import scala.util.DynamicVariable
import OAuthHeader._

import org.scalatra._
import java.net.URL
import scalaz._
import Scalaz._

trait OAuthProviderFilter extends ScalatraFilter with Scalatraz { 
   def storage: OAuthStandardStorage 

   private val provider = new OAuthProvider(storage) 

   val requestTokenPath = "/oauth/request_token"
   val accessTokenPath = "/oauth/access_token"
   val authenticationPath = "/oauth/authenticate"

   private val _oauthRequest = new DynamicVariable[ConsumerRequest](null) 
   def oauthRequest = _oauthRequest value
   
   private def getBundle(method: String = "POST") = headerz("Authorization").map(RequestBundle(method, request.getRequestURL.toString, _))

   postz(requestTokenPath) { 
      for { 
         bundle <- getBundle();
         request <- provider.getToken(bundle)
      } yield {
         "oauth_token=%s&oauth_token_secret=%s".format(request.token, request.tokenSecret)
      }
   } 

   postz(accessTokenPath) {
      for {
         bundle <- getBundle();
	 request <- provider.getAccessToken(bundle)
      } yield {
         "oauth_token=%s&oauth_token_secret=%s".format(request.token, request.tokenSecret)
      }
   }

   postz("/authenticate") {
      for {
         token <- paramz("token");
	 verifier <- provider.getVerifier(token)
      } yield {
         if (verifier.callback == "oob") {
	    verifier.verifier + ""
	 } else {
	    redirect(verifier.callback + "&oauth_token=%s&oauth_verifier=%s".format(verifier.token, verifier.verifier))
	 }
      }
   }

   def protectedAction(method: String, f: => Validation[Any, Any]) = {
      getBundle(method).flatMap((bundle) => {
         provider.getResource(bundle).flatMap((request) => {
	    _oauthRequest.withValue(request)(f)
	 })
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

   def protectedGet(route: String)(f: => Validation[Any, Any]) = getz(route)(protectedAction("GET", f))   
   def protectedPost(route: String)(f: => Validation[Any, Any]) = postz(route)(protectedAction("POST", f))
   def protectedPut(route: String)(f: => Validation[Any, Any]) = putz(route)(protectedAction("PUT", f))
}
