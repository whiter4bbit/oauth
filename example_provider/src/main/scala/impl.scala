package info.whiter4bbit.oauth.scalatra.example

import com.mongodb.casbah.Imports._
import info.whiter4bbit.oauth.mongodb._

trait MongoDBCollections extends OAuthCollections with UserMongoCollection with EventMongoCollection {
  override val requests = MongoConnection()("oauth")("requests")
  override val nonces = MongoConnection()("oauth")("nonces")
  override val consumers = MongoConnection()("oauth")("users")
  override val users = MongoConnection()("oauth")("users")
  override val events = MongoConnection()("oauth")("events")
}

trait Services {
  val userService: UserService
  val eventService: EventService
}

trait ServicesImpl extends Services {  
  override val userService: UserService = new java.lang.Object with UserService with MongoDBCollections
  override val eventService: EventService = new java.lang.Object with EventService with MongoDBCollections
}
