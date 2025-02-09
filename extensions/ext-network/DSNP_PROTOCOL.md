# DSNP Dead Simple Network Protocol

Version: 1.0.0-alpha

---

## 1. Overview

**DSNP Dead Simple Network Protocol** is a minimal, TCP‑only binary protocol designed for multiplayer game engines. It
leverages TCP’s inherent reliability and ordered delivery while keeping the protocol framing and message set extremely
simple. For secure communications, DSNP can be run over TLS. The protocol defines a basic handshake, authentication,
generic in‑game data, and disconnect messages.

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
  type field). This field enables precise extraction of the payload even if it is arbitrary binary data.

- **Payload (variable):**  
  The message data. Its structure depends on the message type. It can be arbitrary binary, a simple ASCII/UTF‑8
  encoded key=value string for control messages or any other format defined by the application (e.g. JSON).

*Example:*  
If a handshake request has a payload of 20 bytes, the sender writes:

- Magic header: `0xDE 0xAD`
- Message Type: e.g. `0x01`
- Payload Length: 20 (encoded as 4 bytes, big‑endian)
- Payload: 20 bytes of data

---

## 3. Message Types

DSNP defines six message types (each represented by 1 byte):

| **Type (Hex)** | **Name**                | **Direction**   | **Purpose**                                                        |
|----------------|-------------------------|-----------------|--------------------------------------------------------------------|
| **0x01**       | Handshake Request       | Client → Server | Initiate connection and advertise basic protocol/network settings. |
| **0x02**       | Handshake Response      | Server → Client | Acknowledge the handshake and return a status message.             |
| **0x03**       | Authentication Request  | Client → Server | Send credentials for client authentication.                        |
| **0x04**       | Authentication Response | Server → Client | Return the result of authentication (success or failure).          |
| **0x05**       | Generic Message         | Bidirectional   | Carry any in‑game data (commands, state updates, chat, etc.).      |
| **0x06**       | Disconnect              | Bidirectional   | Gracefully terminate the connection.                               |

---

## 4. Message Structures

Multi‑byte numeric fields are encoded in big‑endian order. For control messages, the payload is defined as an
ASCII/UTF‑8 key=value string. For game data, the payload is an arbitrary binary blob.

### 4.1 Handshake Request (Type 0x01)

**Purpose:**  
The client initiates a connection and provides its basic settings.

**Payload Format (ASCII key=value pairs):**

- **ver:** Protocol version (e.g., `"1"`)
- **id:** A short client identifier (e.g., `"PIXEL"`).

**Example Payload String:**

```
ver=1;id=PIXEL
```

*On the wire, DSNP sends:*

- Magic Header: `0xDE 0xAD`
- Message Type: `0x01`
- Payload Length: (length of the above ASCII string in bytes)
- Payload: the ASCII bytes for `"ver=1;id=PIXEL"`

---

### 4.2 Handshake Response (Type 0x02)

**Purpose:**  
The server replies to the handshake.

**Payload Format (ASCII key=value pairs):**

- **status:** `"ok"` or `"error"`.
-
    - **auth:** Authentication method (e.g., `"none"`,`"basic"`, `"digest"`).
- **reason:** (optional) A message on error (e.g., `"Invalid version"`).

**Example Payload:**

```
status=ok;auth=basic
```

---

### 4.3 Authentication Request (Type 0x03)

**Purpose:**  
The client sends credentials for authentication.

**Payload Format (ASCII key=value pairs):**

- **auth:** Authentication method (e.g., `"userpass"`).
- **user:** Username.
- **pass:** Password.

**Example Payload:**

```
auth=userpass;user=player1;pass=secret
```

---

### 4.4 Authentication Response (Type 0x04)

**Purpose:**  
The server returns the result of authentication.

**Payload Format (ASCII key=value pairs):**

- **status:** `"ok"` if authenticated; `"fail"` otherwise.
- **msg:** An optional message (e.g., `"Authenticated"`, `"Invalid credentials"`).

**Example Payload:**

```
status=ok;msg=Authenticated
```

---

### 4.5 Generic Message (Type 0x05)

**Purpose:**  
Used for all in‑game communication, including commands, state updates, and chat.

**Payload:**  
An arbitrary binary blob whose structure is defined by your application. (For text-based messages, you may use the
key=value format as above or more complex formats like JSON.)

---

### 4.6 Disconnect (Type 0x06)

**Purpose:**  
Gracefully close the connection.

**Payload Format (ASCII, optional):**

- **reason:** An optional disconnect reason.

**Example Payload:**

```
reason=Goodbye
```

---

## 5. Connection Lifecycle

1. **Connection Establishment:**  
   The client opens a TCP connection to the server.

2. **Handshake Phase:**
    - **Client:** Sends a Handshake Request (Type 0x01) with its protocol version, TCP settings, and client ID.
    - **Server:** Replies with a Handshake Response (Type 0x02).
    - If the status in the Handshake Response is not `"ok"`, the connection is terminated.

3. **Authentication Phase:**
    - **Client:** Sends an Authentication Request (Type 0x03) with its credentials.
    - **Server:** Replies with an Authentication Response (Type 0x04).
    - If authentication fails (status is `"fail"`), the connection is closed.

4. **Game Session:**  
   After successful authentication, both parties exchange Generic Messages (Type 0x05) carrying all game-related data.

5. **Disconnect:**  
   Either party can send a Disconnect message (Type 0x06) with an optional reason to gracefully end the session.

---

## 6. Implementation Considerations

- **Message Parsing:**  
  The receiver scans the TCP stream for DSNP messages by first detecting the 2‑byte magic header (0xDEAD). After that,
  it reads the 1‑byte message type and the next 4 bytes to obtain the payload length. The receiver then reads exactly
  that many bytes for the payload. This method is robust for arbitrary binary data.

- **TCP Settings (Java):**  
  For low latency, disable Nagle’s algorithm:
  ```java
  socket.setTcpNoDelay(true);
  ```
  You can also adjust the send/receive buffer sizes:
  ```java
  socket.setReceiveBufferSize(64 * 1024);
  socket.setSendBufferSize(64 * 1024);
  ```

- **Encryption:**  
  DSNP does not incorporate its own encryption. To secure DSNP communications, run it over TLS.
