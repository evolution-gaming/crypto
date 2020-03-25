package com.evolutiongaming.crypto

import com.typesafe.config.{Config, ConfigFactory}

object DecryptConfig {
  private val EncryptedPasswordsPath = "encryptedPasswords"
  private val AppSecretPath = "application.secret"

  def apply(password: String, config: Config = ConfigFactory.load()): String = {
    if (
      config.hasPath(EncryptedPasswordsPath) &&
        config.getBoolean(EncryptedPasswordsPath)
    ) {
      val secret = config getString AppSecretPath
      Crypto.decryptAES(password, secret.substring(0, 16))
    } else {
      password
    }
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
