package info.whiter4bbit.oauth

import java.net.URLEncoder
import scalaz._
import Scalaz._

case class AuthError(val msg: String)

trait Errors {
   type Error
   def error(descr: String): Error
}

trait OAuthError extends Errors {
   type Error = AuthError
   def error(descr: String) = AuthError(descr)   
}

trait OAuthValidations extends Errors {
   import OAuthParams._

   def containsKey[A, B](map: Map[A, B])(key: A): Validation[Error, B] = {
       map.get(key).map(_.success[Error])
            .getOrElse(error("Header contains no key %s".format(key)).fail)	    
   }

   def validateConsumer(storage: ConsumerStorage)(consumerKey: String): Validation[Error, Consumer] = {
       val consumer = storage.getConsumer(consumerKey).map(_.success)
       consumer.getOrElse(error("Consumer '%s' not found").fail)
   }

   def validateTimestamp(timestamp: String): Validation[Error, String] = {
       try {
         val current = System.currentTimeMillis() / 1000
	 if (current < timestamp.toLong) {
	   error("Timestamp is incorrect").fail
	 } else {
	   timestamp.success
	 }
       } catch {
         case _ => error("Timestamp is incorrect").fail
       }
   }
   
   def validateNonce(storage: NonceStorage)(timestamp: String)(nonce: String): Validation[Error, String] = {
       if (storage.getNonces(timestamp).contains(nonce)) {
         error("Nonce is invalid").fail
       } else {
         nonce.success
       }
   }

   def validateToken(storage: TokenStorage)(token: String): Validation[Error, String] = {   
       storage.get(token) match {
          case Some(request: VerificationRequest) => {
	     request.tokenSecret.success
	  }
	  case _ => {	      
	     error("Invalid token: %s".format(token)).fail
	  }
       }
   }

   def validateAccessToken(storage: TokenStorage)(token: String): Validation[Error, String] = {
       storage.get(token) match {
          case Some(request: AccessTokenRequest) => {
	      request.tokenSecret.success
	  }
	  case _ => {	      
	      error("Invalid token: %s".format(token)).fail
	  }
       }
   }

   def validateVerifier(storage: TokenStorage)(token: String)(verifier: String): Validation[Error, String] = {
       lazy val invalidVerifier = error("Invalid verifier %s".format(verifier)).fail
       storage.get(token) match {
          case Some(request: VerificationRequest) => {
	     if (request.verifier == verifier) {
	        verifier.success
	     } else {
	        invalidVerifier 
	     }
	  }	  
	  case _ => {
	     invalidVerifier
     	  }
       }
   }

   def validateSignature(url: String, method: String, headers: Map[Param, String])(signature: String)(tokenSecret: Option[String])(consumer: Consumer): Validation[Error, Consumer] = {   
       val params = headers.map( (e) => (e._1.name, e._2)).filter( (e) => e._1 != "oauth_signature")
       val data = OAuthUtil.encode(params, url, method)
       val secretKey = consumer.consumerSecret + "&" + tokenSecret.getOrElse("")
       val required = URLEncoder.encode(OAuthUtil.hmac(data, secretKey), "UTF-8")
       if (required == signature) {
          consumer.success
       } else {
          error("OAuth signature is incorrect: '%s', was '%s'".format(required, signature)).fail
       }       
   }
}
