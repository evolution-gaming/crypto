package com.evolutiongaming.crypto

import scala.util.Random

object Encrypt extends App {
  if (args.length < 1 || args.length > 2) {
    println("Expected 1 or 2 arguments - value and optional privateKey")
  } else {
    val value = args(0)
    val privateKey = args.lift(1) getOrElse {
      val generated = Random.alphanumeric.take(16).mkString
      println(s"privateKey wasn't provided and therefore a new privateKey was automatically generated: $generated")
      generated
    }

    require(privateKey.length == 16, s"Expected privateKey to have 16 characters, got ${privateKey.length} characters instead")

    val encrypted = Crypto.encryptAES(value, privateKey)
    println(encrypted)
  }
}
