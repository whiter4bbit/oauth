package info.whiter4bbit.oauth

import scala.collection.mutable.{HashMap, MultiMap, Set => MSet}

class StubStorage extends OAuthStandardStorage {   
  var storage: Map[String, ConsumerRequest] = Map()  
  val nonceStorage: MultiMap[String, String] = new HashMap[String, MSet[String]]() with MultiMap[String, String]
  override def generateVerifier = generateToken(4)
  override def getConsumer(consumerKey: String) = {
     Map("psE5EHpeEU3m0eQVnWBS1A" -> "ZE9Tv1h7bvO1Kjt5uuy77SzJIzJzoA8VcsbWHQkv4")
           .get(consumerKey).map(Consumer(consumerKey, _))
  }
  override def store(request: ConsumerRequest) = {
     storage += (request.key -> request)
  }
  override def get(key: String) = {
     storage.get(key)
  }
  override def addNonce(timestamp: String, nonce: String) = {
     nonceStorage.addBinding(timestamp, nonce)
  }
  override def getNonces(timestamp: String): Set[String] = {
     nonceStorage.get(timestamp).map((nonces) => nonces.toSet)
            .getOrElse(Set[String]())
  }
}

