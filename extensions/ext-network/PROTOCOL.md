# Pixel Framework Network Protocol (PFNP)

Version: 1.0

---

## 1. Introduction

The Pixel Framework Network Protocol (PFNP) is a lightweight binary protocol designed for real‑time communication in multiplayer games. PFNP is engineered for efficiency, security, and adaptability. It supports dynamic key‑value parameters with explicit type tags, fragmentation, extended sequence numbering, and multiplexing of application data over configurable streams. PFNP is transport‑agnostic (usable over UDP or TCP) and negotiates session parameters (including optional encryption) during an initial handshake.

PFNP defines two main categories of messages:

- **Control Messages**: For establishing and managing the connection (handshake, authentication, stream negotiation, heartbeat, shutdown).
- **Data Messages**: For sending and receiving application-level payloads over negotiated streams (requests, responses, acknowledgements).

If encryption is negotiated, all messages (control and data) **after** the handshake are encrypted at the connection level.

---

## 2. Control and Data Messages

**Control messages** are **ordered** and share a single, global 16-bit sequence number space. That number is incremented (modulo 65,536) for each new outgoing control message. The first control message sequence number SHOULD be randomly generated for security/unpredictability. Fragmentation sequence numbers are also 16-bit and may be used if a message spans multiple fragments.

**Data messages** travel over “application streams.” Each such stream can track its own sequence numbering (also 16-bit), or it can reuse the same space as control—implementation MAY vary. Typically, each data stream’s first sequence number is randomly initialized at the time the stream is opened, then incremented by 1 for each new data message on that stream.

Because sequence numbers are 16-bit, they eventually overflow. When a sequence number reaches 65,535 and increments, it wraps around to 0. Implementations MUST handle wrap-around carefully (including for acknowledgements).

---

## 3. Message Formats

All messages have an 8-bit **Message Type**, plus other fields depending on the type. The **Flags** byte (also 8 bits) can serve multiple purposes. Currently, bits 0 and 1 are used for fragmentation indicators (“first fragment” and “last fragment,” see below), and the remaining bits are reserved for future use.

When a message is fragmented, each fragment is sent separately, using the same **Sequence Number** (for the control or data context), and incrementing **Fragment Sequence Number** to indicate fragment ordering. If not fragmented, the **Fragment Sequence Number** can be 0.

Where a message has a **Payload Length** field, it indicates the size in **bytes** of that particular fragment’s payload (not including headers).

### 3.1 Control Messages

---

#### 3.1.1 Handshake Request (Type 1)

| Field                     | Size (bits) | Description                                                                                           |
|---------------------------|-------------|-------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x01`.                                                                                               |
| **Sequence Number**       | 16          | Global control message sequence, randomly initialized for the session.                                |
| **Fragment Sequence Num** | 16          | If the message is split across multiple fragments.                                                    |
| **Flags**                 | 8           | Bits 0..1 are fragmentation flags. Bit 0 = “first fragment”, Bit 1 = “last fragment”. The rest are 0. |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                         |

**Payload**: A set of key‑value parameters (Section 3.3) typically including:
- `"api_version"` (string)
- `"client_id"` (string)
- `"encryption"` (e.g., `"AES-GCM"`, `"none"`, or a comma-separated list)
- `"dh_public"` if doing Diffie-Hellman for key agreement

---

#### 3.1.2 Handshake Answer (Type 2)

| Field                     | Size (bits) | Description                                                                                           |
|---------------------------|-------------|-------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x02`.                                                                                               |
| **Sequence Number**       | 16          | Must match the Handshake Request’s sequence.                                                          |
| **Fragment Sequence Num** | 16          | Fragment ordering if needed.                                                                          |
| **Flags**                 | 8           | Bits 0..1 for fragmentation. Additionally, if bit 2 = 1 => handshake failed; if bit 2 = 0 => success. |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                         |

**Payload**: A set of key-value parameters, such as:
- `"dh_public"` (if using DH)
- `"session_id"` (32-bit random)
- `"encryption"` (the selected cipher or `"none"`)
- `"auth_type"` (e.g., `"none"`, `"basic"`, etc.)

If the handshake fails (bit 2 in **Flags** = 1), a `"failure_reason"` may be included.

---

#### 3.1.3 Authentication Request (Type 8)

