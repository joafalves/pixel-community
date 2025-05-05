# DSNP

### Version: 1.0.0

---

## 1. Overview

**DSNP (0xDEAD Simple Network Protocol)** is a minimal, TCP‑only binary protocol designed for multiplayer game engines.
It leverages TCP inherent reliability and ordered delivery while keeping the protocol framing and message set extremely
simple. For secure communications, DSNP can be run over TLS. The protocol defines handshake, arbitrary in‑game data,
heartbeat and disconnect messages.

Please note that for really intensive real‑time games (or for scenarios where internet connectivity is unreliable), you
might need to consider UDP or a more sophisticated protocol on top of UDP. However, for many games, TCP is sufficient
and much easier and safer to work with. Protocols like QUIC and SCTP are great in theory, but since they lack native
support, over-the-top implementations can be complex and inefficient.

Additionally, if needed, a UDP-based layer can work alongside DSNP. The DSNP handshake can be extended to exchange
additional security parameters, which would then be used to encrypt and decrypt data sent over a connectionless
transport like UDP.

---

## 2. Message Framing

Every single-message DSNP message (without fragmentation) is framed as follows:

```
[Magic Header (2 bytes)] [Message Type (1 byte)] [Payload Length (4 bytes)] [Payload (variable)]
```

- **Magic Header (2 bytes):**  
  Fixed value **0xDEAD** (hexadecimal). This marks the start of a DSNP message and lets the receiver easily
  resynchronize if other (non‑DSNP) data appears on the stream.

- **Message Type (1 byte):**  
  A one‑byte code indicating the message’s purpose (see Section 3).

- **Payload Length (4 bytes):**  
  An unsigned 32‑bit integer in big‑endian order specifying the length (in bytes) of the payload only (excluding the
  type field). This field enables precise extraction of the payload even if it is arbitrary binary data. The maximum
  theoretical payload size is 4 GB (`2^32 bytes`), but in practice, it should be limited to a reasonable size to avoid
  overloading the network stack.

- **Payload (variable):**  
  The message data. Its structure depends on the message type. It can be arbitrary binary, a simple ASCII/UTF‑8
  encoded key=value string for control messages or any other format defined by the application (e.g., JSON).

*Example:*  
If a handshake request has a payload of `20 bytes`, the sender writes:

- Magic header: `0xDE 0xAD`
- Message Type: e.g., `0x01`
- Payload Length: 20 (encoded as 4 bytes, big‑endian)
- Payload: 20 bytes of data

---

## 3. Message Types

DSNP defines the following message types (each represented by 1 byte):

| **Type (Hex)** | **Name**           | **Direction**   | **Purpose**                                                        |
|----------------|--------------------|-----------------|--------------------------------------------------------------------|
| **0x01**       | Handshake Request  | Client → Server | Initiate connection and advertise basic protocol/network settings. |
| **0x02**       | Handshake Response | Server → Client | Acknowledge the handshake and return a status message.             |
| **0xA0**       | Data Message       | Bidirectional   | Carry any in‑game data (commands, state updates, chat, etc.).      |
| **0xF0**       | Heartbeat          | Bidirectional   | Connection app-level heartbeat.                                    |
| **0xFA**       | Disconnect         | Bidirectional   | Gracefully terminate the connection.                               |

---

## 4. Message Structures

Multi‑byte numeric fields are encoded in big‑endian order. For control messages, the payload is defined as an
ASCII/UTF‑8 key=value string. For game data, the payload is an arbitrary binary blob.

### 4.1 Handshake Request (Type 0x01)

**Purpose:**  
The client initiates a connection and provides its basic settings and authentication depending on the client and
server configuration.

This handshake might happen multiple times before the client connection is accepted. For example, if the authentication
method is `"digest"`, the server may challenge the client with a nonce, and the client must respond with a digest.

**Payload Format (ASCII key=value pairs):**

- **ver:** API version (e.g., `"1.0"`)
- **channel:** A channel identifier (e.g., `"game"`, `"chat"`, `"assets"`, etc...)
- **auth:** Authentication line (e.g., `"basic <base64(username:password)>"`)

> Not setting the `auth` field means that the client doesn't suggest any authentication method. The server may
> respond with a challenge or accept the connection without authentication.

**Payload Auth fields (BASIC example):**

- **auth**: `"basic <base64(username:password)>"`

**Payload Auth fields (DIGEST example):**

DIGEST authentication may be started when the server doesn't support BASIC authentication or the client does not
provide the `auth` field. The server will respond with a challenge, and the client must respond with a digest.

