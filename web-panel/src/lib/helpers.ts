import { PetType, PetGender, PetSize, AppStatus, RiskLevel } from './types'

export function getPetTypeDisplay(type: PetType): string {
  const map: Record<PetType, string> = {
    dog: 'Собака',
    cat: 'Кошка',
    bird: 'Птица',
    other: 'Другое',
  }
  return map[type] ?? type
}

export function getGenderDisplay(gender: PetGender): string {
  const map: Record<PetGender, string> = {
    male: 'Мальчик',
    female: 'Девочка',
  }
  return map[gender] ?? gender
}

export function getSizeDisplay(size: PetSize): string {
  const map: Record<PetSize, string> = {
    small: 'Маленький',
    medium: 'Средний',
    large: 'Большой',
  }
  return map[size] ?? size
}

export function getStatusDisplay(status: AppStatus): string {
  const map: Record<AppStatus, string> = {
    pending: 'Ожидает',
    processing: 'В работе',
    approved: 'Одобрена',
    rejected: 'Отклонена',
  }
  return map[status] ?? status
}

export function getStatusColor(status: AppStatus): string {
  const map: Record<AppStatus, string> = {
    pending: 'bg-amber-100 text-amber-800',
    processing: 'bg-blue-100 text-blue-800',
    approved: 'bg-green-100 text-green-800',
    rejected: 'bg-red-100 text-red-800',
  }
  return map[status] ?? 'bg-gray-100 text-gray-800'
}

export function getRiskColor(level: RiskLevel): string {
  const map: Record<RiskLevel, string> = {
    LOW: 'text-green-600 bg-green-50',
    MEDIUM: 'text-amber-600 bg-amber-50',
    HIGH: 'text-orange-600 bg-orange-50',
    VERY_HIGH: 'text-red-600 bg-red-50',
  }
  return map[level] ?? ''
}

export function getRiskLabel(level: RiskLevel): string {
  const map: Record<RiskLevel, string> = {
    LOW: 'Низкий риск',
    MEDIUM: 'Средний риск',
    HIGH: 'Высокий риск',
    VERY_HIGH: 'Очень высокий риск',
  }
  return map[level] ?? level
}

export function getRecommendationLabel(rec: string): string {
  const map: Record<string, string> = {
    APPROVE: 'Можно одобрить',
    APPROVE_WITH_CONDITIONS: 'Одобрить с условиями',
    REVIEW_REQUIRED: 'Требуется проверка',
    REJECT: 'Рекомендуется отклонить',
  }
  return map[rec] ?? rec
}

export function formatDate(dateStr?: string): string {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long', year: 'numeric' })
}

export function formatDateTime(dateStr?: string): string {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function getInitials(name: string): string {
  return name
    .split(' ')
    .map((w) => w[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
}

export function getAgeDisplay(age: number): string {
  if (age === 0) return 'Менее года'
  const lastDigit = age % 10
  const lastTwo = age % 100
  if (lastTwo >= 11 && lastTwo <= 19) return `${age} лет`
  if (lastDigit === 1) return `${age} год`
  if (lastDigit >= 2 && lastDigit <= 4) return `${age} года`
  return `${age} лет`
}
