package info.whiter4bbit.oauth.mongodb 

import info.whiter4bbit.oauth._
import com.mongodb.casbah.Imports._
import scala.collection.mutable.{HashMap, MultiMap, Set => MSet}

trait OAuthCollections {
  val requests: MongoCollection
  val nonces: MongoCollection
  val consumers: MongoCollection
}

trait OAuthMongoStorage extends OAuthStandardStorage {
  self: OAuthCollections => 

  override def getConsumer(key: String) = {
     self.consumers.findOne(MongoDBObject("consumerKey" -> key)).map((request) => {
         Consumer(key, request("consumerSecret").toString)
     })
  }

  override def store(request: ConsumerRequest) = { 
     self.requests.insert(request match { 
         case TokenRequest(consumer,  callback, token, tokenSecret) => {
	    MongoDBObject("consumerKey" -> consumer.consumerKey, 
	                   "callback" -> callback,
			   "token" -> token,
			   "tokenSecret" -> tokenSecret,
			   "type" -> "token")
	 }
	 case VerificationRequest(consumer, callback, verifier, token, tokenSecret) => {
	    MongoDBObject("consumerKey" -> consumer.consumerKey,
	                  "callback" -> callback,
			  "verifier" -> verifier,
			  "token" -> token,
			  "tokenSecret" -> tokenSecret,
			  "type" -> "verification")
	 }
	 case AccessTokenRequest(consumer, token, tokenSecret) => {
 	    MongoDBObject("consumerKey" -> consumer.consumerKey,
	                  "token" -> token,
			  "tokenSecret" -> tokenSecret,
			  "type" -> "access")
	 }
	 case ResourceRequest(consumer, token) => {
	    MongoDBObject("consumerKey" -> consumer.consumerKey,
	                  "token" -> token,
			  "type" -> "resource")
	 }
     }) 
  }
  override def get(key: String) = {
     val p = self.requests.find(MongoDBObject("token" -> key))
                 .sort(MongoDBObject("_id" -> -1)).limit(1)
     p.toList.headOption.flatMap( (request) => {     
         val consumer = getConsumer(request("consumerKey").toString).get
         request("type") match {
	     case "token" => Some(TokenRequest(consumer, 
	                            request("callback").toString, 
				    request("token").toString, 
				    request("tokenSecret").toString))
             case "verification" => Some(VerificationRequest(consumer,
	                            request("callback").toString,
				    request("verifier").toString,
				    request("token").toString,
				    request("tokenSecret").toString))
             case "access" => Some(AccessTokenRequest(consumer,
	                            request("token").toString,
				    request("tokenSecret").toString))
             case "resource" => Some(ResourceRequest(consumer,	     
	                               request("token").toString))
             case _ => None				      
	 }
     })
  }

  override def addNonce(timestamp: String, nonce: String) = {
     nonces += MongoDBObject("timestamp" -> timestamp,
                             "nonce" -> nonce)
  }

  override def getNonces(timestamp: String): Set[String] = {
     nonces.find(MongoDBObject("timestamp" -> timestamp)).map( (resp) => {
         resp("nonce").toString
     }).toSet
  }
}
