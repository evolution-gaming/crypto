# Crypto 
[![Build Status](https://github.com/evolution-gaming/crypto/workflows/CI/badge.svg)](https://github.com/evolution-gaming/crypto/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/evolution-gaming/crypto/badge.svg?branch=master)](https://coveralls.io/github/evolution-gaming/crypto?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/1678ea6c4ac94c10a5cd9c1fc4f51fd4)](https://www.codacy.com/gh/evolution-gaming/crypto/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=evolution-gaming/crypto&amp;utm_campaign=Badge_Grade)
[![Version](https://img.shields.io/badge/version-click-blue)](https://evolution.jfrog.io/artifactory/api/search/latestVersion?g=com.evolutiongaming&a=crypto_2.13&repos=public)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://opensource.org/licenses/Apache-2.0)

A library that facilitates decrypting passwords using an application secret stored in a [Typesafe Config](https://github.com/typesafehub/config) file.

When combined with an approach where the config file used is provided upon deployment and dynamically selected based on an environment variable it helps to avoid storing sensitive passwords in source control, while still allowing development configurations to be stored in source control. 

Partially based on code from the [Play! framework](https://www.playframework.com/).

How to use
===========

Add the library to your dependencies list

```scala
addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

libraryDependencies += "com.evolutiongaming" %% "crypto" % "2.1.0"
```

Create an application config file `environments/default.conf`:

```hocon
encryptedPasswords = true
application {
  secret = "abcdefghijklmnop" // only for example purposes, you should use a strong randomly generated secret
}

password = "3-DG4i9kr/lboBjhjgwMsT/2f1Jc6vI4O9VucM+ucM7TDi9Q==" // use com.evolutiongaming.crypto.Encrypt app to encrypt
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
