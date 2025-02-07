# Pixel Framework Network Protocol (PFNP)

Version: 1.0 - Status: DRAFT

---

## 1. Introduction

The Pixel Framework Network Protocol (PFNP) is a lightweight binary protocol designed for real‑time communication in
multiplayer games. PFNP is engineered for efficiency, security, and adaptability. It supports dynamic key‑value
parameters, fragmentation, extended sequence numbering, and multiplexing of application data
over configurable streams. PFNP is transport‑agnostic (usable over UDP or TCP) and negotiates session parameters (
including optional encryption) during an initial handshake.

PFNP defines two main categories of messages:

- **Control Messages**: For establishing and managing the connection (handshake, authentication, stream negotiation,
  heartbeat, shutdown).
- **Data Messages**: For sending and receiving application-level payloads over negotiated streams (arbitrary data,
  acknowledgements).

If encryption is negotiated, all messages (control and data) **after** the handshake are encrypted at the connection
level.

---

## 2. Control and Data Messages

**Control messages** are **ordered** and share a single, global 16-bit sequence number space (Message ID). That number
is incremented (modulo `65,536`) for each new outgoing control message. The first control Message ID number SHOULD be
randomly generated for unpredictability.

Because sequence numbers are 16-bit, they eventually overflow. When a sequence number reaches 65,535 and increments, it
wraps around to 0. Implementations MUST handle wrap-around carefully (including for acknowledgements).

**Data messages** are transmitted over "application streams." Each stream maintains its own **16-bit Message ID sequence
numbering**, distinct from control messages.
Typically, a data stream’s first Message ID is randomly initialized when the stream is opened, then incremented by 1
for each new data message sent on that stream.
If a message is too large to fit within a single transmission, **fragmentation** is required. In such cases, **data
flags** indicate the **first** and **last** fragment. A message that is both the first and last fragment (i.e.,
not fragmented) will have both flags set to **1**.

Additionally, each fragment carries a **32-bit sequence number**, which increments with every new fragment, independent
of the Message ID. This means that while all fragments of a message share the same Message ID, each fragment has a
unique sequence number.

---

## 3. Message Formats

All messages have an 8-bit **Message Type**, plus other fields depending on the type. The **Flags** (4 bits) can serve
multiple purposes - more information is provided in the specific message types below.

Where a message has a **Payload Length** field, it indicates the size in **bytes** of that particular fragment’s
payload (not including headers).

### 3.1 Control Messages

---

### **3.1.1 Handshake Request (Type 1)**

| Field              | Size (bits) | Description                                                                                     |  
|--------------------|-------------|-------------------------------------------------------------------------------------------------|  
| **Message Type**   | 8           | `0x01`.                                                                                         |  
| **Message ID**     | 16          | Global control message sequence, randomly initialized for the session, incremented per message. |  
| **Payload Length** | 16          | Size in bytes of the payload.                                                                   |  

**Payload**: A set of key‑value parameters (Section 3.3).  
**Standard Parameters** (grouped by usage):

#### **Core Parameters (Mandatory)**

- `"api_version"` (string): PFNP version (e.g., `"1.0"`).
- `"encryption"` (string):
    - **Values**: `"none"`, or a comma-separated list of supported ciphers (e.g., `"AES-GCM,ChaCha20-Poly1305"`).
    - If `"none"`, PSK parameters **MUST** be provided.
- `"client_nonce"` (binary, 16 bytes): Client-generated nonce for replay protection.

#### **Encryption Parameters (Required if `encryption ≠ "none"`)**

- `"dh_public"` (binary): Client’s Diffie-Hellman public key.
- `"dh_group"` (string): DH group (e.g., `"X25519"`, `"P-256"`).
- `"kdf"` (string): Key derivation function (e.g., `"HKDF-SHA256"`).

#### **Pre-Shared Key (PSK) Parameters (Required if `encryption = "none"`)**

