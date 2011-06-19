package info.whiter4bbit.oauth.scalatra.example 

import com.mongodb.casbah.Imports._
import info.whiter4bbit.oauth.TokenGenerator
import org.slf4j.LoggerFactory
import scala.util.parsing.json._
import scalaz._
import Scalaz._

trait UserMongoCollection {
   val users: MongoCollection
}

case class User(val login: String, val password: String, val consumerKey: String, val consumerSecret: String)

trait UserService extends TokenGenerator { self: UserMongoCollection with TokenGenerator => 
   
   val logger = LoggerFactory.getLogger(getClass)

   def generateKey = self.generateToken(32)

   def create(login: String, password: String): Validation[String, User] = {
       val user = User(login, password, generateKey, generateKey)
       if (self.users.find(MongoDBObject("login" -> login)).size == 0) {       
          self.users.insert(MongoDBObject("login" -> login,
                                          "password" -> password,
				          "consumerKey" -> user.consumerKey,
					  "consumerSecret" -> user.consumerSecret))
	  logger.info("User %s has been created" format login)					  
          user.success
       } else {
          logger.info("User %s already exists" format login)
	  "User %s already exists".format(login).fail
       }
   }
}

