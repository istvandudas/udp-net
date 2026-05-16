# UDP-Net Framework

## Build & Test Status
![Build](https://github.com/istvandudas/udp-net/actions/workflows/gradle.yml/badge.svg)
![Coverage](./.github/badges/jacoco.svg)

## The idea and Motivation

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

**What Happens When You Start an Endpoint**
Starting an endpoint automatically spins up:
- a listener thread (receives packets)
- a sender thread (high‑throughput, zero‑garbage sending)
- a maintenance thread (heartbeats, idle detection, cleanup)

**You only need to**:
- configure it
- register a listener
- start it

Everything else is handled internally.

## Design Considerations
- High performance — optimized for real‑time workloads
- Zero garbage — predictable memory behavior, no GC spikes
- Low latency — minimal overhead between user code and the network
- High throughput — efficient batching and sending
- Low CPU usage — avoids busy‑waiting and unnecessary allocations
- Ease of use — simple API, minimal boilerplate, clear semantics