- `"psk_id"` (string): Identifier for a pre-shared key registered with the server.

#### **Optional Parameters**

- `"context"` (string): Application-specific context (e.g., `"game=my_game"`).

---

### **3.1.2 Handshake Answer (Type 2)**

| Field              | Size (bits) | Description                                   |  
|--------------------|-------------|-----------------------------------------------|  
| **Message Type**   | 8           | `0x02`.                                       |  
| **Message ID Ref** | 16          | Matches the Handshake Request’s `Message ID`. |  
| **Flags**          | 4           | Bit 0: Error (`1` = failure).                 |  
| **Payload Length** | 16          | Size in bytes of the payload.                 |  

**Payload**: Key-value parameters grouped by negotiated strategy:

#### **Core Parameters (Mandatory)**

- `"session_id"` (32-bit integer): Unique session identifier derived as:
  ```  
  session_id = HMAC(key=shared_secret_or_psk, data=client_nonce + server_nonce)[0:4]  
  ```  
- `"encryption"` (string): Selected cipher from the client’s list, or `"none"`.
- `"server_nonce"` (binary, 16 bytes): Server-generated nonce.

#### **Encryption Parameters (Included if `encryption ≠ "none"`)**

- `"dh_public"` (binary): Server’s Diffie-Hellman public key.
- `"kdf"` (string): Confirmed KDF (matches client’s proposal).
- `"signature"` (binary): Server’s signature over handshake parameters (e.g., `dh_public`, nonces).

#### **PSK Parameters (Included if `encryption = "none"`)**

- `"psk_id"` (string): Echoes the client’s `psk_id` if valid.

#### **Authentication Parameters**

- `"auth_type"` (string): Authentication method (e.g., `"none"`, `"digest-hmac"`).

#### **Error Handling**

If Bit 0 (error) is set:

- `"failure_reason"` (string): Error code (e.g., `"invalid_psk"`, `"unsupported_encryption"`).
- Additional parameters MAY clarify requirements (e.g., `"allowed_encryption"`, `"psk_hint"`).

---

#### 3.1.3 Authentication Request (Type 8)

| Field              | Size (bits) | Description                    |
|--------------------|-------------|--------------------------------|
| **Message Type**   | 8           | `0x08`.                        |
| **Session Id**     | 32          | Session identification         |
| **Message ID**     | 16          | Next control message sequence. |
| **Payload Length** | 16          | Size in bytes of the payload.  |

**Payload**: Key‑value parameters with credentials or tokens (depending on `auth_type`), for example:

- `"username"` (string)
- `"password"` (string)

The implementation is responsible for defining authentication type and parameter pairings.

---

#### 3.1.4 Authentication Response (Type 9)

| Field              | Size (bits) | Description                                       |
|--------------------|-------------|---------------------------------------------------|
| **Message Type**   | 8           | `0x09`.                                           |
| **Session Id**     | 32          | Session identification                            |
| **Message ID Ref** | 16          | Must match the Authentication Request’s sequence. |
| **Flags**          | 4           | Bit 0 is '1' if error. Others (reserved)          |
| **Payload Length** | 16          | Size in bytes of the payload.                     |

**Payload**: Key-value parameters indicating success or failure, e.g.:

- `"challenge"` if more rounds are needed
- `"session_token"` with a session token if successful
- `"failure_reason"` if authentication failed (optional)

The response may indicate an error state if the client provides incorrect credentials or if the server requires
additional challenge-response rounds for authentication.

---

#### 3.1.5 Stream Negotiation Request (Type 3)

| Field              | Size (bits) | Description                                   |
|--------------------|-------------|-----------------------------------------------|
| **Message Type**   | 8           | `0x03`.                                       |
| **Session Id**     | 32          | Session identification                        |
| **Message ID**     | 16          | Next control message sequence.                |
| **Payload Length** | 16          | Size in bytes of **this fragment’s** payload. |

