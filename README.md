# Crypto [![Build Status](https://travis-ci.org/evolution-gaming/crypto.svg)](https://travis-ci.org/evolution-gaming/crypto) [![Coverage Status](https://coveralls.io/repos/evolution-gaming/crypto/badge.svg)](https://coveralls.io/r/evolution-gaming/crypto) [ ![version](https://api.bintray.com/packages/evolutiongaming/maven/crypto/images/download.svg) ](https://bintray.com/evolutiongaming/maven/crypto/_latestVersion)

A library that facilitates decrypting passwords using an application secret stored in a [Typesafe Config](https://github.com/typesafehub/config) file.

When combined with an approach where the config file used is provided upon deployment and dynamically selected based on an environment variable it helps to avoid storing sensitive passwords in source control, while still allowing development configurations to be stored in source control. 

Partially based on code from the [Play! framework](https://www.playframework.com/).

How to use
===========

Add the following resolver

    resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

Add the library to your dependencies list

    libraryDependencies += "com.evolutiongaming" %% "crypto" % "1.2-SNAPSHOT"

Create an application config file `environments/default.conf`:

```
encryptedPasswords = true
application {
  secret = "abcdefghijklmnop" // only for example purposes, you should use a strong randomly generated secret
}

password = "2-DpBV9t/8a19P5o0fohf//Lpup8DF" // use com.evolutiongaming.crypto.Encrypt app to encrypt
```

Use the library as follows

```scala
import com.evolutiongaming.crypto.DecryptConfig
import com.typesafe.config.ConfigFactory

val environmentKey = "ENVIRONMENT"
val environment = System.getenv(environmentKey).orElse(sys.props.get(environmentKey).getOrElse("default")) // select the environment to use
val config = ConfigFactory.parseResourcesAnySyntax(s"environments/$environment") // load the config file

val password = config.getString("password") // the encrypted password to decrypt
val decrypted = DecryptConfig(password, config) // decrypting the password 

// now you can use the decrypted value to authenticate to external services
 ```

Examples
========

For more examples you can review [DecryptConfigSpec](https://github.com/evolution-gaming/crypto/tree/master/src/test/scala/com/evolutiongaming/crypto/DecryptConfigSpec.scala).
