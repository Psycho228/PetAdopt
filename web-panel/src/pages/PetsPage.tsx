import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth } from '../contexts/AuthContext'
import type { Pet } from '../lib/types'
import { getPetTypeDisplay, getGenderDisplay, getAgeDisplay } from '../lib/helpers'
import { Plus, Edit2, Archive, Search, Filter, Upload } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function PetsPage() {
  const { user } = useAuth()
  const [pets, setPets] = useState<Pet[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState('all')
  const [showInactive, setShowInactive] = useState(false)

  useEffect(() => {
    loadPets()
  }, [])

  async function loadPets() {
    setLoading(true)
    let query = supabase
      .from('pets')
      .select('*')
      .eq('shelter_id', user!.id)
      .order('created_at', { ascending: false })

    if (!showInactive) {
      query = query.eq('is_active', true)
    }

    const { data } = await query
    setPets((data ?? []) as Pet[])
    setLoading(false)
  }

  useEffect(() => {
    loadPets()
  }, [showInactive])

  async function toggleActive(pet: Pet) {
    const { error } = await supabase
      .from('pets')
      .update({ is_active: !pet.is_active })
      .eq('id', pet.id)

    if (!error) loadPets()
  }

  const filtered = pets.filter((p) => {
    if (typeFilter !== 'all' && p.type !== typeFilter) return false
    if (search && !p.name.toLowerCase().includes(search.toLowerCase())) return false
    return true
  })

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">Питомцы</h2>
        <div className="flex items-center gap-2">
          <Link
            to="/pets/import"
            className="flex items-center gap-2 bg-white border border-gray-200 hover:bg-gray-50 text-gray-700 font-medium px-4 py-2.5 rounded-xl transition"
          >
            <Upload className="w-5 h-5" />
            Импорт
          </Link>
          <Link
            to="/pets/new"
            className="flex items-center gap-2 bg-primary-600 hover:bg-primary-700 text-white font-medium px-4 py-2.5 rounded-xl transition"
          >
            <Plus className="w-5 h-5" />
            Добавить
          </Link>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-3 mb-6">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Поиск по имени..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
          />
        </div>

        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value)}
          className="px-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:border-primary-500 outline-none cursor-pointer"
        >
          <option value="all">Все виды</option>
          <option value="dog">Собаки</option>
          <option value="cat">Кошки</option>
          <option value="bird">Птицы</option>
          <option value="other">Другие</option>
        </select>

        <button
          onClick={() => setShowInactive(!showInactive)}
          className={`px-4 py-2.5 rounded-xl border transition ${
            showInactive
              ? 'bg-gray-100 border-gray-300 text-gray-700'
              : 'border-gray-200 text-gray-600 hover:bg-gray-50'
          }`}
        >
          <Filter className="w-4 h-4 inline mr-1" />
          {showInactive ? 'Все' : 'Активные'}
        </button>
      </div>

      {/* Pets grid */}
      {filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-lg">Питомцев не найдено</p>
          <p className="text-sm mt-1">Добавьте первого питомца, нажав кнопку выше</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((pet) => (
            <div
              key={pet.id}
              className={`bg-white rounded-2xl shadow-sm border overflow-hidden transition ${
                !pet.is_active ? 'opacity-60' : ''
              }`}
            >
              <div className="aspect-[4/3] bg-gray-100 relative">
                {pet.photo_url ? (
                  <img
                    src={pet.photo_url}
                    alt={pet.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-gray-300">
                    <svg className="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                )}
                {!pet.is_active && (
                  <div className="absolute top-3 right-3 bg-red-500 text-white text-xs font-medium px-2.5 py-1 rounded-full">
                    В архиве
                  </div>
                )}
              </div>

              <div className="p-4">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <h3 className="font-semibold text-lg">{pet.name}</h3>
                    <p className="text-sm text-gray-500">
                      {getPetTypeDisplay(pet.type)} · {getGenderDisplay(pet.gender)} · {getAgeDisplay(pet.age)}
                    </p>
                  </div>
                </div>

                {pet.traits && pet.traits.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 mb-3">
                    {pet.traits.slice(0, 4).map((t, i) => (
                      <span key={i} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">
                        {t}
                      </span>
                    ))}
                  </div>
                )}

                <div className="flex items-center gap-2 pt-3 border-t border-gray-100">
                  <Link
                    to={`/pets/${pet.id}`}
                    className="flex items-center gap-1.5 text-sm text-primary-600 hover:text-primary-700 font-medium px-3 py-1.5 rounded-lg hover:bg-primary-50 transition"
                  >
                    <Edit2 className="w-4 h-4" />
                    Ред.
                  </Link>
                  <button
                    onClick={() => toggleActive(pet)}
                    className={`flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-lg transition ${
                      pet.is_active
                        ? 'text-gray-500 hover:text-red-600 hover:bg-red-50'
                        : 'text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50'
                    }`}
                  >
                    <Archive className="w-4 h-4" />
                    {pet.is_active ? 'Архив' : 'Вернуть'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
