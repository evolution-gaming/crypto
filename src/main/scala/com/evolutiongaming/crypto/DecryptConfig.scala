package com.evolutiongaming.crypto

import com.evolutiongaming.crypto.Crypto.DecryptAuthException
import com.typesafe.config.{Config, ConfigFactory}

import scala.util.control.NonFatal

object DecryptConfig {
  private val EncryptedPasswordsPath = "encryptedPasswords"
  private val AppSecretPath = "application.secret"

  def apply(password: String, config: Config = ConfigFactory.load()): String = try {
    if (
      config.hasPath(EncryptedPasswordsPath) &&
        config.getBoolean(EncryptedPasswordsPath)
    ) {
      val secret = config getString AppSecretPath
      Crypto.decryptAES(password, secret)
    } else {
      password
    }
  } catch {
    case th: DecryptAuthException => throw th
    case NonFatal(_) => password
  }

  implicit class DecryptConfigOps(val self: Config) extends AnyVal {

    def decryptString(encrypted: String): String =
      apply(encrypted, self)

    def decryptPath(path: String): String = {
      val encrypted = self.getString(path)
      apply(encrypted, self)
    }
  }
}
