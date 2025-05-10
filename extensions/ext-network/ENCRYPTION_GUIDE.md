Pseudo-code for the key derivation and nonce generation steps, annotated with **where** (client/server) and **when** (handshake phase) they occur:

---

### **Phase 1: Handshake Request (Client)**
```
# Client-side code during Handshake Request (Type 1) preparation:

# Generate ephemeral DH key pair (e.g., X25519)
client_private_key = generate_ephemeral_dh_private_key()
client_public_key = derive_public_key(client_private_key)

# Generate client nonce (16 bytes)
client_nonce = secure_random(16)

# Include in Handshake Request payload:
handshake_request_payload = {
    "api_version": "1.2",
    "client_id": "AlicePC",
    "encryption": "AES-GCM",
    "dh_public": base64_encode(client_public_key),
    "dh_group": "X25519",
    "kdf": "HKDF-SHA256",
    "client_nonce": base64_encode(client_nonce)
}
```

---

### **Phase 2: Handshake Answer (Server)**
```
# Server-side code after receiving Handshake Request:

# Generate ephemeral DH key pair
server_private_key = generate_ephemeral_dh_private_key()
server_public_key = derive_public_key(server_private_key)

# Generate server nonce (16 bytes)
server_nonce = secure_random(16)

# Compute shared secret using client's public key
shared_secret = dh_compute_shared_secret(
    server_private_key, 
    client_public_key  # from handshake request
)

# Generate session_id (32-bit)
session_id = secure_random(4)  # 4 bytes

# Include in Handshake Answer payload:
handshake_answer_payload = {
    "dh_public": base64_encode(server_public_key),
    "session_id": session_id,
    "encryption": "AES-GCM",
    "auth_type": "digest-hmac",
    "server_nonce": base64_encode(server_nonce),
    "kdf": "HKDF-SHA256",
    "signature": base64_encode(sign_handshake_params(...))  # optional
}
```

---

### **Phase 3: Post-Handshake Key Derivation (Client & Server)**
```
# Both client and server run this after exchanging nonces and DH public keys:

def derive_keys():
    # Inputs (available to both parties after handshake):
    # - shared_secret (from DH)
    # - client_nonce, server_nonce (exchanged)
    # - session_id (from server)
    # - KDF agreed (e.g., HKDF-SHA256)

    # HKDF-Extract step: combine nonces and shared secret
    salt = client_nonce + server_nonce  # concatenated 32 bytes
    initial_key = hkdf_extract(salt, shared_secret)

    # HKDF-Expand step: derive key material
    context = session_id + "PFNPv1.0_KeyDerivation"
    derived_key_material = hkdf_expand(initial_key, context, output_length=48)

    # Split into AES key and nonce base
    aes_key = derived_key_material[0:32]   # 32 bytes for AES-256
    nonce_base = derived_key_material[32:44]  # 12 bytes

    return aes_key, nonce_base

# Both sides call this:
aes_key, nonce_base = derive_keys()
```

---

### **Phase 4: Per-Message Nonce Generation (Client & Server)**
```
# Both client and server run this for every outgoing/incoming message:

def generate_nonce(sequence_num, fragment_seq_num):
    # Inputs:
    # - sequence_num (16-bit message sequence number)
    # - fragment_seq_num (16-bit fragment number)
    # - nonce_base (from key derivation)

    # Pack sequence and fragment numbers into 4 bytes:
    seq_fragment = pack(">HH", sequence_num, fragment_seq_num)  # 4 bytes total
    padded_seq_fragment = seq_fragment + bytes(4)  # 8 bytes total (pad with zeros)

    # XOR with nonce_base to create unique nonce
    nonce = xor(nonce_base, padded_seq_fragment[0:12])  # ensure 12-byte output

    return nonce

# Example usage for encryption:
sequence_num = 1000  # from message header
fragment_seq_num = 0
nonce = generate_nonce(sequence_num, fragment_seq_num)
ciphertext = aes_gcm_encrypt(aes_key, nonce, plaintext)
```

---

### **Key Points**:
1. **Ephemeral DH Keys**: Generated on both sides during handshake (client in Phase 1, server in Phase 2).
2. **Nonce Exchange**: `client_nonce` and `server_nonce` are exchanged in the handshake messages.
3. **Key Derivation**: Runs on **both** sides after the handshake completes (Phase 3).
4. **Nonce Generation**: Unique per-message nonce generated using sequence/fragment numbers (Phase 4, during encryption/decryption).
5. **Security**:
    - Forward secrecy ensured by ephemeral DH keys.
    - Nonce uniqueness guaranteed by XOR with incrementing sequence numbers.
    - Key derivation includes session-specific values (`session_id`, nonces) to prevent replay attacks.