**Payload**:

- For each requested stream:
    - **Stream ID** (8 bits)
    - **Flags** (8 bits):
        - bit 0 = `ordered`
        - bit 1 = `reliable`
        - bits 2-7 = reserved (0)
    - **Purpose Code** (8 bits) – optional usage

**Suggested purpose codes and flag definition**:

| Purpose Code | Data Type                 | Ordered | Reliable | Reasoning                                                                                                        |
|--------------|---------------------------|---------|----------|------------------------------------------------------------------------------------------------------------------|
| **0x01**     | **Player Position/State** | 1       | 0        | Requires ordering to avoid jitter, but loss-tolerant (new positions supersede old ones).                         |
| **0x02**     | **Critical Game Events**  | 0       | 1        | Events like player death or item pickup: must arrive but order isn't critical (e.g., "player died" is absolute). |
| **0x03**     | **Chat Messages**         | 1       | 1        | Strict ordering and reliability required for coherent conversation.                                              |
| **0x04**     | **File Transfer**         | 1       | 1        | Data integrity and sequencing are mandatory.                                                                     |
| **0x05**     | **Real-Time Telemetry**   | 0       | 0        | High-frequency sensor data where timeliness > completeness (e.g., racing games).                                 |
| **0x06**     | **Asset Chunks**          | 0       | 1        | Texture/model chunks need completeness but not order (parallel loading).                                         |
| **0x07**     | **Media (Audio, Video)**  | 0       | 0        | Prioritize low latency; late packets are useless.                                                                |
| **0x08**     | **Game State Snapshots**  | 1       | 1        | Full world state updates (e.g., RTS games) require both ordering and reliability.                                |
| **0x09**     | **Input Commands**        | 1       | 1        | Player inputs must arrive in sequence and reliably (e.g., fighting games).                                       |
| **0x0A**     | **Environmental Effects** | 0       | 0        | Non-critical events like weather/particles; loss and disorder are acceptable.                                    |
| **0x0B**     | **Matchmaking Signals**   | 1       | 1        | Lobby/party management requires strict sequencing and reliability.                                               |

The implementation is free to modify or add purpose codes or to reserve specific streams for internal use.

---

#### 3.1.6 Stream Negotiation Response (Type 4)

| Field              | Size (bits) | Description                                              |
|--------------------|-------------|----------------------------------------------------------|
| **Message Type**   | 8           | `0x04`.                                                  |
| **Session Id**     | 32          | Session identification                                   |
| **Message ID Ref** | 16          | Must match the corresponding Stream Negotiation Request. |
| **Payload Length** | 16          | Size in bytes of **this fragment’s** payload.            |

**Payload**:

- 8-bit **Number of Streams (N)**
- For each **accepted** stream, the server echoes the details (ID, Flags, Purpose Code). Streams not echoed are
  considered refused.

---

#### 3.1.7 Heartbeat (Type 5)

| Field            | Size (bits) | Description            |
|------------------|-------------|------------------------|
| **Message Type** | 8           | `0x05`.                |
| **Session Id**   | 32          | Session identification |
| **Message Id**   | 32          | Message ID             |

A **heartbeat message** has no payload and does not require a length field.

Heartbeat messages can be sent periodically by either side to maintain an active connection on idle streams. This serves
as a **keep-alive mechanism** for both **TCP** and **UDP** connections:

- **TCP**: Although TCP maintains connection state, some NAT devices and firewalls may terminate idle connections after
  a timeout. Periodic heartbeats serve as a **keep-alive mechanism**, ensuring the connection remains open and detecting
  unresponsive peers.

- **UDP**: Since UDP is **connectionless**, NAT devices rely on active traffic to maintain address mappings. Heartbeats
  prevent premature expiration of these mappings, facilitating **NAT traversal** and ensuring continued packet
  forwarding.

