package info.whiter4bbit.oauth.scalatra.example

import com.mongodb.casbah.Imports._
import info.whiter4bbit.oauth.mongodb._

trait MongoDBCollections extends OAuthCollections with UserMongoCollection {
  override val requests = MongoConnection()("oauth")("requests")
  override val nonces = MongoConnection()("oauth")("nonces")
  override val consumers = MongoConnection()("oauth")("users")
  override val users = MongoConnection()("oauth")("users")
}


