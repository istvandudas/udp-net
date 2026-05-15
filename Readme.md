## Build & Test Status

![Build](https://github.com/istvandudas/udp-net/actions/workflows/java-ci-with-gradle.yml/badge.svg)

![Coverage](./.github/badges/jacoco.svg)



## Packet Polling Strategies
- pure busy‑spin
- pure park
- phased backoff
- exponential backoff
- random jitter backoff
- Aeron‑style idle strategies
- Disruptor‑style phased wait strategies
- three phase - hybrid strategy (**currently implemented**)