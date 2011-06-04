package info.whiter4bbit.oauth

import scalaz._
import Scalaz._
import OAuthHeader._

class OAuthProvider(val storage: OAuthStandardStorage) extends OAuthValidations with OAuthError {
   import OAuthHeader._
   import OAuthParams._

   def getToken(request: RequestBundle): Validation[Error, TokenRequest] = {
      def checkKey = containsKey(request.oauthHeaders)_ 
      def checkConsumer = validateConsumer(storage)_
      def checkSignature = validateSignature(request.path, request.method, request.oauthHeaders)_
      for {
         consumerKey <- checkKey(oauth_consumer_key);
         signature <- checkKey(oauth_signature);
	 signatureMethod <- checkKey(oauth_signature_method);	 
	 callback <- checkKey(oauth_callback);
	 timestamp <- checkKey(oauth_timestamp) flatMap validateTimestamp;	 
	 nonce <- checkKey(oauth_nonce) flatMap validateNonce(storage)(timestamp)_;
	 consumer <- checkConsumer(consumerKey) flatMap checkSignature(signature)(None)
      } yield { 
	 val pair = storage.generatePair      
	 val request = TokenRequest(consumer, callback, pair._1, pair._2)
         storage.store(request)	 
	 storage.addNonce(timestamp, nonce)
         request
      }      
  }

  def getAccessToken(request: RequestBundle): Validation[Error, AccessTokenRequest] = { 
      def checkKey = containsKey(request.oauthHeaders)_ 
      def checkConsumer = validateConsumer(storage)_
      def checkSignature = validateSignature(request.path, request.method, request.oauthHeaders)_
      for {
         consumerKey <- checkKey(oauth_consumer_key);
         signature <- checkKey(oauth_signature);
	 signatureMethod <- checkKey(oauth_signature_method); 
	 timestamp <- checkKey(oauth_timestamp) flatMap validateTimestamp;	 
	 nonce <- checkKey(oauth_nonce) flatMap validateNonce(storage)(timestamp)_; 
	 token <- checkKey(oauth_token);
	 verifier <- checkKey(oauth_verifier) flatMap validateVerifier(storage)(token)_;	 
	 tokenSecret <- validateToken(storage)(token);	 
	 consumer <- checkConsumer(consumerKey) flatMap checkSignature(signature)(Some(tokenSecret))	 
      } yield { 
	 val pair = storage.generatePair            
	 val request = AccessTokenRequest(consumer, pair._1, pair._2)
         storage.store(request)
	 storage.addNonce(timestamp, nonce)
         request 
      } 
  }

  def getResource(request: RequestBundle): Validation[Error, ResourceRequest] = {
      def checkKey = containsKey(request.oauthHeaders)_ 
      def checkConsumer = validateConsumer(storage)_
      def checkSignature = validateSignature(request.path, request.method, request.oauthHeaders)_
      for {
         consumerKey <- checkKey(oauth_consumer_key);
         signature <- checkKey(oauth_signature);
	 signatureMethod <- checkKey(oauth_signature_method); 
	 timestamp <- checkKey(oauth_timestamp) flatMap validateTimestamp;	 
	 nonce <- checkKey(oauth_nonce) flatMap validateNonce(storage)(timestamp)_; 
	 token <- checkKey(oauth_token);
	 tokenSecret <- validateAccessToken(storage)(token);	 
	 consumer <- checkConsumer(consumerKey) flatMap checkSignature(signature)(Some(tokenSecret))	 
      } yield { 
         storage.addNonce(timestamp, nonce)
         ResourceRequest(consumer, token) 
      } 
  }

  def getVerifier(token: String): Validation[Error, VerificationRequest] = {
     storage.get(token) match {
       case Some(request: TokenRequest) => {
         val verifier = storage.generateVerifier
	 val r = VerificationRequest(request.consumer, request.callback, verifier, request.token, request.tokenSecret)
	 storage.store(r)	 
         r.success
	}
	case _ => error("Invalid request").fail
     }
  }

  def getTokenRequest(token: String) = storage.get(token) match {
      case Some(req: TokenRequest) => Some(req)
      case _ => None
  }
}
