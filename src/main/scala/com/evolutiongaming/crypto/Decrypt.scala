package com.evolutiongaming.crypto

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.NonFatal

object DecryptConfig {
  def apply(password: String, config: Config = ConfigFactory.load()): String = try {
    if (config getBoolean "encryptedPasswords") {
      val secret = config getString "application.secret"
      Crypto.decryptAES(password, secret.substring(0, 16))
    } else {
      password
    }
  } catch {
    case NonFatal(e) => password
  }
}
