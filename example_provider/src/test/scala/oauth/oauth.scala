package info.whiter4bbit.oauth

import org.specs2.mutable._
import org.specs2.specification._
import OAuthHeader._
import scalaz._
import Scalaz._

class OAuthSpecs extends Specification {
   import OAuthParams._ 

   val requestTokenH = """oauth_callback=%22oob%22,
                          oauth_consumer_key=%22psE5EHpeEU3m0eQVnWBS1A%22,
			  oauth_nonce=%229291871523625%22,
			  oauth_signature=%22Ywxb98hFJna6oJY7ebgisZ3mKLo%3D%22,
			  oauth_signature_method=%22HMAC-SHA1%22,
			  oauth_timestamp=%221305921358%22"""

   val accessTokenH = """oauth_callback=%22oob%22,
                         oauth_consumer_key=%22psE5EHpeEU3m0eQVnWBS1A%22,
			 oauth_nonce=%2250859099359168%22,
			 oauth_signature=%22iFjmhD920q9SsbM5RRz4iGOFiFA%3D%22,
			 oauth_signature_method=%22HMAC-SHA1%22,
			 oauth_timestamp=%221307055662%22,
			 oauth_token=%22eDUoycNaqQ0lKoU5yYh1EOJ7a%22,
			 oauth_verifier=%22NWM%22"""
   
   val resourceH = """oauth_callback=%22oob%22,
                      oauth_consumer_key=%22psE5EHpeEU3m0eQVnWBS1A%22,
		      oauth_nonce=%2275127541383973%22,
		      oauth_signature=%22z2c6RS0ISAIQvcKNLZJ2JhTSTOE%3D%22,
		      oauth_signature_method=%22HMAC-SHA1%22,
		      oauth_timestamp=%221307096532%22,
		      oauth_token=%22Ki3yjvsvCQmASBaVvvI3nRmaX%22"""

   "OAuth header parameters" should {
      "must be extracted from string into map" in {
         val params = OAuthHeader.parseHeader(requestTokenH)
	 params.size must beEqualTo(6)
	 params must havePair((oauth_callback, "oob"))
	 params must havePair((oauth_consumer_key, "psE5EHpeEU3m0eQVnWBS1A"))
	 params must havePair((oauth_signature_method, "HMAC-SHA1"))
	 params must havePair((oauth_timestamp, "1305921358"))
	 params must havePair((oauth_nonce, "9291871523625"))
	 params must havePair((oauth_signature, "Ywxb98hFJna6oJY7ebgisZ3mKLo%3D"))
      }
   }

   class provider extends Scope {
      val fakeStorage = new StubStorage
      val provider = new OAuthProvider(fakeStorage)
   }

   "OAuth provider" should {
      "token if all parameters is correct" in new provider {      
         val request = RequestBundle("POST", "https://api.twitter.com/oauth/request_token", requestTokenH) 
	 provider.getToken(request) must beLike {  
	    case Success(TokenRequest(_, _, _, _)) => ok 
	 }
      }

      "return error message when parameters in token request are incorrect" in new provider {
         val request = RequestBundle("POST", "/request_token", requestTokenH)
	 provider.getToken(request) must beLike {
	    case Failure(err) => ok 
	 }
      }

      "generate access token if all parameters is correct" in new provider {
         val request = RequestBundle("POST", "http://localhost:8080/oauth/access_token", accessTokenH)	 
	 fakeStorage.store(VerificationRequest(Consumer("psE5EHpeEU3m0eQVnWBS1A", "xVGc4voP28deqLLijcw4NYbw8"), 
	                                       "oob",
	                                       "NWM", 
					       "eDUoycNaqQ0lKoU5yYh1EOJ7a",
					       "xVGc4voP28deqLLijcw4NYbw8"))
	 provider.getAccessToken(request) must beLike {
	    case Success(AccessTokenRequest(_, _, _)) => ok 
	 }
      }

      "return resource request if all parameters is correct" in new provider {         
         fakeStorage.store(AccessTokenRequest(Consumer("psE5EHpeEU3m0eQVnWBS1A", "xVGc4voP28deqLLijcw4NYbw8"),
	                                       "Ki3yjvsvCQmASBaVvvI3nRmaX",
					       "xr7betRHQS28UikX32GtBQLi5"))
         val request = RequestBundle("GET", "http://localhost:8080/resource", resourceH)
	 provider.getResource(request) must beLike {
	    case Success(ResourceRequest(_, _)) => ok
	 }
      }   

      "return error instead of access token when verifier is incorrect" in new provider {
         val request = RequestBundle("POST", "http://localhost:8080/oauth/access_token", accessTokenH)	 
	 fakeStorage.store(VerificationRequest(Consumer("psE5EHpeEU3m0eQVnWBS1A", "xVGc4voP28deqLLijcw4NYbw8"), 
	                                       "oob",
	                                       "NWM1", 
					       "eDUoycNaqQ0lKoU5yYh1EOJ7a",
					       "xVGc4voP28deqLLijcw4NYbw8"))
	 provider.getAccessToken(request) must beLike {
	    case Failure(_) => ok	    
	 }	 
      }

      "return error in case when there already are same nonce for one timestamp" in new provider {
	 val request = RequestBundle("POST", "https://api.twitter.com/oauth/request_token", requestTokenH) 	 
	 provider.getToken(request)
	 provider.getToken(request) must beLike {
	    case Failure(_) => ok
	 }
      }
   }
}