- **auth:** `"digest username="<username>", realm="<realm>", nonce="<server-nonce>", uri="<scope>",
  response="<MD5(username:realm:password:nonce:cnonce:nc)>", nc="<nonce-count>", cnonce="<client-nonce>"

**Example Payload String:**

```
ver=1.0;channel=game;auth=basic dXNlcm5hbWU6cGFzc3dvcmQ=
```

*On the wire, DSNP sends:*

- Magic Header: `0xDE 0xAD`
- Message Type: `0x01`
- Payload Length: (length of the above ASCII string in bytes)
- Payload: `...`

> Note that the best way to ensure safety when sending the user credentials is to use TLS. The DSNP protocol does not
> impose any specific encryption or authentication method. It is up to the implementation to ensure that the
> credentials are sent securely.

---

### 4.2 Handshake Response (Type 0x02)

**Purpose:**  
The server replies to the handshake.

The response contains a status field indicating the result of the handshake. For simplicity purposes, the status field
values use HTTP‑like codes (e.g., `"200"` for success, `"401"` for unauthorized, ...).

**Payload Format (ASCII key=value pairs):**

- **status:** `"200"`, `"401"` or any HTTP‑like status code.
- **reason:** (optional) A message on error (e.g., `"Invalid version"`).
- **auth:** (optional) Authentication challenge line (e.g., `"digest realm=game,nonce=<server-nonce>,..."`).

**Example Payload (simple):**

```
status=200;
```

**Example Payload (with DIGEST auth challenge):**

```
status=401;reason=Unauthorized;auth=digest realm="dsnp",nonce="5d41402abc4b2a76b9719d911017c592",qop="auth",
algorithm=MD5,opaque="3b4f1e2d9a7c6f5b8e1d2c3a4b5e6f7a"
```

---

### 4.3 Data Message (Type 0xA0)

**Purpose:**  
Used for all in‑game communication, including commands, state updates, chat, and so on.

**Payload:**  
An arbitrary data blob whose structure is defined by your application. (For text-based messages, you may use the
key=value format as above or more comprehensive formats like JSON.)

> You may use any serialization format you prefer (e.g., Protocol Buffers, FlatBuffers, JSON, etc.) on the application
> layer. The DSNP protocol does not impose any specific serialization format.

**Example Payload (JSON):**

```json
{
  "type": "chat",
  "from": "Alice",
  "message": "Hello, Bob!"
}
```

---

### 4.4 Heartbeat (Type 0xF0)

**Purpose:**
Application layer keep-alive message to ensure the connection remains active.

By default, has no payload (payload length is `0`).
Some implementations might include data, e.g., a timestamp or service status, with payload length defined accordingly.

---

### 4.5 Disconnect (Type 0xFA)

**Purpose:**  
Gracefully close the connection. This does not necessarily end the session, as the session's lifecycle is managed by
the implementation.

This message can be sent by any party, server or client.

**Payload Format (ASCII, optional):**

- **reason:** (optional) Disconnect reason.
- **timeout:** (optional) Timeout in seconds before the shutdown sender closes the connection.

**Example Payload:**

```
reason=Maintenance;timeout=300
```

---

## 5. Connection Lifecycle

1. **Connection Establishment:**  
   The client opens a TCP connection to the server.

2. **Handshake Phase:**
    - **Client:** Sends a Handshake Request (Type `0x01`) with its protocol version, TCP settings, and client ID.
    - **Server:** Replies with a Handshake Response (Type `0x02`).
    - If the status in the Handshake Response is not successful, the connection is terminated.

3. **Game Session:**  
   After successful handshake, both parties exchange Data Messages (Type `0xA0`) carrying all game-related data.

4. **Connection Maintenance:**
   Both parties SHOULD have a configurable idle-timeout (time without data transfer), which, if reached, SHALL trigger
   a Hearbeat (Type `0xF0`) message to ensure the connection isn't closed prematurely. The idle-timeout SHOULD be
   configured to no longer than 30 seconds, a value widely adopted in network protocols and load balancers to maintain
   NAT bindings and keep-alive states across intermediate devices.

5. **Disconnect:**  
   Either party can send a Disconnect message (Type `0xFA`) with an optional reason to gracefully end the connection.

---

## 6. Implementation Considerations & Opinions

### Message Parsing:

The receiver scans the TCP stream for DSNP messages by first detecting the 2‑byte magic header (`0xDEAD`). After that,
it reads the 1‑byte message type and the next 4 bytes to obtain the payload length. The receiver then reads exactly
that many bytes for the payload.

### Network Channels

You MAY provide multiple network channel configurations to differentiate between different types of data. For example,
you may have a channel for game state updates, another for chat messages, and a third for assets. Each channel
can have its own settings, such as buffer sizes, timeouts, and other TCP-level options. This allows you to optimize the
network stack for the characteristics of each data type.

This is especially useful to avoid overloading latency-sensitive data (e.g., game state updates) with less time-critical
payloads such as chat messages, logs, or asset transfers. Channels provide an abstraction for clean separation and allow
fine-tuning of performance per use case.

Implementations may choose to bind each channel to a separate TCP connection. In this case, clients initiate a new DSNP
connection for each desired channel, using the channel field in the handshake (e.g., channel=game, channel=asset, etc.).
The server may independently accept or reject each channel request.

You MAY use the same channel abstraction if later you decide to implement a UDP-based layer on top of DSNP.

### Game Processing

This protocol is designed to be simple and easy to implement. It is not intended to be a fully-opinionated game network
stack. You are free to implement your game logic on top of DSNP as you see fit.

For example, you may choose to implement a custom serialization format for your game state updates, or you may use a
tick based approach to send updates at regular intervals. The protocol is flexible enough to accommodate a wide range of
game architectures.

### TCP Settings (Java):

For lower latency, disable Nagle’s algorithm:

```java
socket.setTcpNoDelay(true);
```

You can also adjust the send/receive buffer sizes, example:

```java
socket.setReceiveBufferSize(64*1024);
socket.setSendBufferSize(64*1024);
```

### Encryption:

DSNP does not incorporate its own encryption. To secure DSNP communications, run it over TLS which is widely supported
and battle-tested.
