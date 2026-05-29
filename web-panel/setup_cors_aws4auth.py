#!/usr/bin/env python3
"""
Настройка CORS через requests_aws4auth (правильная SigV4 подпись)
Запуск: python setup_cors_aws4auth.py
"""

import requests
from requests_aws4auth import AWS4Auth
import urllib3

# Отключаем предупреждения о SSL
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Конфигурация
ENDPOINT = "https://s3.regru.cloud"
ACCESS_KEY = "N8Z0ZYU4W3IHSGZKBBN5"
SECRET_KEY = "Yu7Z54MtphmMqXB0zOZSIaqWYCphil1gXOyywWKm"
BUCKET = "pet-photos"
REGION = "us-east-1"

# CORS конфигурация как XML
CORS_XML = """<?xml version="1.0" encoding="UTF-8"?>
<CORSConfiguration xmlns="http://s3.amazonaws.com/doc/2006-03-01/">
  <CORSRule>
    <AllowedOrigin>*</AllowedOrigin>
    <AllowedMethod>GET</AllowedMethod>
    <AllowedMethod>PUT</AllowedMethod>
    <AllowedMethod>POST</AllowedMethod>
    <AllowedMethod>DELETE</AllowedMethod>
    <AllowedMethod>HEAD</AllowedMethod>
    <AllowedHeader>*</AllowedHeader>
    <MaxAgeSeconds>3600</MaxAgeSeconds>
  </CORSRule>
</CORSConfiguration>
"""

def main():
    url = f"{ENDPOINT}/{BUCKET}?cors"
    
    # Создаём AWS4Auth
    auth = AWS4Auth(ACCESS_KEY, SECRET_KEY, REGION, 's3')
    
    headers = {
        'Content-Type': 'application/xml',
    }
    
    print(f"Отправка CORS конфигурации в {url}...")
    
    try:
        response = requests.put(
            url,
            data=CORS_XML,
            headers=headers,
            auth=auth,
            verify=False
        )
        
        if response.status_code == 200:
            print("✅ CORS успешно настроен!")
            print("   Разрешены все origins (*)")
            print("   Разрешены методы: GET, PUT, POST, DELETE, HEAD")
            print("   Max-Age: 3600 секунд")
        else:
            print(f"❌ Ошибка: {response.status_code}")
            print(f"Ответ: {response.text[:500]}")
    except Exception as e:
        print(f"❌ Исключение: {e}")

if __name__ == '__main__':
    main()
