const XLSX = require('xlsx')
const fs = require('fs')
const path = require('path')

const dogs = [
  { name: 'Рекс', breed: 'Немецкая овчарка', color: 'Черно-коричневый', traits: 'Верный, защитник, умный' },
  { name: 'Белла', breed: 'Лабрадор', color: 'Золотистый', traits: 'Дружелюбная, активная, игривая' },
  { name: 'Шарик', breed: 'Без породы', color: 'Рыжий', traits: 'Ласковый, послушный, добрый' },
  { name: 'Мухтар', breed: 'Алабай', color: 'Белый', traits: 'Спокойный, защитник, самостоятельный' },
  { name: 'Дружок', breed: 'Хаски', color: 'Серо-белый', traits: 'Активный, общительный, энергичный' },
  { name: 'Джесси', breed: 'Корги', color: 'Рыже-белый', traits: 'Умная, веселая, преданная' },
  { name: 'Бобик', breed: 'Бигль', color: 'Триколор', traits: 'Любопытный, дружелюбный, активный' },
  { name: 'Лайка', breed: 'Без породы', color: 'Черный', traits: 'Верная, ласковая, послушная' },
  { name: 'Тузик', breed: 'Такса', color: 'Коричневый', traits: 'Храбрый, игривый, упрямый' },
  { name: 'Малыш', breed: 'Чихуахуа', color: 'Бежевый', traits: 'Миниатюрный, преданный, живой' },
]

const cats = [
  { name: 'Барсик', breed: 'Британская', color: 'Серый', traits: 'Спокойный, ласковый, домашний' },
  { name: 'Мурка', breed: 'Сиамская', color: 'Кремовый', traits: 'Говорливая, умная, общительная' },
  { name: 'Васька', breed: 'Без породы', color: 'Рыжий', traits: 'Независимый, игривый, любопытный' },
  { name: 'Снежок', breed: 'Турецкая ангора', color: 'Белый', traits: 'Нежный, ласковый, спокойный' },
  { name: 'Маркиз', breed: 'Мейн-кун', color: 'Черный мрамор', traits: 'Величественный, добрый, спокойный' },
  { name: 'Луна', breed: 'Шотландская вислоухая', color: 'Серебристая', traits: 'Тихая, ласковая, милая' },
  { name: 'Персик', breed: 'Персидская', color: 'Рыжий', traits: 'Спокойный, пушистый, домашний' },
  { name: 'Граф', breed: 'Бенгальская', color: 'Золотистый пятнистый', traits: 'Активный, умный, игривый' },
  { name: 'Матильда', breed: 'Рэгдолл', color: 'Сил-пойнт', traits: 'Нежная, ласковая, спокойная' },
  { name: 'Черныш', breed: 'Бомбейская', color: 'Черный', traits: 'Спокойный, преданный, тихий' },
]

const birds = [
  { name: 'Кеша', breed: 'Волнистый попугай', color: 'Зелено-желтый', traits: 'Говорливый, веселый, яркий' },
  { name: 'Гоша', breed: 'Корелла', color: 'Серый с желтым', traits: 'Дружелюбный, умный, ласковый' },
  { name: 'Арчи', breed: 'Жако', color: 'Серый', traits: 'Очень умный, говорливый, активный' },
]

const others = [
  { name: 'Хома', breed: 'Сирийский хомяк', color: 'Рыжий', traits: 'Ночной, милый, самостоятельный' },
  { name: 'Кролик', breed: 'Декоративный кролик', color: 'Белый', traits: 'Нежный, ласковый, пугливый' },
  { name: 'Моржик', breed: 'Морская свинка', color: 'Триколор', traits: 'Общительный, милый, громкий' },
  { name: 'Черепаха', breed: 'Красноухая черепаха', color: 'Зеленая', traits: 'Спокойная, долгожитель, неприхотливая' },
]

function generatePets() {
  const all = [
    ...dogs.map((d) => ({ ...d, type: 'dog', gender: Math.random() > 0.5 ? 'male' : 'female' })),
    ...cats.map((c) => ({ ...c, type: 'cat', gender: Math.random() > 0.5 ? 'male' : 'female' })),
    ...birds.map((b) => ({ ...b, type: 'bird', gender: Math.random() > 0.5 ? 'male' : 'female' })),
    ...others.map((o) => ({ ...o, type: 'other', gender: Math.random() > 0.5 ? 'male' : 'female' })),
  ]

  return all.map((pet, index) => ({
    name: pet.name,
    age: Math.floor(Math.random() * 8) + 1,
    type: pet.type,
    gender: pet.gender,
    size: pet.type === 'dog' ? (Math.random() > 0.5 ? 'large' : 'medium') : pet.type === 'cat' ? 'small' : 'small',
    breed: pet.breed,
    color: pet.color,
    weight: pet.type === 'dog' ? (Math.random() * 30 + 5).toFixed(1) : pet.type === 'cat' ? (Math.random() * 5 + 2).toFixed(1) : (Math.random() * 0.5 + 0.1).toFixed(2),
    traits: pet.traits,
    description: `${pet.name} — ${pet.breed.toLowerCase()}, ${pet.color.toLowerCase()} окрас. ${pet.traits.toLowerCase()}. Ищет любящую семью.`,
    photo_filename: `pet_${String(index + 1).padStart(2, '0')}.jpg`,
    is_neutered: Math.random() > 0.3,
    has_vaccination: Math.random() > 0.2,
  }))
}

function createXlsx() {
  const pets = generatePets()
  
  const worksheet = XLSX.utils.json_to_sheet(pets)
  
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Питомцы')
  
  const outputPath = path.join(__dirname, 'import_template.xlsx')
  XLSX.writeFile(workbook, outputPath)
  
  console.log(`✅ Создан файл: ${outputPath}`)
  console.log(`📊 Всего питомцев: ${pets.length}`)
  console.log(`🐕 Собак: ${pets.filter(p => p.type === 'dog').length}`)
  console.log(`🐈 Кошек: ${pets.filter(p => p.type === 'cat').length}`)
  console.log(`🦜 Птиц: ${pets.filter(p => p.type === 'bird').length}`)
  console.log(`🐹 Других: ${pets.filter(p => p.type === 'other').length}`)
}

createXlsx()