- **Heartbeat Period Configuration**: The interval between heartbeat messages **should be adaptive** based on network
  conditions and expected NAT/firewall timeouts. Implementations MAY allow configuration of this interval, with
  recommended values typically ranging from **15 to 60 seconds**. Shorter intervals improve responsiveness in detecting
  disconnections but may introduce unnecessary overhead.

---

#### 3.1.8 Shutdown Request (Type 6)

| Field              | Size (bits) | Description                    |
|--------------------|-------------|--------------------------------|
| **Message Type**   | 8           | `0x06`.                        |
| **Session Id**     | 32          | Session identification         |
| **Message ID**     | 16          | Next control message sequence. |
| **Payload Length** | 16          | Size in bytes of the payload.  |

A shutdown request signals that the sender wants to end the session gracefully. The receiver **MUST** reply with a
Shutdown Acknowledgement.

The payload MAY include an optional reason for the shutdown (e.g., `"maintenance"`, `"timeout"`). This reason can be
used for logging or debugging purposes and is encoded as a UTF-8 string. Implementations are free to define custom
reasons. For example, a shutdown timeout (e.g., `"shutdown_in: 30s"`) could indicate that the session is being closed
after a specific delay, effectively announcing the shutdown - useful for clients to display a countdown or inform users.

Either party may initiate the **Shutdown Request**, signaling the intent to close both the connection and the session.

---

#### 3.1.9 Shutdown Ack (Type 7)

| Field              | Size (bits) | Description                                    |
|--------------------|-------------|------------------------------------------------|
| **Message Type**   | 8           | `0x07`.                                        |
| **Session Id**     | 32          | Session identification                         |
| **Message ID Ref** | 16          | Must match the corresponding Shutdown Request. |

No payload. Confirms receipt of the Shutdown Request.

The **sender** SHOULD wait for the **Shutdown Ack** before closing the connection. The **sender** MAY perform necessary
cleanup operations before sending the **Shutdown Ack**. Once the **Shutdown Ack** is received, both parties should close
the connection and session.

The **sender** MAY choose to close the connection immediately after sending the **Shutdown Request** if the receiver
does not respond. In such cases, the sender SHOULD apply a reasonable timeout for receiving the **Shutdown Ack**.

---

### 3.2 Data Messages

Data messages are sent over negotiated streams. Each stream has a unique ID and manage its own sequence numbers.
The first message ID and sequence number for each stream are randomly chosen, then incremented for each message and
fragment, respectively.

#### 3.2.1 Data Message (Type 10)

| Field               | Size (bits) | Description                                                          |
|---------------------|-------------|----------------------------------------------------------------------|
| **Message Type**    | 8           | `0x0A`.                                                              |
| **Session Id**      | 32          | Session identification                                               |
| **Stream ID**       | 8           | 1–255, identifying which stream is used.                             |
| **Message ID**      | 16          | Identifies the data message.                                         |
| **Sequence Number** | 32          | Continuously increasing sequence number for this stream, per packet. |
| **Flags**           | 4           | Message flags.                                                       |
| **Payload Length**  | 16          | Size in bytes of the application data payload.                       |

**Flags**:

- Bit 0: First message fragment.
- Bit 1: Last message fragment.
- Bit 2: Ordered flag - Replicates the stream configuration, can be useful for stateless proxies.
- Bit 3: Reliable flag - Replicates the stream configuration, can be useful for stateless proxies.

Fragmentation is used for large payloads. The message ID and the flags are used to reassemble fragments. The payload
length is the size of the fragment’s payload, not the total message size. A message that is not fragmented, will have
both bits 0 and 1 set to 1 (first and last fragment).

Stateful proxies or receivers **MUST** prioritize the negotiated stream configuration over the fragment ordered and
reliable flags.

Payload is opaque. Applications define the scope and structure (e.g., JSON, plain-text).

Payload examples:

```
(1) Chat messages: chat|some_username|hello!
(2) Positional updates: pos|player_id|x=123.45,y=67.89
```

