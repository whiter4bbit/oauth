package info.whiter4bbit.oauth

import scala.collection.mutable.{Map => MMap}
import org.apache.commons.lang.StringUtils.{removeStart, removeEnd}
import java.net.URLEncoder

case class Token(val token: String, val tokenSecret: String)
case class Consumer(val consumerKey: String, val consumerSecret: String)
case class RequestBundle(val method: String, val path: String, val oauthHeaders: Map[OAuthParams.Param, String])

object OAuthParams {
  sealed abstract class Param(val name: String)
  case object realm extends Param("realm")
  case object oauth_consumer_key extends Param("oauth_consumer_key")
  case object oauth_callback extends Param("oauth_callback")
  case object oauth_token extends Param("oauth_token")  
  case object oauth_signature_method extends Param("oauth_signature_method")
  case object oauth_timestamp extends Param("oauth_timestamp")
  case object oauth_nonce extends Param("oauth_nonce")
  case object oauth_signature extends Param("oauth_signature")
  case object oauth_verifier extends Param("oauth_verifier")

  val values = Set(realm, oauth_consumer_key, oauth_token, oauth_signature_method,oauth_timestamp,
    oauth_nonce,oauth_signature, oauth_callback, oauth_verifier)
  
  def forName(name: String) = values.filter(_.name == name).headOption  
}

sealed abstract class ConsumerRequest(val consumer: Consumer, val key: String)
final case class TokenRequest(override val consumer: Consumer, val callback: String, val token: String, val tokenSecret: String)
   extends ConsumerRequest(consumer, key = token)
final case class VerificationRequest(override val consumer: Consumer, val callback: String, val verifier: String, val token: String, val tokenSecret: String)
   extends ConsumerRequest(consumer, key = token)
final case class AccessTokenRequest(override val consumer: Consumer, val token: String, val tokenSecret: String)
   extends ConsumerRequest(consumer, key = token)
final case class ResourceRequest(override val consumer: Consumer, val token: String) 
   extends ConsumerRequest(consumer, key = token)   

object OAuthHeader {
   implicit def stringToHeader(header: String) = parseHeader(header)

   def remove(s: String, r: String) = {
      removeEnd(removeStart(s, r), r)
   }   
   def parseHeader(header: String): Map[OAuthParams.Param, String] = {
      val oauthParams: MMap[OAuthParams.Param, String] = MMap()
      header.split(",").toList.foreach( (p) => {
        val h = p.split("=")
	if (h.length == 2) {
           val key = h(0).replaceFirst("OAuth", "").trim 
	   val value = remove(remove(h(1).trim(), "\""),"%22")
	   OAuthParams.forName(key).map(key => {
	      oauthParams += ((key, value))
	   })
	}
      })
      oauthParams.toMap
   }   
}

