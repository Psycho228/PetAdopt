const Jimp = require('jimp')
const fs = require('fs')
const path = require('path')
const JSZip = require('jszip')

const OUTPUT_DIR = path.join(__dirname, '..', 'scripts', 'photos')
const ZIP_PATH = path.join(__dirname, '..', 'scripts', 'photos.zip')

const pets = [
  { name: 'Рекс', type: 'dog' },
  { name: 'Белла', type: 'dog' },
  { name: 'Шарик', type: 'dog' },
  { name: 'Мухтар', type: 'dog' },
  { name: 'Дружок', type: 'dog' },
  { name: 'Джесси', type: 'dog' },
  { name: 'Бобик', type: 'dog' },
  { name: 'Лайка', type: 'dog' },
  { name: 'Тузик', type: 'dog' },
  { name: 'Малыш', type: 'dog' },
  { name: 'Барсик', type: 'cat' },
  { name: 'Мурка', type: 'cat' },
  { name: 'Васька', type: 'cat' },
  { name: 'Снежок', type: 'cat' },
  { name: 'Маркиз', type: 'cat' },
  { name: 'Луна', type: 'cat' },
  { name: 'Персик', type: 'cat' },
  { name: 'Граф', type: 'cat' },
  { name: 'Матильда', type: 'cat' },
  { name: 'Черныш', type: 'cat' },
  { name: 'Кеша', type: 'bird' },
  { name: 'Гоша', type: 'bird' },
  { name: 'Арчи', type: 'bird' },
  { name: 'Хома', type: 'other' },
  { name: 'Кролик', type: 'other' },
  { name: 'Моржик', type: 'other' },
  { name: 'Черепаха', type: 'other' },
]

const colors = [
  0xFF6B6B, 0x4ECDC4, 0x45B7D1, 0x96CEB4, 0xFFEAA7,
  0xDDA0DD, 0x98D8C8, 0xF7DC6F, 0xBB8FCE, 0x85C1E9,
  0xF8C471, 0x82E0AA, 0xF1948A, 0x85C1E9, 0xD7BDE2,
]

async function generateImage(pet, index) {
  const width = 400
  const height = 300
  const color = colors[index % colors.length]

  const image = await Jimp.create(width, height, color)

  // Рамка
  image.scan(0, 0, width, height, function (x, y, idx) {
    if (x < 10 || x >= width - 10 || y < 10 || y >= height - 10) {
      this.bitmap.data[idx] = 255
      this.bitmap.data[idx + 1] = 255
      this.bitmap.data[idx + 2] = 255
    }
  })

  // Текст
  const font = await Jimp.loadFont(Jimp.FONT_SANS_32_WHITE)
  const smallFont = await Jimp.loadFont(Jimp.FONT_SANS_16_WHITE)

  image.print(font, 0, 100, {
    text: pet.name,
    alignmentX: Jimp.HORIZONTAL_ALIGN_CENTER,
  }, width, 50)

  const typeLabel = pet.type === 'dog' ? 'Собака' : pet.type === 'cat' ? 'Кошка' : pet.type === 'bird' ? 'Птица' : 'Другой'
  image.print(smallFont, 0, 160, {
    text: typeLabel,
    alignmentX: Jimp.HORIZONTAL_ALIGN_CENTER,
  }, width, 30)

  // Сохраняем
  const filename = `pet_${String(index + 1).padStart(2, '0')}.jpg`
  const filepath = path.join(OUTPUT_DIR, filename)
  await image.quality(85).writeAsync(filepath)

  console.log(`  ${filename} — ${pet.name} (${typeLabel})`)
  return { filename, filepath }
}

async function main() {
  if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true })
  }

  // Очищаем
  const existing = fs.readdirSync(OUTPUT_DIR)
  for (const f of existing) {
    fs.unlinkSync(path.join(OUTPUT_DIR, f))
  }

  console.log('Генерация фото...')
  const files = []

  for (let i = 0; i < pets.length; i++) {
    const file = await generateImage(pets[i], i)
    files.push(file)
  }

  // ZIP
  console.log('\nСоздание ZIP...')
  const zip = new JSZip()

  for (const file of files) {
    const data = fs.readFileSync(file.filepath)
    zip.file(file.filename, data)
  }

  const zipBuffer = await zip.generateAsync({ type: 'nodebuffer' })
  fs.writeFileSync(ZIP_PATH, zipBuffer)

  console.log(`\n✅ ZIP создан: ${ZIP_PATH}`)
  console.log(`📦 Размер: ${(zipBuffer.length / 1024).toFixed(1)} KB`)
  console.log(`📸 Всего фото: ${files.length}`)
}

main().catch(console.error)