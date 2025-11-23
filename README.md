<div style="text-align: center; margin-top: 2em; margin-bottom: 2em;">
    <picture>
        <source media="(prefers-color-scheme: dark)"
            srcset="https://github.com/ptitjes/kzmq/raw/main/docs/images/kzmq-logo-dark-background.svg">
        <img alt="Kzmq logo" width="500" style="max-width:100%;"
            src="https://github.com/ptitjes/kzmq/raw/main/docs/images/kzmq-logo-light-background.svg">
    </picture>
</div>

![Build](https://github.com/ptitjes/kzmq/actions/workflows/main.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ptitjes/kzmq-core)](https://mvnrepository.com/artifact/io.github.ptitjes)
[![Kotlin](https://img.shields.io/badge/kotlin-1.7.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

> Note that the library is experimental, and the API is subject to change.
> See [Implementation status](#implementation-status) for more information.

Kzmq is a Kotlin multi-platform ZeroMQ library. It supports four backend engines:
- CIO, a pure Kotlin coroutine-based implementation (using `ktor-network`),
- [JeroMQ](https://github.com/zeromq/jeromq), a pure Java implementation of ZeroMQ,
- [ZeroMQjs](https://github.com/zeromq/zeromq.js), a Node.JS addon implementation of ZeroMQ,
- [Libzmq](https://github.com/zeromq/libzmq), the main native implementation of ZeroMQ.

# Getting started

## Simple example

Here is how you might implement a server that prints the messages it receives
and responds to them with "Hello, world!":

```kotlin
import kotlinx.coroutines.*
import org.zeromq.*

suspend fun main() = coroutineScope {
  // Create a ZeroMQ context (with an auto-discovered engine)
  Context().use { context ->
    // Create a "Reply" socket to talk to clients
    context.createReply().apply {
      bind("tcp://localhost:5555")
    }.use { socket ->
      while (isActive) {
        // Suspend until a message is received
        val content = socket.receive {
            readFrame { readString() }
        }

        // Print the message
        println(content)

        // Send a response back
        socket.send {
            writeFrame("Hello, $content!")
        }
      }
    }
  }
}
```

## More examples

For now, you can look at the
[test suite](https://github.com/ptitjes/kzmq/blob/master/kzmq-tests/src/commonTest/kotlin).

## Documentation

You can generate the Kdoc documentation by running the `dokkaHtmlMultiModule` gradle task.

# Engines

Kzmq supports four backend engines:
- CIO, a pure Kotlin coroutine-based implementation (using `ktor-network`),
- [JeroMQ](https://github.com/zeromq/jeromq), a pure Java implementation of ZeroMQ,
- [ZeroMQjs](https://github.com/zeromq/zeromq.js), a Node.JS addon implementation of ZeroMQ,
- [Libzmq](https://github.com/zeromq/libzmq), the main native implementation of ZeroMQ.

The tables below might help you choose an engine depending on your needs.

> We support auto-discovery of engines.
> See [Using in your projects](#using-in-your-projects) for more information.

## Targets

| Target/Engine | CIO | JeroMQ | ZeroMQjs  | Libzmq |
|---------------|:---:|:------:|:---------:|:------:|
| JVM           |  Y  |   Y    |           |        |
| Node.JS       |     |        |     Y     |        |
| Native        |  Y  |        |           |   Y    |

## Transports

| Transport/Engine |  CIO  | JeroMQ | ZeroMQjs  | Libzmq |
|------------------|:-----:|:------:|:---------:|:------:|
| `tcp://`         |   Y   |   Y    |     Y     |   Y    |
| `ipc://`         | Y (♭) |        |     Y     |   Y    |
| `inproc://`      | Y (♯) | Y (♯)  |   Y (♯)   | Y (♯)  |

(♭) Supported on Native. Requires JVM >= 16 for the JVM target.

(♯) Each engine only supports the `inproc` transport with itself.

## Protocol Version

| Protocol/Engine |  CIO  | JeroMQ | ZeroMQjs  | Libzmq |
|-----------------|:-----:|:------:|:---------:|:------:|
| ZMTP 3.0        |   Y   |   Y    |     Y     |   Y    |
| ZMTP 3.1        | Y (♭) |        |           | Y (♭)  |

(♭) New ZMTP 3.1 sockets are not yet supported

# Implementation status

> Note that the library is experimental, and the API is subject to change.

| Engine  |     CIO      |    JeroMQ    |   ZeroMQjs   |      Libzmq      |
|---------|:------------:|:------------:|:------------:|:----------------:|
| Status  | Experimental | Experimental | Experimental | Work in progress |

# Using in your projects

> Note that the library is experimental, and the API is subject to change.

This library is published to Maven Central.

## Gradle

### Repository

- First, add the Maven Central repository if it is not already there:

```kotlin
repositories {
    mavenCentral()
}
```

- Add the API package to your `commonMain` source-set dependencies
  (or to a particular target source-set dependencies, if you only need it on that target):

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.ptitjes:kzmq-core:0.1.0")
            }
        }
    }
}
```

- Then add one or more engine packages to their corresponding source-sets.

> In case you are lost, you can read more about
> [multiplatform projects](https://kotlinlang.org/docs/multiplatform-add-dependencies.html).

### CIO engine

The CIO engine supports only the JVM and native targets.
Add the CIO engine package to your `commonMain` source-set dependencies
(or to a particular target source-set dependencies):

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.ptitjes:kzmq-engine-cio:0.1.0")
            }
        }
    }
}
```

### JeroMQ engine

The JeroMQ engine supports only the JVM targets.
Add the JeroMQ engine package to your `jvmMain` source-set dependencies:

```kotlin
kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation("io.github.ptitjes:kzmq-engine-jeromq:0.1.0")
            }
        }
    }
}
```

### ZeroMQ.js engine

The ZeroMQ.js engine supports only the Node.js targets.
Add the ZeroMQ.js engine package to your `jsMain` source-set dependencies:

```kotlin
kotlin {
    sourceSets {
        jsMain {
            dependencies {
                implementation("io.github.ptitjes:kzmq-engine-zeromqjs:0.1.0")
            }
        }
    }
}
```

### Libzmq engine

The Libzmq engine supports only the native targets.
Add the Libzmq engine package to your `nativeMain` source-set dependencies:

```kotlin
kotlin {
    sourceSets {
        nativeMain {
            dependencies {
                implementation("io.github.ptitjes:kzmq-engine-libzmq:0.1.0")
            }
        }
    }
}
```

# Building

## Native build

In order to build for native targets, you need to have the development package for `libzmq`
(e.g. `zeromq-devel` on Fedora, or `libzmq3-dev` on Ubuntu).
