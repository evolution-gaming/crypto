package com.evolutiongaming.crypto

object Decrypt extends App {
  if (args.length != 2) {
    println("Expected 2 arguments - value and privateKey")
  } else {
    val value = args(0)
    val privateKey = args(1)

    val encrypted = Crypto.decryptAES(value, privateKey)
    println(encrypted)
  }
}
