#  DSNP

### Version: 1.0.0

---

## 1. Overview

**DSNP (0xDEAD Simple Network Protocol)** is a minimal, TCP‑only binary protocol designed for multiplayer game engines. It
leverages TCP inherent reliability and ordered delivery while keeping the protocol framing and message set extremely
simple. For secure communications, DSNP can be run over TLS. The protocol defines handshake, arbitrary in‑game data,
heartbeat and disconnect messages.

Please note that for really intensive real‑time games (or for scenarios where internet connectivity is unreliable), you
might need to consider UDP or a more sophisticated protocol on top of UDP. However, for many games, TCP is sufficient
and much easier and safer to work with. Protocols like QUIC and SCTP are great in theory, but since they lack native
support, over-the-top implementations can be complex and inefficient.

Additionally, if needed, a UDP-based layer can work alongside DSNP. The DSNP handshake can be extended to exchange 
additional security parameters, which would then be used to encode and decode data sent over a connectionless 
transport like UDP.

---

## 2. Message Framing

Every DSNP message is framed as follows:

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
| **0x03**       | Data Message       | Bidirectional   | Carry any in‑game data (commands, state updates, chat, etc.).      |
| **0x04**       | Heartbeat          | Bidirectional   | Connection app-level heartbeat.                                    |
| **0x05**       | Disconnect         | Bidirectional   | Gracefully terminate the connection.                               |

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
- **scope:** A scope identifier (e.g., `"game"`, `"lobby"`)
- **auth:** Authentication method (e.g., `"none"`, `"basic"`, `"digest"`)

**Payload Auth fields (basic):**

- **auth**: `"basic"`
- **username:** Username for basic authentication.
- **password:** Password for basic authentication.

**Payload Auth fields (digest, first-request):**

- **auth:** `"digest"`

**Payload Auth fields (digest, subsequent-request):**

- **auth:** `"digest"`
- **nonce:** Server nonce.
- **cnonce:** Client nonce.
- **response:** Digest response.
- **qop:** Quality of protection (e.g., `"auth"`, `"auth-int"`) - based on the server challenge.
- **nc:** Nonce count (incremented for each new handshake).

**Example Payload String:**

```
ver=1.0;scope=game;auth=basic;username=player1;password=secret
```

*On the wire, DSNP sends:*

- Magic Header: `0xDE 0xAD`
- Message Type: `0x01`
- Payload Length: (length of the above ASCII string in bytes)
- Payload: the ASCII bytes for `"..."`

---

### 4.2 Handshake Response (Type 0x02)

**Purpose:**  
The server replies to the handshake.

The response contains a status field indicating the result of the handshake. For simplicity purposes, the status field
values use HTTP‑like codes (e.g., `"200"` for success, `"401"` for unauthorized, ...).

**Payload Format (ASCII key=value pairs):**

- **status:** `"200"`, `"401"` or any HTTP‑like status code.
- **reason:** (optional) A message on error (e.g., `"Invalid version"`).
- **heartbeat:** (optional) Heartbeat interval in seconds.

**Payload Auth fields (digest, challenge):**

- **auth:** `"digest"`
- **nonce:** Server nonce.
- **qop:** Quality of protection (e.g., `"auth"`, `"auth-int"`).
- **realm:** Authentication realm.
- **opaque:** Opaque value.
- **algorithm:** Hash algorithm (e.g., `"MD5"`, `"SHA-256"`).

**Example Payload:**

```
status=200;heartbeat=30
```

---

### 4.3 Data Message (Type 0x03)

**Purpose:**  
Used for all in‑game communication, including commands, state updates, chat, and so on.

**Payload:**  
An arbitrary data blob whose structure is defined by your application. (For text-based messages, you may use the
key=value format as above or more comprehensive formats like JSON.)

**Example Payload (JSON):**

```json
{
  "type": "chat",
  "from": "Alice",
  "message": "Hello, Bob!"
}
```

---

### 4.4 Heartbeat (Type 0x04)   

**Purpose:** 
Application layer keep-alive message to ensure the connection remains active.

By default, has no payload (payload length is `0`).
Some implementations might include data, e.g., a timestamp or service status, with payload length defined accordingly.

---

### 4.5 Disconnect (Type 0x05)

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
   After successful handshake, both parties exchange Data Messages (Type `0x03`) carrying all game-related data.

4. **Connection Maintenance:**
   Both parties SHOULD have a configurable idle-timeout (time without data transfer), which, if reached, SHALL trigger
   a Hearbeat (Type `0x04`) message to ensure the connection isn't closed prematurely. The idle-timeout SHOULD be
   configured to no longer than 30 seconds, a value widely adopted in network protocols and load balancers to maintain 
   NAT bindings and keep-alive states across intermediate devices.

5. **Disconnect:**  
   Either party can send a Disconnect message (Type `0x05`) with an optional reason to gracefully end the connection.

---

## 6. Implementation Considerations

- **Message Parsing:**  
  The receiver scans the TCP stream for DSNP messages by first detecting the 2‑byte magic header (`0xDEAD`). After that,
  it reads the 1‑byte message type and the next 4 bytes to obtain the payload length. The receiver then reads exactly
  that many bytes for the payload.

- **TCP Settings (Java):**  
  For lower latency, disable Nagle’s algorithm:
  ```java
  socket.setTcpNoDelay(true);
  ```
  You can also adjust the send/receive buffer sizes, example:
  ```java
  socket.setReceiveBufferSize(64 * 1024);
  socket.setSendBufferSize(64 * 1024);
  ```

- **Encryption:**  
  DSNP does not incorporate its own encryption. To secure DSNP communications, run it over TLS which is widely supported
  and battle-tested.
