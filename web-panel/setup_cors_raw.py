#!/usr/bin/env python3
"""
Настройка CORS через raw HTTP запрос (для reg.ru Cloud)
Запуск: python setup_cors_raw.py
"""

import requests
import hashlib
import hmac
import base64
from datetime import datetime

# Конфигурация
ENDPOINT = "https://s3.regru.cloud"
ACCESS_KEY = "N8Z0ZYU4W3IHSGZKBBN5"
SECRET_KEY = "Yu7Z54MtphmMqXB0zOZSIaqWYCphil1gXOyywWKm"
BUCKET = "pet-photos"

# CORS конфигурация как XML
CORS_XML = """<?xml version="1.0" encoding="UTF-8"?>
<CORSConfiguration>
  <CORSRule>
    <AllowedOrigin>*</AllowedOrigin>
    <AllowedMethod>GET</AllowedMethod>
    <AllowedMethod>PUT</AllowedMethod>
    <AllowedMethod>POST</AllowedMethod>
    <AllowedMethod>DELETE</AllowedMethod>
    <AllowedMethod>HEAD</AllowedMethod>
    <AllowedMethod>OPTIONS</AllowedMethod>
    <AllowedHeader>*</AllowedHeader>
    <ExposeHeader>ETag</ExposeHeader>
    <MaxAgeSeconds>3600</MaxAgeSeconds>
  </CORSRule>
</CORSConfiguration>
"""

def sha256_hash(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def hmac_sha256(key: bytes, msg: str) -> bytes:
    return hmac.new(key, msg.encode('utf-8'), hashlib.sha256).digest()

def get_signature_key(key: str, date_stamp: str, region: str, service: str) -> bytes:
    k_date = hmac_sha256(('AWS4' + key).encode('utf-8'), date_stamp)
    k_region = hmac_sha256(k_date, region)
    k_service = hmac_sha256(k_region, service)
    return hmac_sha256(k_service, 'aws4_request')

def main():
    url = f"{ENDPOINT}/{BUCKET}?cors"
    
    now = datetime.utcnow()
    amz_date = now.strftime('%Y%m%dT%H%M%SZ')
    date_stamp = now.strftime('%Y%m%d')
    
    region = 'us-east-1'
    service = 's3'
    
    payload_hash = sha256_hash(CORS_XML.encode('utf-8'))
    
    canonical_request = f"""PUT
/{BUCKET}/cors

host:{ENDPOINT.replace('https://', '')}
x-amz-content-sha256:{payload_hash}
x-amz-date:{amz_date}

host;x-amz-content-sha256;x-amz-date
{payload_hash}"""

    credential_scope = f"{date_stamp}/{region}/{service}/aws4_request"
    string_to_sign = f"""AWS4-HMAC-SHA256
{amz_date}
{credential_scope}
{sha256_hash(canonical_request.encode('utf-8'))}"""

    signing_key = get_signature_key(SECRET_KEY, date_stamp, region, service)
    signature = hmac_sha256(signing_key, string_to_sign).hex()
    
    authorization = f"AWS4-HMAC-SHA256 Credential={ACCESS_KEY}/{credential_scope}, SignedHeaders=host;x-amz-content-sha256;x-amz-date, Signature={signature}"
    
    headers = {
        'Host': ENDPOINT.replace('https://', ''),
        'Content-Type': 'application/xml',
        'x-amz-content-sha256': payload_hash,
        'x-amz-date': amz_date,
        'Authorization': authorization,
    }
    
    print(f"Отправка CORS конфигурации в {url}...")
    
    try:
        response = requests.put(url, data=CORS_XML.encode('utf-8'), headers=headers, verify=False)
        
        if response.status_code == 200:
            print("✅ CORS успешно настроен!")
        else:
            print(f"❌ Ошибка: {response.status_code}")
            print(f"Ответ: {response.text[:500]}")
    except Exception as e:
        print(f"❌ Исключение: {e}")

if __name__ == '__main__':
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    main()
