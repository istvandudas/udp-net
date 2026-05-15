<!-- TOC -->
  * [Feature roadmap](#feature-roadmap)
  * [Metrics](#metrics)
    * [Sender](#sender)
    * [Receiver](#receiver)
    * [End-to-end](#end-to-end)
  * [Stress test profiles (for 1 Gbit/s switch)](#stress-test-profiles-for-1-gbits-switch)
    * [Profile A - Baseline (no feature)](#profile-a---baseline-no-feature)
    * [Profile B — Rate‑limited](#profile-b--ratelimited)
    * [Profile C — Reliability enabled](#profile-c--reliability-enabled)
    * [Profile D — Congestion control](#profile-d--congestion-control)
    * [Profile E — Object pooling](#profile-e--object-pooling)
  * [What measures](#what-measures-)
    * [Step 1 — Unreliable UDP](#step-1--unreliable-udp)
    * [Step 2 — Add token bucket](#step-2--add-token-bucket)
    * [Step 3 — Add batching](#step-3--add-batching)
    * [Step 4 — Add object pools](#step-4--add-object-pools)
    * [Step 5 — Add reliability](#step-5--add-reliability)
    * [Step 6 — Add congestion control](#step-6--add-congestion-control)
  * [How to structure the test harness](#how-to-structure-the-test-harness)
    * [Sender](#sender-1)
    * [Receiver](#receiver-1)
<!-- TOC -->

## Feature roadmap
1. Unreliable UDP (baseline)
2. UDP + rate limiting (token bucket)
3. UDP + batching
4. UDP + object pools
5. UDP + reliability (ACKs, NACKs, selective retransmission)
6. UDP + congestion control (AIMD, BBR‑like, or your own)
7. UDP + flow control
8. UDP + fragmentation / reassembly
9. UDP + encryption

## Metrics
### Sender
1. Packets per second (pps)
2. Bytes per second (bps)
3. CPU usage per thread
4. GC activity (minor/major collections)
5. Allocation rate (bytes/sec)
6. Object pool hit/miss ratio
7. Send queue backlog
8. Token bucket refill/consume stats
9. Latency of send() syscall
 
### Receiver
1. Packets received
2. Packets dropped
3. Out‑of‑order packets
4. Duplicate packets
5. Jitter
6. Reassembly queue depth
7. ACK/NACK rate (when reliability added)

### End-to-end
1. Goodput (useful payload/sec)
2. Retransmission rate
3. Latency distribution (p50, p90, p99, p999)
4. Protocol overhead %
5. Congestion window evolution (when CC added)

## Stress test profiles (for 1 Gbit/s switch)
### Profile A - Baseline (no feature)
Goal: measure raw UDP throughput and CPU cost.

**Rates:**
- 32 B → 250–350 kpps
- 64 B → 120–175 kpps
- 128 B → 60–90 kpps
- 256 B → 30–45 kpps
- 512 B → 15–22 kpps
- 1024 B → 7–11 kpps
- 1400 B → 6–8 kpps

**Threads:**
1 thread for all except 32–64 B (use 2 threads)

### Profile B — Rate‑limited
Goal: verify token bucket correctness.
Use the same pps targets as above.

**Verify:**
- no overshoot
- stable long‑term average
- burst behavior matches bucket capacity

### Profile C — Reliability enabled
Goal: measure cost of ACKs, retransmissions, and queues.
Expect 10–40% throughput drop depending on design.

**Measure:**
- retransmission rate
- ACK/NACK overhead
- RTT estimation stability
- reorder buffer size

### Profile D — Congestion control
Goal: verify fairness and stability.

Compete 2–4 senders on the same switch.

**Expect:**
- AIMD → sawtooth throughput
- BBR‑like → stable throughput
- Your own → measurable behavior

### Profile E — Object pooling
Goal: measure GC elimination.

**Metrics:**
- allocation rate → should drop to near zero
- GC pauses → should disappear
- throughput → should increase 10–30%

## What measures 
### Step 1 — Unreliable UDP
- Max pps
- CPU cost per packet
- GC cost
- Syscall cost
- NIC saturation point

### Step 2 — Add token bucket
- Overshoot %
- Jitter introduced by throttling
- Burst absorption
- CPU cost of bucket

### Step 3 — Add batching
- pps improvement
- syscall reduction
- latency impact

### Step 4 — Add object pools
- allocation rate → should drop to zero
- GC pauses → should vanish
- throughput improvement

### Step 5 — Add reliability
- retransmission rate
- reorder buffer depth
- ACK/NACK overhead
- RTT estimation stability

### Step 6 — Add congestion control
- fairness between flows
- stability under load
- oscillation amplitude
- convergence time

## How to structure the test harness
### Sender
- N worker threads (1–2)

**Each thread:**
- generates packets
- timestamps them
- sends via DatagramChannel
- logs metrics every second

### Receiver
Single thread

**Measures:**
- arrival time
- sequence number
- loss
- jitter
- reorder
- duplicates

**Metrics collector**
Runs in a separate thread
**Aggregates:**
- pps
- bps
- latency histograms
- GC stats
- CPU usage
- bucket stats
- retransmission stats

**Visualization**
- Grafana dashboards
- Flamegraphs?? for CPU hotspots