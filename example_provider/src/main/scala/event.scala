package info.whiter4bbit.oauth.scalatra.example

import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory
import java.util.Date
import scalaz._
import Scalaz._

trait EventMongoCollection {
   val events: MongoCollection
}

case class Event(val id: Option[String], val name: String, val description: String, val startDate: Date, val endDate: Date)

trait EventService { self: EventMongoCollection => 
   val logger = LoggerFactory.getLogger(getClass)
   def add(event: Event, consumerKey: String): Validation[String, Event] = {
      if (self.events.find(MongoDBObject("name" -> event.name)).size == 0) {
          self.events.insert(MongoDBObject("name" -> event.name,
	                                   "description" -> event.description,					   
					   "startDate" -> event.startDate,
					   "consumerKey" -> consumerKey,					   
					   "endDate" -> event.endDate))
          self.events.last.getAs[ObjectId]("_id").map((id) => {
	       Event(Some(id.toString), event.name, event.description, event.startDate, event.endDate).success
	  }).getOrElse("Can't resolve last object id".fail)
      } else {
          ("There are exists event with name %s" format event.name).fail
      }
   }

   def mine(consumerKey: String): Validation[String, List[Event]] = {
      self.events.find(MongoDBObject("consumerKey" -> consumerKey)).map((event) => {
         for {
	    id <- event.getAs[ObjectId]("_id");
	    name <- event.getAs[String]("name");
	    description <- event.getAs[String]("description");
	    startDate <- event.getAs[java.util.Date]("startDate");
	    endDate <- event.getAs[java.util.Date]("endDate")
	 } yield {
	    Event(Some(id.toString), name, description, startDate, endDate)
	 }
      }).toList.filter(_.isDefined).map(_.get).success
   }

   def objectId(eventId: String): Option[ObjectId] = {
      try {
         Some(new ObjectId(eventId))
      } catch {
         case _ => None
      }
   }

   def attend(eventId: String, consumerKey: String): Validation[String, String] = {   
      (for {
         id <- objectId(eventId);
	 modified <- self.events.findAndModify(MongoDBObject("_id" -> id), 
	                                       MongoDBObject("$addToSet" -> MongoDBObject("attendees" -> consumerKey)))
      } yield {
         eventId.success
      }).getOrElse("Error while updating event".fail)
   }

}
