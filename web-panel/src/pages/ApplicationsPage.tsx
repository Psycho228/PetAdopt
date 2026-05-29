import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth } from '../contexts/AuthContext'
import type { Application } from '../lib/types'
import { getStatusDisplay, getStatusColor, formatDate } from '../lib/helpers'
import { Link } from 'react-router-dom'
import { Search, Filter, Eye } from 'lucide-react'

const statusOptions = [
  { value: 'all', label: 'Все' },
  { value: 'pending', label: 'Ожидают' },
  { value: 'processing', label: 'В работе' },
  { value: 'approved', label: 'Одобрены' },
  { value: 'rejected', label: 'Отклонены' },
]

export default function ApplicationsPage() {
  const { user } = useAuth()
  const [applications, setApplications] = useState<Application[]>([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('all')

  useEffect(() => {
    loadApplications()
  }, [])

  async function loadApplications() {
    setLoading(true)

    const { data: petIds } = await supabase
      .from('pets')
      .select('id')
      .eq('shelter_id', user!.id)

    const ids = (petIds ?? []).map((p) => p.id)
    if (ids.length === 0) {
      setApplications([])
      setLoading(false)
      return
    }

    const { data } = await supabase
      .from('applications')
      .select('*')
      .in('pet_id', ids)
      .order('created_at', { ascending: false })

    setApplications((data ?? []) as Application[])
    setLoading(false)
  }

  const filtered = applications.filter((a) => {
    if (statusFilter !== 'all' && a.status !== statusFilter) return false
    if (search) {
      const q = search.toLowerCase()
      return (
        a.user_name?.toLowerCase().includes(q) ||
        a.pet_name?.toLowerCase().includes(q) ||
        a.user_email?.toLowerCase().includes(q)
      )
    }
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
      <h2 className="text-2xl font-bold mb-6">Заявки</h2>

      <div className="flex flex-wrap items-center gap-3 mb-6">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            placeholder="Поиск по имени, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="px-4 py-2.5 rounded-xl border border-gray-200 bg-white focus:border-primary-500 outline-none cursor-pointer"
        >
          {statusOptions.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <p className="text-lg">Заявок не найдено</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100">
                  <th className="text-left px-5 py-3 font-medium text-gray-500">Питомец</th>
                  <th className="text-left px-5 py-3 font-medium text-gray-500">Заявитель</th>
                  <th className="text-left px-5 py-3 font-medium text-gray-500">Email</th>
                  <th className="text-left px-5 py-3 font-medium text-gray-500">Статус</th>
                  <th className="text-left px-5 py-3 font-medium text-gray-500">Дата</th>
                  <th className="text-right px-5 py-3 font-medium text-gray-500"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filtered.map((app) => (
                  <tr key={app.id} className="hover:bg-gray-50 transition">
                    <td className="px-5 py-3.5 font-medium text-gray-900">{app.pet_name}</td>
                    <td className="px-5 py-3.5">{app.user_name || '—'}</td>
                    <td className="px-5 py-3.5 text-gray-500">{app.user_email || '—'}</td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex text-xs font-medium px-2.5 py-1 rounded-full ${getStatusColor(app.status)}`}>
                        {getStatusDisplay(app.status)}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-gray-500">{formatDate(app.created_at)}</td>
                    <td className="px-5 py-3.5 text-right">
                      <Link
                        to={`/applications/${app.id}`}
                        className="inline-flex items-center gap-1.5 text-primary-600 hover:text-primary-700 font-medium text-sm px-3 py-1.5 rounded-lg hover:bg-primary-50 transition"
                      >
                        <Eye className="w-4 h-4" />
                        Подробнее
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
