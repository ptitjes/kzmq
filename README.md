<div style="text-align: center; margin-top: 2em; margin-bottom: 2em;">
    <picture>
        <source media="(prefers-color-scheme: dark)"
            srcset="https://github.com/ptitjes/kzmq/raw/main/docs/images/kzmq-logo-dark-background.svg">
        <img alt="Kzmq logo" width="500" style="max-width:100%;"
            src="https://github.com/ptitjes/kzmq/raw/main/docs/images/kzmq-logo-light-background.svg">
    </picture>
</div>

![Build](https://github.com/ptitjes/kzmq/actions/workflows/main.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/org.zeromq/kzmq)](https://mvnrepository.com/artifact/org.zeromq)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

> Warning: This library is a work in progress.

Kzmq is a Kotlin multi-platform ZeroMQ library. It supports multiple backend engines:

- CIO, a Kotlin coroutine-based implementation (using `ktor-network` and `ktor-io`),
- JeroMQ, a pure Java implementation of ZeroMQ,
- ZeroMQ.JS, a Node.JS addon implementation of ZeroMQ,
- Libzmq, the main native implementation of ZeroMQ.

# Engines

The following tables might help you choose a backend engine depending on your needs.

## Targets

| Target/Engine | CIO | JeroMQ | ZeroMQ.JS | Libzmq |
|---------------|:---:|:------:|:---------:|:------:|
| JVM           |  Y  |   Y    |           |        |
| Node.JS       |     |        |     Y     |        |
| Native        |  Y  |        |           |   Y    |

## Transports

| Transport/Engine |  CIO  | JeroMQ | ZeroMQ.JS | Libzmq |
|------------------|:-----:|:------:|:---------:|:------:|
| TCP              |   Y   |   Y    |     Y     |   Y    |
| IPC              | Y (♭) |        |     Y     |   Y    |

(♭) Supported on Native. Requires JVM >= 16 for the JVM target.

## Protocol Version

| Protocol/Engine |  CIO  | JeroMQ | ZeroMQ.JS | Libzmq |
|-----------------|:-----:|:------:|:---------:|:------:|
| ZMTP 3.0        |   Y   |   Y    |     Y     |   Y    |
| ZMTP 3.1        | Y (♭) |        |           | Y (♭)  |

(♭) New ZMTP 3.1 sockets are not yet supported

# Build

Install `libncurses5` (`ncurses-compat-libs` on Fedora).

Install `libzmq` (`zeromq-devel` on Fedora).