| Field                     | Size (bits) | Description                                                                                                        |
|---------------------------|-------------|--------------------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x08`.                                                                                                            |
| **Sequence Number**       | 16          | Next control message sequence.                                                                                     |
| **Fragment Sequence Num** | 16          | For fragmentation if needed.                                                                                       |
| **Flags**                 | 8           | Bits 0..1 for fragmentation.                                                                                       |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                                      |

**Payload**: Key‑value parameters with credentials or tokens (depending on `auth_type`), for example:
- `"username"` (string)
- `"password"` or `"password_hash"` (string/binary)

---

#### 3.1.4 Authentication Response (Type 9)

| Field                     | Size (bits) | Description                                                                                                                      |
|---------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x09`.                                                                                                                          |
| **Sequence Number**       | 16          | Must match the Authentication Request’s sequence.                                                                                |
| **Fragment Sequence Num** | 16          | For fragmentation if needed.                                                                                                     |
| **Flags**                 | 8           | Bits 0..1 for fragmentation.                                                                                                     |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                                                    |

**Payload**: Key-value parameters indicating success or failure, e.g.:
- `"auth_status"` = `"ok"` or `"failed"`
- `"failure_reason"` if `"failed"`

---

#### 3.1.5 Stream Negotiation Request (Type 3)

| Field                     | Size (bits) | Description                                                                                                          |
|---------------------------|-------------|----------------------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x03`.                                                                                                              |
| **Sequence Number**       | 16          | Next control message sequence.                                                                                       |
| **Fragment Sequence Num** | 16          | For fragmentation.                                                                                                   |
| **Flags**                 | 8           | Bits 0..1 for fragmentation.                                                                                         |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                                        |

**Payload**:
- 8-bit **Number of Streams (N)**
- For each requested stream:
    - **Stream ID** (8 bits)
    - **Flags** (8 bits):
        - bit 7 = `ordered`
        - bit 6 = `reliable`
        - bits 5..0 = reserved (0)
    - **Purpose Code** (16 bits) – optional usage

---

#### 3.1.6 Stream Negotiation Response (Type 4)

| Field                     | Size (bits) | Description                                                                                         |
|---------------------------|-------------|-----------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x04`.                                                                                             |
| **Sequence Number**       | 16          | Must match the corresponding Stream Negotiation Request.                                           |
| **Fragment Sequence Num** | 16          | For fragmentation.                                                                                  |
| **Flags**                 | 8           | Bits 0..1 for fragmentation.                                                                        |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** payload.                                                       |

**Payload**:
- 8-bit **Number of Streams (N)**
- For each **accepted** stream, the server echoes the details (ID, Flags, Purpose Code). Streams not echoed are considered refused.

---

#### 3.1.7 Heartbeat (Type 5)

| Field          | Size (bits) | Description         |
|----------------|-------------|---------------------|
| **Message Type** | 8         | `0x05`.             |

Heartbeat has no payload. (No length field is needed here.)

---

#### 3.1.8 Shutdown Request (Type 6)

| Field                     | Size (bits) | Description                                                     |
|---------------------------|-------------|-----------------------------------------------------------------|
| **Message Type**          | 8           | `0x06`.                                                         |
| **Sequence Number**       | 16          | Next control message sequence.                                  |

A shutdown request signals that the sender wants to end the session.

---

#### 3.1.9 Shutdown Ack (Type 7)

| Field               | Size (bits) | Description                                              |
|---------------------|-------------|----------------------------------------------------------|
| **Message Type**    | 8           | `0x07`.                                                  |
| **Sequence Number** | 16          | Must match the corresponding Stream Negotiation Request. |

No payload. Confirms receipt of the Shutdown Request.

---

### 3.2 Data Messages

Data messages can be sent once at least one stream is negotiated. Each stream may have its own sequence number space or share a global data message counter. In either case, the **Sequence Number** in data messages acts as the **message ID**. The first message ID SHOULD be chosen randomly for security, then increment by 1 for each message.

#### 3.2.1 Data Message (Type 10)

| Field                     | Size (bits) | Description                                                                                               |
|---------------------------|-------------|-----------------------------------------------------------------------------------------------------------|
| **Message Type**          | 8           | `0x0A`.                                                                                                   |
| **Sequence Number**       | 16          | Message ID for this stream (initially random, then incremented).                                          |
| **Fragment Sequence Num** | 16          | For fragmentation.                                                                                        |
| **Stream ID**             | 8           | 1–255, identifying which stream is used.                                                                  |
| **Flags**                 | 8           | Bits 0..1 for fragmentation.                                                                              |
| **Payload Length**        | 16          | Size in bytes of **this fragment’s** application data payload.                                            |

Payload is opaque to PFNP. Applications define the scope and structure (e.g., JSON, Protobuf).

Example uses:

```
Chat messages ({"type": "chat", "text": "Hello!"}).
Positional updates ({"x": 100, "y": 200}).
```

---

#### 3.2.2 Acknowledgement (Type 11)