---

#### 3.2.2 Acknowledgement (Type 11)

| Field                               | Size (bits) | Description                                                  |
|-------------------------------------|-------------|--------------------------------------------------------------|
| **Message Type**                    | 8           | `0x0B`.                                                      |
| **Session Id**                      | 32          | Session identification                                       |
| **Stream ID**                       | 8           | Identifies the stream being acknowledged.                    |
| **Highest Ordered Sequence Number** | 32          | The highest sequence number received in order (without gaps) |
| **Payload Length**                  | 16          | Size in bytes of the acknowledgement payload.                |

This message acknowledges the receipt of data on a specified stream. It includes a field for the highest ordered
sequence number to provide immediate insight into the contiguous range of data received, and a payload that details
disjoint ranges for cases where gaps exist.

**Payload**: Each acknowledgment entry is 64 bits and represents a range of sequence numbers acknowledged outside the
contiguous sequence. The entries MUST be ordered by sequence number, older-first.

- **Start Sequence Number** (32 bits): The first sequence number in the acknowledged range.
- **End Sequence Number** (32 bits): The last sequence number in the acknowledged range.

Multiple acknowledgment entries can be included to cover disjoint ranges, which is especially useful in scenarios with
missing packets (e.g., in unreliable or partially reliable streams). If there are no gaps, the payload can be empty as
the highest ordered sequence number is sufficient.

Implementations should account for `bit overflow` when comparing sequence numbers. For example, if the highest ordered
sequence number is `4,294,967,296` and the next message is `0`, the implementation should recognize the sequence as
contiguous.

---

### **3.2.3 Ordered vs. Reliable Stream Behavior**

The `ordered` and `reliable` flags in stream negotiation are independent and govern distinct aspects of message
delivery:

| **Flag**   | **Meaning**                                                                    |  
|------------|--------------------------------------------------------------------------------|  
| `ordered`  | Messages **MUST** be delivered to the application in the order they were sent. |  
| `reliable` | Messages **MUST** be retransmitted until acknowledged (no silent drops).       |  

Note that a message in this context, is the sum of all fragments of a data message.

#### **Stream Type Combinations**

1. **Ordered + Reliable**:
    - **Behavior**:
        - Messages are retransmitted on loss (reliable).
        - The receiver holds out-of-order messages in a buffer until gaps are filled.
        - Delivery to the application is strictly in sequence.
    - **Use Case**: File transfers, chat messages.

2. **Ordered + Unreliable**:
    - **Behavior**:
        - Messages are **not** retransmitted on loss (unreliable).
        - The sender assigns sequential `seq` numbers to messages but does not retransmit gaps.
        - **Delivery rules**:
            - If a message arrives with `seq == expected_seq`: Deliver immediately, increment `expected_seq`.
            - If a message arrives with `seq > expected_seq`: Buffer it and start a **micro-timeout** (1–2 RTTs, e.g.,
              50ms) for the missing `expected_seq`.
                - If the gap is filled before timeout: Deliver messages in order.
                - If the timeout expires: Deliver all buffered messages with `seq ≥ expected_seq`, update `expected_seq`
                  to `highest_delivered_seq + 1`, and discard older buffered messages.
            - If a message arrives with `seq < expected_seq`: Discard (stale).
        - Ensures **monotonic delivery** (no older messages delivered after newer ones).
    - **Use Case**: Time-sensitive state updates where recentness > completeness (e.g., player positions, physics
      states). Avoids jitter from late packets while tolerating loss.

3. **Unordered + Reliable**:
    - **Behavior**:
        - Messages are retransmitted on loss (reliable).
        - The receiver delivers whole messages to the application immediately upon arrival, regardless of order.
    - **Use Case**: Non-linear data where completeness matters more than order (e.g., texture chunks).

