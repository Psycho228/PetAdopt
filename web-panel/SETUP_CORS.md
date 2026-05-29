-- Настройка CORS для bucket pet-photos в reg.ru Cloud
-- Выполнить через curl (из PowerShell или CMD)

-- 1. Создать файл cors.json в папке web-panel:
-- Скопируй этот JSON в файл cors.json:
{
  "CORSRules": [
    {
      "AllowedOrigins": ["http://192.168.1.67:5173"],
      "AllowedMethods": ["GET", "PUT", "POST", "DELETE", "OPTIONS"],
      "AllowedHeaders": ["*"],
      "MaxAgeSeconds": 360
    }
  ]
}

-- 2. Выполнить curl команду (замени ключи на свои):
-- В PowerShell:
$accessKey = "N8Z0ZYU4W3IHSGZKBBN5"
$secretKey = "Yu7Z54MtphmMqXB0zOZSIaqWYCphil1gXOyywWKm"
$bucket = "pet-photos"
$endpoint = "https://s3.regru.cloud"

-- Если у тебя есть curl с поддержкой AWS SigV4 (например, из Git Bash):
-- curl -X PUT "$endpoint/$bucket" \
--   -H "Content-Type: application/json" \
--   -H "Authorization: AWS4-HMAC-SHA256 ..." \
--   -d @cors.json

-- Либо через панель reg.ru Cloud (рекомендуется):
-- 1. Зайди в https://cloud.reg.ru
-- 2. Object Storage → pet-photos → Настройки → CORS
-- 3. Добавь правило:
--    Allowed Origins: http://192.168.1.67:5173
--    Allowed Methods: GET, PUT, POST, DELETE, OPTIONS
--    Allowed Headers: *
--    Max Age: 360