| Field           | Size (bits) | Description                                                              |
|-----------------|-------------|--------------------------------------------------------------------------|
| **Message Type** | 8          | `0x0B`.                                                                  |
| **Stream ID**    | 8          | Which stream’s messages are acknowledged.                                |
| **Ack Count**    | 8          | Number of acknowledgment entries in the payload (each entry is 32 bits). |

**Payload**: Each acknowledgment entry (32 bits):
- **Start Message ID** (16 bits): The first sequence number in the acknowledged range.
- **End Message ID** (16 bits): The last sequence number in the acknowledged range.

Multiple entries allow acknowledging disjoint ranges if there are gaps (e.g., missing messages in an unreliable or partially reliable scenario).

---

### 3.3 Parameter Payload Format for Control Messages

Many control messages (e.g., Handshake, Authentication, Stream Negotiation) use key‑value parameters. The format is:

| Field          | Size (bits)        | Description                                                              |
|--------------- |--------------------|--------------------------------------------------------------------------|
| **Key Length** | 8                  | Number of bytes in the key name (1–255).                                 |
| **Key**        | (KeyLength × 8)    | UTF-8 text.                                                              |
| **Value Length** | 16               | Number of bytes in the value (0–65,535).                                 |
| **Value Type** | 8                  | E.g., 0x03 = string, 0x04 = binary, etc.                                 |
| **Value**      | (ValueLength × 8)  | The value data, in the format indicated by `Value Type`.                 |

---

## 4. Security and Encryption

During the handshake, the client and server decide if encryption is used (e.g., `"AES-GCM"`). If so, they may perform a Diffie-Hellman exchange (`"dh_public"`) to derive a shared key. After that, **all** messages (control and data) are encrypted. If the chosen cipher needs an IV/nonce per message, implementations must define how to generate or include it (e.g., derived from the Sequence Number or sent in a small header).

### 4.1 Encryption and Nonce Generation
When encryption is enabled (e.g., AES-GCM), a unique nonce MUST be generated for each encrypted message. The nonce is derived from existing session and message metadata to avoid header modifications:

#### A) Nonce Derivation Rule:
The 12-byte nonce is constructed as follows:

```
nonce = session_id (4 bytes) || sequence_number (2 bytes) || fragment_sequence_num (2 bytes) || 0x00000000 (4 bytes)
```

- session_id: The 32-bit session identifier from the Handshake Answer.

- sequence_number: The 16-bit message sequence number (control or data).

- fragment_sequence_num: The 16-bit fragment sequence number.

- The final 4 bytes are reserved (zero-padded).

Example:
For a message with:

- session_id = 0x89ABCDEF

- sequence_number = 0x1234

- fragment_sequence_num = 0x0001

- The nonce becomes:

```
89 AB CD EF 12 34 00 01 00 00 00 00  
``` 

#### B) Uniqueness Guarantee:

The combination of session_id, sequence_number, and fragment_sequence_num ensures uniqueness across all messages and fragments.

Even if sequence_number wraps around, the session_id (unique per connection) prevents nonce reuse.

#### C) Implementation Notes:

No Header Changes: The nonce is computed implicitly; no new fields are added to headers.

AES-GCM Requirements: The derived nonce MUST be used for both encryption and decryption.


---

## 5. Authentication

The handshake answer includes an `"auth_type"`. If it is `"none"`, the client proceeds without credentials. Otherwise, the client **must** send an **Authentication Request (Type 8)**. The server replies with an **Authentication Response (Type 9)** indicating success or failure. If more challenge-response rounds are required, these same message types are reused in multiple exchanges.

---

## 6. Session and Shutdown

A PFNP session encompasses handshake, optional authentication, stream negotiation, and subsequent data exchanges. Either side can issue a **Shutdown Request (Type 6)**, and the other side replies with **Shutdown Ack (Type 7)**, after which the connection ends.

---

## 7. Example: Full Session with Two-Round Digest Authentication and Two Streams

Below is a step-by-step illustration of a PFNP session. The client and server negotiate encryption, perform a two-round **digest** authentication (initiated by the **client**), open two streams (one reliable/ordered **chat** stream, one unreliable/unordered **game** stream), and exchange data.

### 7.1 Handshake

1. **Client → Server: Handshake Request (Type 1)**
    - **Header**
        - Message Type = 0x01
        - Sequence Number = 1000 (randomly chosen)
        - Fragment Sequence Number = 0
        - Flags = 0x03 (bits 0 and 1 → first & last fragment)
        - Payload Length = 46 (example size)
    - **Payload** (key‑value parameters):
        - `api_version = "1.2"`
        - `client_id = "AlicePC"`
        - `encryption = "AES-GCM"`
        - `dh_public = "base64_of_client_dh"`

