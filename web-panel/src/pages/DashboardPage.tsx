import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth } from '../contexts/AuthContext'
import type { DashboardStats } from '../lib/types'
import {
  PawPrint,
  ClipboardList,
  CheckCircle2,
  XCircle,
  Clock,
  TrendingUp,
  Users,
  Cat,
} from 'lucide-react'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [chartData, setChartData] = useState<{ date: string; count: number }[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboard()
  }, [])

  async function loadDashboard() {
    setLoading(true)
    const shelterId = user!.id

    const [
      { count: activePets },
      { count: totalPets },
      { data: apps, error: appsErr },
    ] = await Promise.all([
      supabase.from('pets').select('*', { count: 'exact', head: true }).eq('is_active', true).eq('shelter_id', shelterId),
      supabase.from('pets').select('*', { count: 'exact', head: true }).eq('shelter_id', shelterId),
      supabase.from('applications').select('*').in('pet_id', await getShelterPetIds(shelterId)),
    ])

    if (appsErr) console.error(appsErr)

    const appsList = (apps ?? []) as { status: string; created_at?: string }[]

    const grouped: Record<string, number> = {}
    appsList.forEach((a) => {
      const d = a.created_at?.slice(0, 10) ?? '?'
      grouped[d] = (grouped[d] ?? 0) + 1
    })
    const chart = Object.entries(grouped)
      .sort(([a], [b]) => a.localeCompare(b))
      .slice(-14)
      .map(([date, count]) => ({ date, count }))

    setStats({
      totalPets: totalPets ?? 0,
      activePets: activePets ?? 0,
      totalApplications: appsList.length,
      pendingApplications: appsList.filter((a) => a.status === 'pending').length,
      processingApplications: appsList.filter((a) => a.status === 'processing').length,
      approvedApplications: appsList.filter((a) => a.status === 'approved').length,
      rejectedApplications: appsList.filter((a) => a.status === 'rejected').length,
    })
    setChartData(chart)
    setLoading(false)
  }

  async function getShelterPetIds(shelterId: string): Promise<string[]> {
    const { data } = await supabase.from('pets').select('id').eq('shelter_id', shelterId)
    return (data ?? []).map((p) => p.id)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      </div>
    )
  }

  const cards = [
    { label: 'Всего питомцев', value: stats?.totalPets ?? 0, icon: PawPrint, color: 'bg-primary-100 text-primary-700' },
    { label: 'Активных', value: stats?.activePets ?? 0, icon: Cat, color: 'bg-emerald-100 text-emerald-600' },
    { label: 'Всего заявок', value: stats?.totalApplications ?? 0, icon: ClipboardList, color: 'bg-secondary-100 text-secondary-700' },
    { label: 'Ожидают', value: stats?.pendingApplications ?? 0, icon: Clock, color: 'bg-amber-100 text-amber-600' },
    { label: 'В работе', value: stats?.processingApplications ?? 0, icon: TrendingUp, color: 'bg-primary-100 text-primary-700' },
    { label: 'Одобрено', value: stats?.approvedApplications ?? 0, icon: CheckCircle2, color: 'bg-green-100 text-green-600' },
    { label: 'Отклонено', value: stats?.rejectedApplications ?? 0, icon: XCircle, color: 'bg-red-100 text-red-600' },
  ]

  return (
    <div>
      <div className="mb-6">
        <p className="text-sm font-semibold text-primary-700 mb-1">Хвостики</p>
        <h2 className="text-2xl font-bold text-gray-900">Обзор приюта</h2>
        <p className="text-sm text-gray-500 mt-1">Питомцы и заявки в одном рабочем пространстве</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-8">
        {cards.map((card) => (
          <div key={card.label} className="bg-warm-50 rounded-xl p-5 shadow-sm border border-warm-200">
            <div className={`inline-flex w-10 h-10 items-center justify-center rounded-xl ${card.color} mb-3`}>
              <card.icon className="w-5 h-5" />
            </div>
            <div className="text-2xl font-bold text-gray-900">{card.value}</div>
            <div className="text-sm text-gray-500 mt-0.5">{card.label}</div>
          </div>
        ))}
      </div>

      <div className="bg-warm-50 rounded-xl p-6 shadow-sm border border-warm-200">
        <h3 className="text-lg font-semibold mb-4">Заявки по дням (последние 14 дней)</h3>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#EDE6DB" />
              <XAxis dataKey="date" tick={{ fontSize: 12 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#2F7D6B" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : (
          <div className="text-gray-400 text-center py-12">Нет данных для отображения</div>
        )}
      </div>
    </div>
  )
}
