package info.whiter4bbit.oauth

import java.net.URLEncoder
import java.security.SignatureException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Arrays
import java.net._
import java.io._

object HeaderUtils {
  def wrap(source: String) = "\"" + source + "\""
}

object OAuthUtil {

  val HMACSHA1 = "HmacSHA1"

  def hmac(data: String, key: String): String = {
    val secretKey = new SecretKeySpec(key.getBytes, HMACSHA1)
    val mac = Mac.getInstance(HMACSHA1)
    mac.init(secretKey)
    var raw = mac.doFinal(data.getBytes)
    return base64(raw)
  }

  def base64(source: Array[Byte]): String = {
    val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    var base64 = ""
    var fixedSource = source
    var paddingCount = fixedSource.length % 3
    if (paddingCount > 0) {
      paddingCount = 3 - paddingCount
      fixedSource = Arrays.copyOf(fixedSource, fixedSource.length + paddingCount)
    }
    for (i <- List.range(0, fixedSource.length, 3)) {
      val c1: Int = fixedSource(i) & 0xFF
      val c2: Int = fixedSource(i+1) & 0xFF
      val c3: Int = fixedSource(i+2) & 0xFF
      var s = (c1 << 16) | (c2 << 8) | c3
      val b1: Int = (s >> 18)
      s = s ^ (b1 << 18)
      val b2: Int = (s >> 12)
      s = s ^ (b2 << 12)
      val b3: Int = (s >> 6)
      s = s ^ (b3 << 6)
      val b4: Int = s
      base64 += String.valueOf(alpha.charAt(b1)) +
              String.valueOf(alpha.charAt(b2)) +
              String.valueOf(alpha.charAt(b3)) +
              String.valueOf(alpha.charAt(b4))
    }
    return base64.substring(0, base64.length - paddingCount) + "=" * paddingCount
  } 

  def urlEncode(source: String): String = URLEncoder.encode(source, "utf-8")

  def encodeParameters(parameters: Map[String, String], sep: String): String =  
      parameters.toList.sortWith((a,b)=>a._1 < b._1)
            .map(a => urlEncode(a._1) + "=" + urlEncode(a._2) + "")
            .mkString(sep)

  def encode(parameters: Map[String, String], baseURI: String, httpMethod: String): String =
    httpMethod + "&" + urlEncode(baseURI) + "&" + urlEncode(encodeParameters(parameters, "&"))
}