2. **Server → Client: Handshake Answer (Type 2)**
    - **Header**
        - Message Type = 0x02
        - Sequence Number = 1000 (matching the request)
        - Fragment Sequence Number = 0
        - Flags = 0x04 (bit 2 = 0 → success, bits 0..1 = first & last fragment could be 0x03 or 0x04 depending on exact usage)
        - Payload Length = 52
    - **Payload**:
        - `dh_public = "base64_of_server_dh"`
        - `session_id = 0xA1B2C3D4`
        - `encryption = "AES-GCM"`
        - `auth_type = "digest"`

Both sides derive the shared key from the Diffie-Hellman values. All further messages are encrypted.

### 7.2 Two-Round Digest Authentication

Since `auth_type = "digest"`, the **client** starts by sending an Authentication Request, even though it doesn’t yet have a nonce or challenge from the server.

3. **Client → Server: Authentication Request (Type 8)**
    - **Header**
        - Message Type = 0x08
        - Sequence Number = 1001 (next control sequence for the client)
        - Fragment Sequence Number = 0
        - Flags = 0x03 (no fragmentation)
        - Payload Length = 24 (example)
    - **Payload** (key‑value):
        - `username = "Alice"`
        - `realm = "unknown"` (placeholder; client expects a challenge)

4. **Server → Client: Authentication Response (Type 9)**
    - **Header**
        - Message Type = 0x09
        - Sequence Number = 1001 (matches request)
        - Fragment Sequence Number = 0
        - Flags = 0x03
        - Payload Length = 34 (example)
    - **Payload**:
        - `auth_status = "challenge"`
        - `nonce = "abc123"`
        - `realm = "example-game"`

Now the client has the necessary challenge.

5. **Client → Server: Authentication Request (Type 8)** (second round)
    - **Header**
        - Message Type = 0x08
        - Sequence Number = 1002
        - Fragment Sequence Number = 0
        - Flags = 0x03
        - Payload Length = 42
    - **Payload**:
        - `username = "Alice"`
        - `realm = "example-game"`
        - `response = "digest_for_nonce_abc123"` (calculated digest)

6. **Server → Client: Authentication Response (Type 9)**
    - **Header**
        - Message Type = 0x09
        - Sequence Number = 1002
        - Fragment Sequence Number = 0
        - Flags = 0x03
        - Payload Length = 20
    - **Payload**:
        - `auth_status = "ok"`

Authentication is now successful.

### 7.3 Stream Negotiation

The client wants two streams:

- **Stream 1**: “chat,” which is reliable and ordered (bit 7=1, bit 6=1 → `0xC0`)
- **Stream 2**: “game,” which is unreliable and unordered (bit 7=0, bit 6=0 → `0x00`)

7. **Client → Server: Stream Negotiation Request (Type 3)**
    - **Header**
        - Message Type = 0x03
        - Sequence Number = 1003
        - Fragment Sequence Number = 0
        - Flags = 0x03
        - Payload Length = 8
    - **Payload**:
        - `Number of Streams = 2`
        1. Stream ID = 1, Flags = 0xC0, Purpose Code = 0x0001  (chat)
        2. Stream ID = 2, Flags = 0x00, Purpose Code = 0x0002  (game)

8. **Server → Client: Stream Negotiation Response (Type 4)**
    - **Header**
        - Message Type = 0x04
        - Sequence Number = 1003 (matching)
        - Fragment Sequence Number = 0
        - Flags = 0x03
        - Payload Length = 8
    - **Payload**:
        - `Number of Streams = 2`
            - Stream 1 (ID=1, Flags=0xC0, Purpose=0x0001)
            - Stream 2 (ID=2, Flags=0x00, Purpose=0x0002)

Both are accepted.

### 7.4 Data Exchange

Now the client can send data messages on the two streams.

9. **Client → Server: Data Message (Type 10)**
    - **Header**
        - Message Type = 0x0A
        - Sequence Number = 1 (for the chat stream)
        - Fragment Sequence Number = 0
        - Stream ID = 1
        - Flags = 0x03
        - Payload Length = 24
    - **Payload**:
        - `{"type": "chat", "text": "Hello!"}`
   
... and so on for the game stream.

### 7.5 Shutdown

When the client is done:

10. **Client → Server: Shutdown Request (Type 6)**
    - **Header**
        - Message Type = 0x06
        - Sequence Number = 1004 (next control message)
        - Fragment Sequence Number = 0
        - Flags = 0x03

11. **Server → Client: Shutdown Ack (Type 7)**
    - **Header**
        - Message Type = 0x07

Connection and session ends after the ack.