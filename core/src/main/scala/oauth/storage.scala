package info.whiter4bbit.oauth

trait ConsumerStorage {
   def getConsumer(key: String): Option[Consumer] 
}

trait TokenStorage { this: TokenGenerator =>
   def generateVerifier: String = generateToken(4)
   def store(request: ConsumerRequest)
   def get(requestKey: String): Option[ConsumerRequest]   
}

trait NonceStorage {
   def addNonce(timestamp: String, nonce: String)
   def getNonces(timestamp: String): Set[String]
}

trait TokenGenerator {
   val tokenLength = 25 
   val alpha = List(('a', 'z'), ('A', 'Z'), ('0', '9'))
       .map((i) => List.range(i._1, i._2)).flatten.map(_.asInstanceOf[Char]) 
   def generatePair = (generateToken(), generateToken())
   def generateToken(n: Int = tokenLength) = { 
      val random = new java.util.Random()
      def rand(range: Int) = random.nextInt(range)
      List.range(0, n).map((i) => alpha(rand(alpha.length))).mkString      
   }
}

trait OAuthStandardStorage extends TokenStorage with ConsumerStorage with NonceStorage with TokenGenerator

