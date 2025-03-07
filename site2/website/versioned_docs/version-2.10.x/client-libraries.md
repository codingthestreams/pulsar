---
id: client-libraries
title: Pulsar client libraries
sidebar_label: "Overview"
original_id: client-libraries
---

Pulsar supports the following client libraries:

| Language  | Documentation                                                                            | Release note                                                             | Code repo                                                             |
|-----------|------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|-----------------------------------------------------------------------|
| Java      | - [User doc](client-libraries-java.md) <br /><br />- [API doc](/api/client/)             | [Here](/release-notes/)                                                  | [Here](https://github.com/apache/pulsar/tree/master/pulsar-client)    |
| C++       | - [User doc](client-libraries-cpp.md) <br /><br />- [API doc](@pulsar:apidoc:cpp@)       | [Here](/release-notes/)                                                  | [Here](https://github.com/apache/pulsar-client-cpp)                   |
| Python    | - [User doc](client-libraries-python.md) <br /><br />- [API doc](@pulsar:apidoc:python@) | [Here](/release-notes/)                                                  | [Here](https://github.com/apache/pulsar-client-python)                |
| WebSocket | [User doc](client-libraries-websocket.md)                                                | [Here](/release-notes/)                                                  | [Here](https://github.com/apache/pulsar/tree/master/pulsar-websocket) |
| Go client | [User doc](client-libraries-go.md)                                                       | [Here](https://github.com/apache/pulsar-client-go/blob/master/CHANGELOG) | [Here](https://github.com/apache/pulsar-client-go)                    |
| Node.js   | [User doc](client-libraries-node.md)                                                     | [Here](https://github.com/apache/pulsar-client-node/releases)            | [Here](https://github.com/apache/pulsar-client-node)                  |
| C#        | [User doc](client-libraries-dotnet.md)                                                   | [Here](https://github.com/apache/pulsar-dotpulsar/blob/master/CHANGELOG) | [Here](https://github.com/apache/pulsar-dotpulsar)                    |

:::note

- The code repos of **Java, C++, Python,** and **WebSocket** clients are hosted in the [Pulsar main repo](https://github.com/apache/pulsar) and these clients are released with Pulsar, so their release notes are parts of [Pulsar release note](/release-notes/).
- The code repos of **Go, Node.js,** and **C#** clients are hosted outside of the [Pulsar main repo](https://github.com/apache/pulsar) and these clients are not released with Pulsar, so they have independent release notes.

:::

## Feature matrix
Pulsar client feature matrix for different languages is listed on [Pulsar Feature Matrix (Client and Function)](https://docs.google.com/spreadsheets/d/1YHYTkIXR8-Ql103u-IMI18TXLlGStK8uJjDsOOA0T20/edit#gid=1784579914) page.

## Third-party clients

Besides the official released clients, multiple projects on developing Pulsar clients are available in different languages.

> If you have developed a new Pulsar client, feel free to submit a pull request and add your client to the list below.

| Language | Project | Maintainer | License | Description |
|----------|---------|------------|---------|-------------|
| Go | [pulsar-client-go](https://github.com/Comcast/pulsar-client-go) | [Comcast](https://github.com/Comcast) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | A native golang client |
| Go | [go-pulsar](https://github.com/t2y/go-pulsar) | [t2y](https://github.com/t2y) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | 
| Haskell | [supernova](https://github.com/cr-org/supernova) | [Chatroulette](https://github.com/cr-org) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | Native Pulsar client for Haskell | 
| Scala | [neutron](https://github.com/cr-org/neutron) | [Chatroulette](https://github.com/cr-org) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | Purely functional Apache Pulsar client for Scala built on top of Fs2 |
| Scala | [pulsar4s](https://github.com/sksamuel/pulsar4s) | [sksamuel](https://github.com/sksamuel) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | Idomatic, typesafe, and reactive Scala client for Apache Pulsar |
| Rust | [pulsar-rs](https://github.com/wyyerd/pulsar-rs) | [Wyyerd Group](https://github.com/wyyerd) | [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) | Future-based Rust bindings for Apache Pulsar |
| .NET | [pulsar-client-dotnet](https://github.com/fsharplang-ru/pulsar-client-dotnet) | [Lanayx](https://github.com/Lanayx) | [![GitHub](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT) | Native .NET client for C#/F#/VB |
| Node.js | [pulsar-flex](https://github.com/ayeo-flex-org/pulsar-flex) | [Daniel Sinai](https://github.com/danielsinai), [Ron Farkash](https://github.com/ronfarkash), [Gal Rosenberg](https://github.com/galrose)| [![GitHub](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT) | Native Nodejs client |
