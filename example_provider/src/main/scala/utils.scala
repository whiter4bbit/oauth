package info.whiter4bbit.oauth.scalatra.example

import org.slf4j.LoggerFactory
import org.scalatra._
import scalate.ScalateSupport
import scalaz._
import Scalaz._

trait Scalatraz { self: ScalatraFilter =>
    case class Errorz(msg: String)

    def paramz(name: String): Validation[Errorz, String] = {
       params.get(name).map(_.success).getOrElse(Errorz("Missing parameter %s" format name).fail)          
    }

    def postz(route: String)(f: => Validation[Any, Any]) = post(route)({
        f ||| ((e) => halt(400, e.toString))
    })

    def getz(route: String)(f: => Validation[Any, Any]) = get(route)({
        f ||| ((e) => halt(400, e.toString))
    })
}
