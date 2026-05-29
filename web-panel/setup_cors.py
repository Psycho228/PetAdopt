#!/usr/bin/env python3
"""
Настройка CORS для bucket pet-photos в reg.ru Cloud S3
Запуск: python setup_cors.py
"""

import boto3
from botocore.config import Config
import urllib3

# Отключаем предупреждения о самоподписанном сертификате
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# Конфигурация
ENDPOINT = "https://s3.regru.cloud"
ACCESS_KEY = "N8Z0ZYU4W3IHSGZKBBN5"
SECRET_KEY = "Yu7Z54MtphmMqXB0zOZSIaqWYCphil1gXOyywWKm"
BUCKET = "pet-photos"

# CORS конфигурация (формат для reg.ru Cloud)
CORS_CONFIG = {
    'CORSRules': [
        {
            'AllowedOrigins': ['*'],
            'AllowedMethods': ['GET', 'PUT', 'POST', 'DELETE', 'HEAD', 'OPTIONS'],
            'AllowedHeaders': ['*'],
            'ExposeHeaders': ['ETag'],
            'MaxAgeSeconds': 3600
        }
    ]
}

def main():
    # Создаём клиент S3 с отключённой проверкой SSL
    s3 = boto3.client(
        's3',
        endpoint_url=ENDPOINT,
        aws_access_key_id=ACCESS_KEY,
        aws_secret_access_key=SECRET_KEY,
        config=Config(
            signature_version='s3v4',
            retries={'max_attempts': 3, 'mode': 'standard'}
        ),
        verify=False  # Отключаем проверку SSL
    )

    # Применяем CORS
    try:
        s3.put_bucket_cors(Bucket=BUCKET, CORSConfiguration=CORS_CONFIG)
        print(f"✅ CORS успешно настроен для bucket '{BUCKET}'")
        print(f"   Allowed Origins: {CORS_CONFIG['CORSRules'][0]['AllowedOrigins']}")
        print(f"   Allowed Methods: {CORS_CONFIG['CORSRules'][0]['AllowedMethods']}")
    except Exception as e:
        print(f"❌ Ошибка: {e}")

if __name__ == '__main__':
    main()
