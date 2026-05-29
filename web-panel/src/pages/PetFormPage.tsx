import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { uploadToS3 } from '../lib/s3'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate, useParams } from 'react-router-dom'
import type { Pet, PetType, PetGender, PetSize } from '../lib/types'
import { ArrowLeft, Save, Upload, X, Star, Trash2 } from 'lucide-react'

const types: { value: PetType; label: string }[] = [
  { value: 'dog', label: 'Собака' },
  { value: 'cat', label: 'Кошка' },
  { value: 'bird', label: 'Птица' },
  { value: 'other', label: 'Другое' },
]

const genders: { value: PetGender; label: string }[] = [
  { value: 'male', label: 'Мальчик' },
  { value: 'female', label: 'Девочка' },
]

const sizes: { value: PetSize; label: string }[] = [
  { value: 'small', label: 'Маленький' },
  { value: 'medium', label: 'Средний' },
  { value: 'large', label: 'Большой' },
]

export default function PetFormPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEdit = Boolean(id)

  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [photoFile, setPhotoFile] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState('')

  const [form, setForm] = useState<Partial<Pet>>({
    name: '',
    age: 0,
    type: 'cat',
    gender: 'male',
    size: 'medium',
    breed: '',
    color: '',
    weight: null,
    traits: [],
    description: '',
    photo_url: '',
    additional_photos: [],
    is_neutered: false,
    has_vaccination: false,
    is_active: true,
  })

  // Отслеживаем изменения photoPreview для отладки
  useEffect(() => {
    console.log('[PetForm] photoPreview changed:', photoPreview)
  }, [photoPreview])

  useEffect(() => {
    if (isEdit && id) {
      loadPet(id)
    }
  }, [id])

  async function loadPet(petId: string) {
    setLoading(true)
    const { data } = await supabase.from('pets').select('*').eq('id', petId).single()
    if (data) {
      setForm(data as Pet)
      setPhotoPreview(data.photo_url ?? '')
    }
    setLoading(false)
  }

  function handleChange(field: keyof Pet, value: any) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  function handlePhotoChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    if (!file) return

    console.log('[PetForm] Selected file:', file.name, 'size:', file.size)

    // Сброс input чтобы можно было выбрать тот же файл повторно
    e.target.value = ''

    const currentMain = form.photo_url
    if (currentMain) {
      // Перемещаем текущее главное фото в дополнительные
      const updated = [currentMain, ...(form.additional_photos ?? [])].filter(Boolean)
      console.log('[PetForm] Moving current main to additional:', updated)
      handleChange('additional_photos', updated)
    }

    setPhotoFile(file)
    
    // Используем FileReader для создания data URL вместо blob URL
    const reader = new FileReader()
    reader.onloadend = () => {
      const result = reader.result as string
      console.log('[PetForm] FileReader result length:', result?.length)
      setPhotoPreview(result)
    }
    reader.readAsDataURL(file)
  }

  async function uploadPhoto(file: File): Promise<string> {
    return await uploadToS3(file)
  }

  function makeMainPhoto(url: string) {
    const currentMain = form.photo_url
    const others = (form.additional_photos ?? []).filter((p) => p !== url)
    if (currentMain) {
      others.unshift(currentMain)
    }
    handleChange('photo_url', url)
    handleChange('additional_photos', others)
  }

  function removeAdditionalPhoto(url: string) {
    handleChange(
      'additional_photos',
      (form.additional_photos ?? []).filter((p) => p !== url)
    )
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    console.log('[PetForm] handleSubmit called')

    if (!user) {
      console.error('[PetForm] No user!')
      setError('Ошибка: пользователь не авторизован')
      return
    }

    setError('')
    setSaving(true)

    try {
      let photoUrl = form.photo_url ?? ''
      let additionalPhotos = [...(form.additional_photos ?? [])]
      console.log('[PetForm] Initial photoUrl:', photoUrl, 'photoFile:', photoFile?.name)

      if (photoFile) {
        console.log('[PetForm] Uploading photo to S3...')
        const newUrl = await uploadPhoto(photoFile)
        console.log('[PetForm] Photo uploaded to S3:', newUrl)

        // Если есть текущее главное фото — добавляем его в дополнительные
        if (photoUrl && photoUrl !== newUrl) {
          if (!additionalPhotos.includes(photoUrl)) {
            additionalPhotos.unshift(photoUrl)
          }
        }
        photoUrl = newUrl
      }

      const payload = {
        name: form.name,
        age: Number(form.age) || 0,
        type: form.type,
        gender: form.gender,
        size: form.size,
        breed: form.breed || '',
        color: form.color || '',
        weight: form.weight ? Number(form.weight) : null,
        traits: form.traits || [],
        description: form.description || '',
        photo_url: photoUrl,
        additional_photos: additionalPhotos,
        is_neutered: form.is_neutered ?? false,
        has_vaccination: form.has_vaccination ?? false,
        is_active: form.is_active ?? true,
        shelter_id: user.id,
      }

      console.log('[PetForm] Saving payload:', payload)

      if (isEdit && id) {
        const { error: saveErr } = await supabase.from('pets').update(payload).eq('id', id)
        if (saveErr) {
          console.error('[PetForm] Update error:', saveErr)
          throw saveErr
        }
        console.log('[PetForm] Update success')
      } else {
        const { error: saveErr } = await supabase.from('pets').insert(payload)
        if (saveErr) {
          console.error('[PetForm] Insert error:', saveErr)
          throw saveErr
        }
        console.log('[PetForm] Insert success')
      }

      navigate('/pets')
    } catch (err: any) {
      console.error('[PetForm] Error:', err)
      setError(err.message || 'Ошибка сохранения')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div>
      <button
        onClick={() => navigate('/pets')}
        className="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-4 transition"
      >
        <ArrowLeft className="w-5 h-5" />
        Назад к списку
      </button>

      <h2 className="text-2xl font-bold mb-6">{isEdit ? 'Редактировать питомца' : 'Добавить питомца'}</h2>

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 max-w-3xl">
        {error && <div className="bg-red-50 text-red-700 text-sm rounded-xl px-4 py-3 mb-4">{error}</div>}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Имя *</label>
            <input
              required
              value={form.name}
              onChange={(e) => handleChange('name', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
              placeholder="Барсик"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Возраст (лет) *</label>
            <input
              required
              type="number"
              min={0}
              max={30}
              value={form.age ?? ''}
              onChange={(e) => handleChange('age', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Вид *</label>
            <select
              required
              value={form.type}
              onChange={(e) => handleChange('type', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:border-primary-500 outline-none"
            >
              {types.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Пол *</label>
            <select
              required
              value={form.gender}
              onChange={(e) => handleChange('gender', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:border-primary-500 outline-none"
            >
              {genders.map((g) => (
                <option key={g.value} value={g.value}>
                  {g.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Размер</label>
            <select
              value={form.size}
              onChange={(e) => handleChange('size', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:border-primary-500 outline-none"
            >
              {sizes.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Порода</label>
            <input
              value={form.breed ?? ''}
              onChange={(e) => handleChange('breed', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
              placeholder="Без породы"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Окрас</label>
            <input
              value={form.color ?? ''}
              onChange={(e) => handleChange('color', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
              placeholder="Рыжий"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Вес (кг)</label>
            <input
              type="number"
              step="0.1"
              value={form.weight ?? ''}
              onChange={(e) => handleChange('weight', e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
              placeholder="3.5"
            />
          </div>
        </div>

        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-1">Черты (через запятую)</label>
          <input
            value={(form.traits ?? []).join(', ')}
            onChange={(e) => handleChange('traits', e.target.value.split(',').map((s) => s.trim()).filter(Boolean))}
            className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
            placeholder="Ласковый, активный, хорошо ходит на поводке"
          />
        </div>

        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
          <textarea
            rows={4}
            value={form.description ?? ''}
            onChange={(e) => handleChange('description', e.target.value)}
            className="w-full px-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition resize-none"
            placeholder="Расскажите о характере, привычках, истории питомца..."
          />
        </div>

        <div className="flex items-center gap-6 mb-6">
          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={form.is_neutered}
              onChange={(e) => handleChange('is_neutered', e.target.checked)}
              className="w-4 h-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
            />
            <span className="text-sm text-gray-700">Стерилизован</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={form.has_vaccination}
              onChange={(e) => handleChange('has_vaccination', e.target.checked)}
              className="w-4 h-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
            />
            <span className="text-sm text-gray-700">Привит</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={form.is_active}
              onChange={(e) => handleChange('is_active', e.target.checked)}
              className="w-4 h-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
            />
            <span className="text-sm text-gray-700">Активен (виден пользователям)</span>
          </label>
        </div>

        {/* Главное фото */}
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">Главное фото</label>
          <div className="flex items-center gap-4">
            {photoPreview ? (
              <div className="relative">
                <img 
                  src={photoPreview} 
                  alt="preview" 
                  className="w-32 h-32 object-cover rounded-xl"
                  onError={(e) => {
                    console.error('[PetForm] Image failed to load:', photoPreview?.slice(0, 50))
                    e.currentTarget.style.display = 'none'
                  }}
                />
                <div className="absolute top-1 left-1 bg-primary-500 text-white text-xs font-medium px-1.5 py-0.5 rounded-md flex items-center gap-0.5">
                  <Star className="w-3 h-3" />
                  Главное
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setPhotoFile(null)
                    setPhotoPreview('')
                    handleChange('photo_url', '')
                  }}
                  className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ) : (
              <div className="w-32 h-32 bg-gray-100 rounded-xl flex items-center justify-center text-gray-400 text-sm">
                Нет фото
              </div>
            )}
            <label className="flex flex-col items-center justify-center w-32 h-32 border-2 border-dashed border-gray-300 rounded-xl cursor-pointer hover:border-primary-500 hover:bg-primary-50 transition">
              <Upload className="w-6 h-6 text-gray-400 mb-1" />
              <span className="text-xs text-gray-500">Загрузить</span>
              <input type="file" accept="image/*" className="hidden" onChange={handlePhotoChange} />
            </label>
          </div>
          {photoFile && (
            <p className="text-xs text-gray-500 mt-2">Выбран файл: {photoFile.name}</p>
          )}
        </div>

        {/* Дополнительные фото */}
        {(form.additional_photos ?? []).length > 0 && (
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Дополнительные фото ({form.additional_photos?.length})
            </label>
            <div className="flex flex-wrap gap-3">
              {(form.additional_photos ?? []).map((url, index) => (
                <div key={index} className="relative group">
                  <img src={url} alt={`photo-${index}`} className="w-24 h-24 object-cover rounded-xl" />
                  <div className="absolute inset-0 bg-black/50 rounded-xl opacity-0 group-hover:opacity-100 transition flex items-center justify-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => makeMainPhoto(url)}
                      className="p-1.5 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition"
                      title="Сделать главным"
                    >
                      <Star className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={() => removeAdditionalPhoto(url)}
                      className="p-1.5 bg-red-500 text-white rounded-lg hover:bg-red-600 transition"
                      title="Удалить"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="flex items-center gap-3">
          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-300 text-white font-medium px-6 py-2.5 rounded-xl transition"
          >
            {saving ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Save className="w-5 h-5" />
            )}
            {isEdit ? 'Сохранить' : 'Добавить'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/pets')}
            className="px-6 py-2.5 rounded-xl border border-gray-200 text-gray-600 hover:bg-gray-50 transition"
          >
            Отмена
          </button>
        </div>
      </form>
    </div>
  )
}