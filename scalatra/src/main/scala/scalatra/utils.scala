package info.whiter4bbit.oauth.scalatra

import org.scalatra._
import javax.servlet.http.HttpServletRequest

trait ScalatraHelpers { this: ScalatraFilter => 
   def header(name: String): Option[String] = {
      val h = request.getHeader(name)
      if (h == null) {
        None
      } else {
        Some(h)
      }
   } 
}
