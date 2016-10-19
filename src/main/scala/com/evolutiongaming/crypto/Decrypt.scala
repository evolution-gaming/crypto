package com.evolutiongaming.crypto

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.NonFatal

object Decrypt {
  def apply(password: String, config: Config = ConfigFactory.load()): String = try {
    val system = config getConfig "evolutiongaming.system"
    if (system getBoolean "encryptedPasswords") {
      val secret = system getString "application.secret"
      Crypto.decryptAES(password, secret.substring(0, 16))
    } else {
      password
    }
  } catch {
    case NonFatal(e) => password
  }
}
