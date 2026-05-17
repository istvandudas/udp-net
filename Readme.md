# UDP-Net Framework

<!-- TOC -->
* [UDP-Net Framework](#udp-net-framework)
  * [Build & Test Status](#build--test-status)
  * [The Idea and Motivation](#the-idea-and-motivation)
  * [Current Framework Features](#current-framework-features)
  * [How to use](#how-to-use)
  * [Design Considerations](#design-considerations)
  * [Stress Testing](#stress-testing)
    * [UnreliableUdpEndpointStressTest](#unreliableudpendpointstresstest)
    * [How to Run Stress Tests](#how-to-run-stress-tests)
<!-- TOC -->

## Build & Test Status
![Build](https://github.com/istvandudas/udp-net/actions/workflows/gradle.yml/badge.svg)
![Coverage](./.github/badges/jacoco.svg)
## The Idea and Motivation

This project aims to provide a lightweight, pure‑Java, high‑performance UDP networking framework designed for real‑time applications such as fast‑paced multiplayer games.
While TCP/IP offers reliability, it often introduces latency, head‑of‑line blocking, and unpredictable stalls — all of which are unacceptable for responsive game networking. UDP is the natural choice, but building reliability and session semantics on top of it is non‑trivial.

This framework fills that gap: it delivers a clean, easy‑to‑use API with the foundations needed for building reliable, low‑latency communication.
Reliability features will be introduced incrementally, but the current API is stable and intended to remain backward‑compatible.

## Current Framework Features
- Connection management (initiation, acceptance, rejection, closure)
- Endpoint abstraction with listener support
- Client and server session identifiers (CSI/SSI)
- Dedicated high‑throughput sender thread
- Heartbeat, idle detection, and connection‑lost cleanup
- Zero‑copy buffer handling via custom Buffer class
- Strict framing layer for predictable packet structure

## How to use
The core abstraction of the framework is the [Endpoint](src/main/java/org/net/endpoint/Endpoint.java).
The default UDP implementation is [UnreliableUdpEndpoint](https://github.com/istvandudas/udp-net/blob/main/src/main/java/org/net/endpoint/udp/endpoint/UnreliableUdpEndpoint.java) which provides a lightweight, high‑performance communication layer on top of UDP.
To create and start an endpoint:
- Construct it with an [EndpointConfig](src/main/java/org/net/endpoint/EndpointConfig.java)
- Register an [EndpointListener](src/main/java/org/net/endpoint/EndpointListener.java)
- Start the endpoint — this launches all internal threads (listener, sender, maintenance)

**Conceptual Model**
An Endpoint is a neutral communication primitive.
It can act as:
- a client
- a server
- a sender
- a receiver

…but each role requires its own instance.

This keeps the API simple and avoids hidden state or role‑dependent behavior.

**Note**: This module is meant to be used like any regular dependency.
To do that, you must first **publish it to your local Maven repository** so other
projects can resolve it normally.

**What Happens When You Start an Endpoint**
Starting an endpoint automatically spins up:
- a listener thread (receives packets)
- a sender thread (high‑throughput, zero‑garbage sending)
- a maintenance thread (heartbeats, idle detection, cleanup)

**You only need to**:
- configure it
- register a listener
- start it
- connection via endpoint (if needed in case of client)
- handle incoming event via EndpointListener
- sending messages via UdpConnection

**Keep in Mind**
- Respect MTU‑safe packet size — the practical limit is ~1200 bytes,
but slightly less in this framework because each packet includes a 
34‑byte header. Your application‑level payload must fit within this 
safe range to avoid fragmentation. See more about MTU limits.
- Handle framing and encryption at the application layer — the
framework provides raw, high‑performance UDP transport, but it
is the sender’s responsibility to implement framing and encryption/decryption
on top of it. This keeps the core fast, predictable, and zero‑garbage.

Everything else is handled internally.

## Design Considerations
- High performance — optimized for real‑time workloads
- Zero garbage — predictable memory behavior, no GC spikes
- Low latency — minimal overhead between user code and the network
- High throughput — efficient batching and sending
- Low CPU usage — avoids busy‑waiting and unnecessary allocations
- Ease of use — simple API, minimal boilerplate, clear semantics

## Stress Testing
The framework includes a stress test suite to verify high-throughput performance and reliability under load.

### UnreliableUdpEndpointStressTest
This test simulates multiple senders pushing high volumes of data to a single receiver using different packet sizes (64B to 1024B) and varying network speeds. It verifies that:
- The system handles high packet rates without internal failures.
- All sent packets are correctly received.
- Metrics are accurately tracked and reported.

### How to Run Stress Tests
Stress tests are excluded from the default test run to save time. You can run them using the following commands:

**Run all stress tests:**
```bash
./gradlew stressTest
```
*or*
```bash
./gradlew test -Pstress
```

**Run only the UnreliableUdpEndpointStressTest:**
```bash
./gradlew test --tests org.net.endpoint.udp.endpoint.UnreliableUdpEndpointStressTest -Pstress
```
