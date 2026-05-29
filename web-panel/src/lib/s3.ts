import CryptoJS from 'crypto-js'

const S3_ACCESS_KEY = import.meta.env.VITE_S3_ACCESS_KEY
const S3_SECRET_KEY = import.meta.env.VITE_S3_SECRET_KEY
const S3_BUCKET = import.meta.env.VITE_S3_BUCKET_NAME
const S3_ENDPOINT = import.meta.env.VITE_S3_ENDPOINT_URL

function sha256(message: string): string {
  return CryptoJS.SHA256(message).toString(CryptoJS.enc.Hex)
}

function hmacSha256(key: string | CryptoJS.lib.WordArray, message: string): CryptoJS.lib.WordArray {
  return CryptoJS.HmacSHA256(message, key)
}

function getSignatureKey(secretKey: string, dateStamp: string, region: string, service: string): CryptoJS.lib.WordArray {
  const kDate = hmacSha256('AWS4' + secretKey, dateStamp)
  const kRegion = hmacSha256(kDate, region)
  const kService = hmacSha256(kRegion, service)
  const kSigning = hmacSha256(kService, 'aws4_request')
  return kSigning
}

/**
 * Загрузить файл в S3 с SigV4 подписью
 */
export async function uploadToS3(file: File): Promise<string> {
  if (!S3_ACCESS_KEY || !S3_SECRET_KEY || !S3_BUCKET || !S3_ENDPOINT) {
    throw new Error('S3 credentials not configured. Check VITE_S3_* variables in .env')
  }

  const ext = file.name.split('.').pop() || 'jpg'
  const key = `pets/${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`
  const url = `${S3_ENDPOINT}/${S3_BUCKET}/${key}`

  const now = new Date()
  const dateStamp = now.toISOString().slice(0, 10).replace(/-/g, '')
  const amzDate = dateStamp + 'T' + now.toISOString().slice(11, 19).replace(/:/g, '') + 'Z'

  const region = 'us-east-1'
  const service = 's3'

  // Читаем файл как base64 для payload hash
  const arrayBuffer = await file.arrayBuffer()
  const wordArray = CryptoJS.lib.WordArray.create(arrayBuffer as any)
  const payloadHash = CryptoJS.SHA256(wordArray).toString(CryptoJS.enc.Hex)

  const headers: Record<string, string> = {
    'host': new URL(S3_ENDPOINT).host,
    'x-amz-content-sha256': payloadHash,
    'x-amz-date': amzDate,
    'content-type': file.type || 'application/octet-stream',
  }

  const method = 'PUT'
  const canonicalUri = `/${S3_BUCKET}/${key}`
  const canonicalQueryString = ''

  const signedHeaders = Object.keys(headers).sort().join(';')
  const canonicalHeaders = Object.keys(headers)
    .sort()
    .map((k) => `${k.toLowerCase()}:${headers[k].trim()}\n`)
    .join('')

  const canonicalRequest = [
    method,
    canonicalUri,
    canonicalQueryString,
    canonicalHeaders,
    signedHeaders,
    payloadHash,
  ].join('\n')

  const credentialScope = `${dateStamp}/${region}/${service}/aws4_request`
  const stringToSign = [
    'AWS4-HMAC-SHA256',
    amzDate,
    credentialScope,
    sha256(canonicalRequest),
  ].join('\n')

  const signingKey = getSignatureKey(S3_SECRET_KEY, dateStamp, region, service)
  const signature = hmacSha256(signingKey, stringToSign).toString(CryptoJS.enc.Hex)

  const authorization = `AWS4-HMAC-SHA256 Credential=${S3_ACCESS_KEY}/${credentialScope}, SignedHeaders=${signedHeaders}, Signature=${signature}`

  const response = await fetch(url, {
    method: 'PUT',
    headers: {
      ...headers,
      Authorization: authorization,
    },
    body: arrayBuffer,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`S3 upload failed: ${response.status} ${response.statusText}\n${text}`)
  }

  return url
}
