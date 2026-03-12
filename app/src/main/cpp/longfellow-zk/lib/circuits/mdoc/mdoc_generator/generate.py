import os
import hashlib
import cbor2
from cbor2 import CBORTag
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import utils as ec_utils

P256_ORDER = 0xffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551
NAMESPACE = "org.iso.18013.5.1"
CURRENT_DATE = "2024-05-20T12:00:00Z"

USER_DATA = {
    "given_name": "Raul",
    "family_name": "Gomez",
    "birth_date": "2001-09-20", 
    "age_over_18": False,
    "issue_date": "2023-01-01",
    "expiry_date": "2033-01-01",
    "document_number": "TFG-12345"
}

# ==========================================
# 1. Transcript 
# ==========================================
dummy_transcript_hash = bytes([0x55] * 32)
session_transcript = [None, None, dummy_transcript_hash]
dummy_transcript = cbor2.dumps(session_transcript, canonical=True)

# ==========================================
# 2. Claves Estáticas
# ==========================================
issuer_private_value = 0x1111111111111111111111111111111111111111111111111111111111111111
issuer_private_key = ec.derive_private_key(issuer_private_value, ec.SECP256R1())
pn = issuer_private_key.public_key().public_numbers()
pkx_hex = pn.x.to_bytes(32, 'big').hex()
pky_hex = pn.y.to_bytes(32, 'big').hex()

device_private_value = 0x2222222222222222222222222222222222222222222222222222222222222222
device_private_key = ec.derive_private_key(device_private_value, ec.SECP256R1())
dpn = device_private_key.public_key().public_numbers()

# ==========================================
# 3. Items y MSO 
# ==========================================
value_digests = {}
issuer_signed_items = []
digest_id = 0

for key, value in USER_DATA.items():
    item = {
        "digestID": digest_id,
        "random": bytes([digest_id] * 16),
        "elementIdentifier": key,
        "elementValue": value
    }
    item_bytes = cbor2.dumps(item, canonical=True)
    tagged_item_bytes = CBORTag(24, item_bytes)
    
    # FIX: Serializamos el Tag 24 completo y hasheamos ESTO, no el interior.
    tagged_bytes_serialized = cbor2.dumps(tagged_item_bytes)
    
    issuer_signed_items.append(tagged_item_bytes)
    value_digests[digest_id] = hashlib.sha256(tagged_bytes_serialized).digest() 
    digest_id += 1

mso = {
    "version": "1.0",
    "digestAlgorithm": "SHA-256",
    "valueDigests": { NAMESPACE: value_digests },
    "deviceKeyInfo": {
        "deviceKey": {
            1: 2, -1: 1, -2: dpn.x.to_bytes(32, 'big'), -3: dpn.y.to_bytes(32, 'big')
        }
    },
    "docType": "org.iso.18013.5.1.mDL",
    "validityInfo": {
        "signed": CBORTag(0, "2023-01-01T00:00:00Z"),
        "validFrom": CBORTag(0, "2023-01-01T00:00:00Z"),
        "validUntil": CBORTag(0, "2033-01-01T00:00:00Z")
    }
}

# ==========================================
# 4. Firma Issuer
# ==========================================
mso_bytes = cbor2.dumps(mso, canonical=True)
payload = cbor2.dumps(CBORTag(24, mso_bytes))

protected_header = cbor2.dumps({1: -7}) 
sig_structure = ["Signature1", protected_header, b"", payload]
sig_data = cbor2.dumps(sig_structure)

der_signature = issuer_private_key.sign(sig_data, ec.ECDSA(hashes.SHA256()))
r, s = ec_utils.decode_dss_signature(der_signature)
if s > P256_ORDER // 2: s = P256_ORDER - s  
raw_signature = r.to_bytes(32, 'big') + s.to_bytes(32, 'big')

issuer_auth = [protected_header, {}, payload, raw_signature]

# ==========================================
# 5. Firma Dispositivo 
# ==========================================
device_name_spaces_raw = cbor2.dumps({}) 
device_name_spaces_tagged = CBORTag(24, device_name_spaces_raw)

device_auth = [
    "DeviceAuthentication", 
    session_transcript,  
    "org.iso.18013.5.1.mDL", 
    device_name_spaces_tagged
]
device_auth_bytes = cbor2.dumps(device_auth, canonical=True)

da_tagged = CBORTag(24, device_auth_bytes)
da_payload = cbor2.dumps(da_tagged)

device_protected_header = cbor2.dumps({1: -7}) 
device_sig_structure = ["Signature1", device_protected_header, b"", da_payload]
device_sig_data = cbor2.dumps(device_sig_structure)

device_der_sig = device_private_key.sign(device_sig_data, ec.ECDSA(hashes.SHA256()))
dr, ds = ec_utils.decode_dss_signature(device_der_sig)
if ds > P256_ORDER // 2: ds = P256_ORDER - ds
device_raw_sig = dr.to_bytes(32, 'big') + ds.to_bytes(32, 'big')

device_signature = [device_protected_header, {}, None, device_raw_sig]

# ==========================================
# 6. Empaquetar y Exportar
# ==========================================
document = {
    "docType": "org.iso.18013.5.1.mDL",
    "issuerSigned": {
        "nameSpaces": { NAMESPACE: issuer_signed_items },
        "issuerAuth": issuer_auth
    },
    "deviceSigned": {
        "nameSpaces": device_name_spaces_tagged,
        "deviceAuth": { "deviceSignature": device_signature }
    }
}

final_mdoc_bytes = cbor2.dumps({"version": "1.0", "documents": [document], "status": 0}, canonical=True)

def to_c_array(byte_data):
    return ', '.join([f'0x{b:02x}' for b in byte_data])

cpp_code = f"""// AUTO-GENERADO POR PYTHON PARA EL TFG
#pragma once
#include <stddef.h>
#include <stdint.h>
#include "circuits/mdoc/mdoc_zk.h" 
#include "algebra/static_string.h"          
#include "circuits/mdoc/mdoc_attribute_ids.h" 

namespace custom_mock {{

const proofs::MdocTests mi_credencial = {{
    StaticString("0x{pkx_hex}"),  
    StaticString("0x{pky_hex}"),  
    {{ {to_c_array(dummy_transcript)} }}, 
    {len(dummy_transcript)},              
    (uint8_t*)"{CURRENT_DATE}",           
    kMDLDocType,                  
    {len(final_mdoc_bytes)},              
    {{ {to_c_array(final_mdoc_bytes)} }}  
}};

}} // namespace custom_mock
"""
print(cpp_code)