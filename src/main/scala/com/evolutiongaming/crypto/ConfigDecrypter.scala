package com.evolutiongaming.crypto

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

/**
  * Provides decryption of passwords encrypted using [[Crypto.encryptAES]] for Typesafe Config.
  *
  * To use extension methods for [[Config]], import from [[ConfigDecrypter.syntax]].
  *
  * Configuration parameters:
  * - `encryptedPasswords` - if not set or `false` password values are treated as plain-text,
  * if `true` - as cipher-text
  * - `application.secret` - private key string to use in decryption
  */
trait ConfigDecrypter {
  /**
    * If set password values are expected to be encrypted, plain-text otherwise
    */
  def encryptedPasswordsEnabled: Boolean
  /**
    * Decrypts a password value obtaining the plain text
    */
  def decryptString(encryptedPassword: String): Try[String]
  /**
    * Decrypts a password value obtaining the plain text - throws exceptions if the plain text recovery is not
    * possible
    */
  def decryptStringUnsafe(encryptedPassword: String): String
  /**
    * Decrypts a password value at a config path obtaining the plain text
    */
  def decryptPath(configPath: String): Try[String]
  /**
    * Decrypts a password value at a config path obtaining the plain text - throws exceptions if
    * the plain text recovery is not possible
    */
  def decryptPathUnsafe(configPath: String): String
}

object ConfigDecrypter {
  private val EncryptedPasswordsPath = "encryptedPasswords"
  private val AppSecretPath = "application.secret"

  /**
    * Provides [[ConfigDecrypter]] functionality as extension methods for [[Config]]
    */
  object syntax {
    @inline implicit def config2ConfigDecrypter(config: Config): ConfigDecrypter = ConfigDecrypter(config)
  }

  /**
    * Creates [[ConfigDecrypter]] working on the provided config
    */
  def apply(config: Config = ConfigFactory.load()): ConfigDecrypter = new ConfigDecrypter {
    override def encryptedPasswordsEnabled: Boolean =
      config.hasPath(EncryptedPasswordsPath) && config.getBoolean(EncryptedPasswordsPath)

    override def decryptStringUnsafe(encryptedPassword: String): String =
      if (encryptedPasswordsEnabled) {
        val secret = config.getString(AppSecretPath)
        Crypto.decryptAES(encryptedPassword, secret)
      } else {
        encryptedPassword
      }

    override def decryptString(encryptedPassword: String): Try[String] = Try {
      decryptStringUnsafe(encryptedPassword)
    }

    override def decryptPath(configPath: String): Try[String] = Try {
      decryptPathUnsafe(configPath)
    }

    override def decryptPathUnsafe(configPath: String): String =
      decryptStringUnsafe(config.getString(configPath))
  }
}