4. **Unordered + Unreliable**:
    - **Behavior**:
        - Messages are delivered as they arrive, with no retransmissions or ordering guarantees.
    - **Use Case**: Low-latency, loss-tolerant applications such as VoiP.

#### **Implementation Requirements**

- **Buffering**:
    - Ordered streams **MUST** buffer messages to enforce sequencing. Implementations SHOULD limit buffer sizes to
      prevent memory exhaustion (e.g., discard messages older than 5 seconds or N fragments).
- **Acknowledgements**:
    - For ordered streams, acknowledgements (Type 11) **SHOULD** use **cumulative ACKs** (e.g., "all messages up to
      seq=5 are received").
    - For unordered streams, acknowledgements **MAY** use **selective ACKs** (SACKs) to report individual received
      messages.
- **Sequence Numbers**:
    - Sequence numbers increment by 1 for every fragment sent on the stream, even if unreliable. This allows receivers
      to detect gaps.

#### Stream Type Reference**

| `ordered` | `reliable` | Retransmits? | Buffers Messages?        | 
|-----------|------------|--------------|--------------------------|
| Yes       | Yes        | Yes          | Yes                      |
| Yes       | No         | No           | Yes (with micro-timeout) | 
| No        | Yes        | Yes          | No                       | 
| No        | No         | No           | No                       |

---

### 3.3 Parameter Payload Format for Control Messages

Most control messages (e.g., Handshake, Authentication, Stream Negotiation) use key‑value parameters. The format is:

| Field            | Size (bits)       | Description                              |
|------------------|-------------------|------------------------------------------|
| **Key Length**   | 8                 | Number of bytes in the key name (1–255). |
| **Key**          | (KeyLength × 8)   | UTF-8 text.                              |
| **Value Length** | 16                | Number of bytes in the value (0–65,535). |
| **Value**        | (ValueLength × 8) | Opaque application data.                 |

---

## 4. Security and Encryption

#### **4.1 Key Derivation and Nonce Generation**

A. **Shared Secret**:

- Both parties compute the raw Diffie-Hellman shared secret using their private keys and the peer’s public key.
- **Ephemeral Keys**: DH keys MUST be ephemeral (generated per session) to ensure forward secrecy.

B. **Key Derivation Function (KDF)**:

- Keys and nonces are derived using the negotiated KDF (e.g., `HKDF-SHA256`).
- Inputs to the KDF:
    - `shared_secret` (raw DH output)
    - `session_id` (from Handshake Answer)
    - `client_nonce` + `server_nonce` (from handshake)
    - Context string: `"<MyGameName>"`

   ```
   derived_key_material = HKDF-Expand(
       HKDF-Extract(client_nonce + server_nonce, shared_secret),
       session_id + context_string
   )
   ```

C. **Key/Nonce Assignment**:

- AES-GCM Key: First 32 bytes of `derived_key_material`.
- **Nonce Base**: Next 12 bytes (used to construct per-message nonces).

D. **Per-Message Nonce**:

- The 12-byte nonce is constructed by combining the Message ID (16-bit) and Sequence Number (32-bit) to ensure
  uniqueness across all fragments and messages. The formula is:
  ```
  nonce = nonce_base XOR (session_id || message_id || sequence_number || 0x0000)  
  ```
- session_id: 32-bit session identifier (from the message header).
- message_id: 16-bit Message ID from the Data Message header (identifies the message).
- sequence_number: 32-bit Sequence Number from the Data Message header (incremented per fragment).
- 0x0000: 2-byte padding (16 bits) to fill the 12-byte nonce.

**Example:**

For a Data Message with:

- `session_id` = 0xA1B2C3D4
- `message_id` = 0x1234
- `sequence_number` = 0x56789ABC

The concatenated value is XORed with the 12-byte nonce_base to produce the final nonce:

```
A1 B2 C3 D4   12 34   56 78 9A BC   00 00  
^^^^^^^^^^^   ^^^^^   ^^^^^^^^^^^   ^^^^^  
session_id    msg_id  seq_num       padding  
```

#### **4.2 Server Authentication**

If encryption is enabled (`encryption ≠ "none"`):

1. **Signature Requirements**:
    - The server **MUST** include a `"signature"` in the Handshake Answer (Type 2).
    - The signature covers the following concatenated data (in order):
        - Server’s `dh_public` (raw bytes)
        - Client’s `client_nonce` (raw bytes)
        - Server’s `server_nonce` (raw bytes)
        - `session_id` (raw bytes)
        - `encryption` (UTF-8 string)
        - `kdf` (UTF-8 string)

2. **Client Validation**:
    - The client **SHOULD** verify the signature using the server’s public key (pre-configured or fetched via a trusted
      mechanism).
    - If the signature is missing, invalid, or covers tampered data, the client **MUST** abort the handshake and send a
      `"failure_reason"` of `"invalid_signature"`.

#### 4.3 Retransmission and Reliability

- For streams marked `reliable` (bit 6=1), implementations MUST retransmit unacknowledged messages after a timeout
  period (recommended default: 200ms with exponential backoff).

#### 4.4 Session ID Integrity Validation

1. **Encrypted Sessions**:
    - The `session_id` is derived from the DH shared secret. Tampering causes decryption failure.
2. **Unencrypted Sessions**:
    - The `session_id` is derived from a pre-shared key (PSK) and nonces.
    - Servers **MUST** validate that the `session_id` matches the recomputed HMAC of `client_nonce + server_nonce` using
      the PSK.

#### 4.5: PSK Requirements for Unencrypted Sessions**

- If `encryption="none"`, clients **MUST** provide a valid `psk_id` during the handshake.
- Servers **MUST** reject handshakes with `encryption="none"` if no PSK is configured.

---

## 5. Authentication

The handshake answer includes an `"auth_type"`.
If it is `"none"`, the client proceeds without credentials. Otherwise, the client **must** send an **Authentication
Request (Type 8)**.
The server replies with an **Authentication Response (Type 9)** indicating success or failure.
If more challenge-response rounds are required, these same message types are reused in multiple exchanges.

---

## 6. Session and Shutdown

A PFNP session encompasses handshake, optional authentication, stream negotiation, and subsequent data exchanges. Either
side can issue a **Shutdown Request (Type 6)**, and the other side replies with **Shutdown Ack (Type 7)**, after which
the connection ends.

---

## 7. Example: Full Session with Two-Round Digest Authentication and Two Streams

Below is a step-by-step illustration of a PFNP session. The client and server negotiate encryption, perform a two-round
**digest** authentication (initiated by the **client**), open two streams (one reliable/ordered **chat** stream, one
unreliable/unordered **game** stream), and exchange data.

### 7.1 Handshake

1. **Client → Server: Handshake Request (Type 1)**
    - **Header**
        - Message Type = 0x01
        - Message ID = 1000 (randomly chosen)
        - Payload Length = 46 (example size)
    - **Payload** (key‑value parameters):
        - `api_version = "1.2"`
        - `client_id = "AlicePC"`
        - `encryption = "AES-GCM"`
        - `dh_public = "base64_of_client_dh"`
        - `dh_group = "X25519"`
        - `client_nonce = "base64_random_16B"`
        - `kdf = "HKDF-SHA256"`

2. **Server → Client: Handshake Answer (Type 2)**
    - **Header**
        - Message Type = 0x02
        - Message ID = 1000 (matching the request)
        - Flags = 0x00
        - Payload Length = 52
    - **Payload**:
        - `dh_public = "base64_of_server_dh"`
        - `session_id = 0xA1B2C3D4`
        - `encryption = "AES-GCM"`
        - `auth_type = "digest"`
        - `server_nonce = "base64_random_16B"`
        - `kdf = "HKDF-SHA256"`
        - `signature = "base64_sig"` (Base64-encoded signature of the handshake parameters)

Both sides derive the shared key from the Diffie-Hellman values. All further messages are encrypted.

### 7.2 Two-Round Digest Authentication

Since `auth_type = "digest"`, the **client** starts by sending an Authentication Request, even though it doesn’t yet
have a nonce or challenge from the server.

3. **Client → Server: Authentication Request (Type 8)**
    - **Header**
        - Message Type = 0x08
        - Session ID = 0xA1B2C3D4
        - Message ID = 1001 (next control sequence for the client)
        - Payload Length = 24 (example)
    - **Payload** (key‑value):
        - `username = "Alice"`
        - `realm = "unknown"` (placeholder; client expects a challenge)

4. **Server → Client: Authentication Response (Type 9)**
    - **Header**
        - Message Type = 0x09
        - Session ID = 0xA1B2C3D4
        - Message ID = 1001 (matches request)
        - Flags = 0b1000 (error, challenge expected)
        - Payload Length = 34 (example)
    - **Payload**:
        - `auth_status = "challenge"`
        - `nonce = "abc123"`
        - `realm = "example-game"`

Now the client has the necessary challenge.

5. **Client → Server: Authentication Request (Type 8)** (second round)
    - **Header**
        - Message Type = 0x08
        - Session ID = 0xA1B2C3D4
        - Message ID = 1002
        - Payload Length = 42
    - **Payload**:
        - `username = "Alice"`
        - `realm = "example-game"`
        - `response = "digest_for_nonce_abc123"` (calculated digest)

6. **Server → Client: Authentication Response (Type 9)**
    - **Header**
        - Message Type = 0x09
        - Session ID = 0xA1B2C3D4
        - Message ID = 1002
        - Flags = 0b0000
        - Payload Length = 24
    - **Payload**:
        - `session_token = "..."`

Authentication is now successful.

### 7.3 Stream Negotiation

The client wants two streams:

- **Stream 1**: "game," which is unreliable and unordered
- **Stream 2**: "chat," which is reliable and ordered

7. **Client → Server: Stream Negotiation Request (Type 3)**
    - **Header**
        - Message Type = 0x03
        - Session ID = 0xA1B2C3D4
        - Message ID = 1003
        - Payload Length = 8
    - **Payload**:
        1. Stream ID = 1, Flags = 0x00, Purpose Code = 0x0001
        2. Stream ID = 2, Flags = 0x03, Purpose Code = 0x0002

8. **Server → Client: Stream Negotiation Response (Type 4)**
    - **Header**
        - Message Type = 0x04
        - Session ID = 0xA1B2C3D4
        - Sequence Number = 1003 (matching)
        - Payload Length = 8
    - **Payload**:
        - `Number of Streams = 2`
            - Stream 1 (ID=1, Flags=..., Purpose=...)
            - Stream 2 (ID=2, Flags=..., Purpose=...)

Both are accepted.

### 7.4 Data Exchange

Now the client can send data messages on the two streams.

9. **Client → Server: Data Message (Type 10)**
    - **Header**
        - Message Type = 0x0A
        - Session ID = 0xA1B2C3D4
        - Message ID = 1 (for the chat stream)
        - Sequence Number = 0
        - Stream ID = 2
        - Flags = 0b1100 (first and last fragment)
        - Payload Length = 24
    - **Payload**:
        - `{"type": "chat", "text": "Hello!"}`

... and so on for the game stream.

### 7.5 Shutdown

When the client is done:

10. **Client → Server: Shutdown Request (Type 6)**
    - **Header**
        - Message Type = 0x06
        - Session ID = 0xA1B2C3D4
        - Message ID = 1004 (next control message)

11. **Server → Client: Shutdown Ack (Type 7)**
    - **Header**
        - Message Type = 0x07
        - Session ID = 0xA1B2C3D4
        - Message ID = 1004 (matching request)

Connection and session ends after the ack.