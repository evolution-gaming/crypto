package com.evolutiongaming.crypto

import com.typesafe.config.{Config, ConfigFactory}

/**
  * @deprecated use ConfigDecrypter which does not swallow decryption failures
  */
/*
TODO: add @deprecated annotation in the next release
@deprecated("Use ConfigDecrypter which does not swallow decryption failures", since = "3.1.0")
 */
object DecryptConfig {
  def apply(password: String, config: Config = ConfigFactory.load()): String = {
    ConfigDecrypter(config).decryptString(password).getOrElse(password)
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
