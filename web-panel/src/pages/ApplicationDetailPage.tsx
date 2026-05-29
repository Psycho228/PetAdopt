import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { useNavigate, useParams } from 'react-router-dom'
import type { Application, QuestionnaireAnswer, RiskAssessment } from '../lib/types'
import {
  getStatusDisplay,
  getStatusColor,
  formatDateTime,
  getRiskLabel,
  getRiskColor,
  getRecommendationLabel,
} from '../lib/helpers'
import Chat from '../components/Chat'
import {
  ArrowLeft,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  ShieldCheck,
  User,
  Mail,
  Phone,
  Clock,
  Calendar,
  PawPrint,
  FileText,
  MessageSquare,
} from 'lucide-react'

export default function ApplicationDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [app, setApp] = useState<Application | null>(null)
  const [questionnaire, setQuestionnaire] = useState<QuestionnaireAnswer | null>(null)
  const [risk, setRisk] = useState<RiskAssessment | null>(null)
  const [currentUser, setCurrentUser] = useState<{ id: string } | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)

  useEffect(() => {
    getCurrentUser()
    if (id) loadData()
  }, [id])

  async function getCurrentUser() {
    const { data: { user } } = await supabase.auth.getUser()
    if (user) {
      setCurrentUser({ id: user.id })
    }
  }

  async function loadData() {
    setLoading(true)
    const { data: appData } = await supabase.from('applications').select('*').eq('id', id).single()
    if (!appData) {
      setLoading(false)
      return
    }
    const a = appData as Application
    setApp(a)

    const [{ data: qData }, { data: rData }] = await Promise.all([
      supabase.from('questionnaire_answers').select('*').eq('user_id', a.user_id).single(),
      supabase.from('risk_assessments').select('*').eq('user_id', a.user_id).order('created_at', { ascending: false }).limit(1).single(),
    ])

    setQuestionnaire((qData as QuestionnaireAnswer | null) ?? null)
    setRisk((rData as RiskAssessment | null) ?? null)
    setLoading(false)
  }

  async function updateStatus(status: 'approved' | 'rejected') {
    setUpdating(true)
    const { error } = await supabase.from('applications').update({ status }).eq('id', id)
    if (!error) await loadData()
    setUpdating(false)
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      </div>
    )
  }

  if (!app) {
    return (
      <div className="text-center py-16">
        <p className="text-lg text-gray-500">Заявка не найдена</p>
        <button
          onClick={() => navigate('/applications')}
          className="mt-4 text-primary-600 hover:text-primary-700 font-medium"
        >
          К списку заявок
        </button>
      </div>
    )
  }

  const riskLevel = (risk?.overallRisk as any) ?? ''

  return (
    <div>
      <button
        onClick={() => navigate('/applications')}
        className="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-4 transition"
      >
        <ArrowLeft className="w-5 h-5" />
        Назад
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Status card */}
          <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
            <div className="flex items-start justify-between mb-4">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h2 className="text-xl font-bold">Заявка на {app.pet_name}</h2>
                  <span className={`inline-flex text-xs font-medium px-2.5 py-1 rounded-full ${getStatusColor(app.status)}`}>
                    {getStatusDisplay(app.status)}
                  </span>
                </div>
                <p className="text-sm text-gray-500">Подана {formatDateTime(app.created_at)}</p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex items-center gap-3 bg-gray-50 rounded-xl p-3">
                <User className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Заявитель</p>
                  <p className="text-sm font-medium">{app.user_name || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 bg-gray-50 rounded-xl p-3">
                <Mail className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Email</p>
                  <p className="text-sm font-medium">{app.user_email || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 bg-gray-50 rounded-xl p-3">
                <Clock className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Удобное время</p>
                  <p className="text-sm font-medium">{app.contact_time || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 bg-gray-50 rounded-xl p-3">
                <Calendar className="w-5 h-5 text-gray-400" />
                <div>
                  <p className="text-xs text-gray-500">Удобные дни</p>
                  <p className="text-sm font-medium">{app.contact_days || '—'}</p>
                </div>
              </div>
            </div>

            {app.message && (
              <div className="mt-4 bg-primary-50 rounded-xl p-4">
                <p className="text-xs font-medium text-primary-700 mb-1">Сообщение от заявителя</p>
                <p className="text-sm text-gray-700">{app.message}</p>
              </div>
            )}

            {app.status !== 'approved' && app.status !== 'rejected' && (
              <div className="flex items-center gap-3 mt-6 pt-4 border-t border-gray-100">
                <button
                  onClick={() => updateStatus('approved')}
                  disabled={updating}
                  className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 disabled:bg-emerald-300 text-white font-medium px-5 py-2.5 rounded-xl transition"
                >
                  <CheckCircle2 className="w-5 h-5" />
                  Одобрить
                </button>
                <button
                  onClick={() => updateStatus('rejected')}
                  disabled={updating}
                  className="flex items-center gap-2 bg-red-600 hover:bg-red-700 disabled:bg-red-300 text-white font-medium px-5 py-2.5 rounded-xl transition"
                >
                  <XCircle className="w-5 h-5" />
                  Отклонить
                </button>
                {currentUser && (
                  <button
                    onClick={() => {
                      // Открыть чат в модальном окне или отдельном блоке
                      const chatElement = document.getElementById('application-chat')
                      chatElement?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                    }}
                    className="flex items-center gap-2 bg-primary-600 hover:bg-primary-700 text-white font-medium px-5 py-2.5 rounded-xl transition"
                  >
                    <MessageSquare className="w-5 h-5" />
                    Чат
                  </button>
                )}
              </div>
            )}
          </div>

          {/* Questionnaire */}
          {questionnaire ? (
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
              <div className="flex items-center gap-2 mb-4">
                <FileText className="w-5 h-5 text-primary-600" />
                <h3 className="text-lg font-semibold">Анкета заявителя</h3>
              </div>

              <div className="space-y-6">
                {/* Section 1 */}
                <QuestionnaireSection title="Основная информация">
                  <QField label="ФИО" value={questionnaire.q1_full_name} />
                  <QField label="Возраст" value={questionnaire.q1_age ? `${questionnaire.q1_age} лет` : undefined} />
                  <QField label="Город" value={questionnaire.q1_city} />
                  <QField label="Род занятий" value={questionnaire.q1_occupation} />
                  <QField label="Способ связи" value={questionnaire.q1_contact_method} />
                </QuestionnaireSection>

                {/* Section 2 */}
                <QuestionnaireSection title="Жилищные условия">
                  <QField label="Тип жилья" value={questionnaire.q2_housing_type} />
                  <QField label="Разрешены ли животные" value={boolLabel(questionnaire.q2_pets_allowed)} />
                  <QField label="С кем живёт" value={arrayLabel(questionnaire.q2_living_with)} />
                  <QField label="Согласие семьи" value={boolLabel(questionnaire.q2_family_consent)} />
                  <QField label="Есть дети" value={boolLabel(questionnaire.q2_has_children)} />
                  <QField label="Возраст детей" value={questionnaire.q2_children_ages} />
                  <QField label="Другие животные" value={boolLabel(questionnaire.q2_has_other_pets)} />
                  <QField label="Какие животные" value={arrayLabel(questionnaire.q2_other_pets_types)} />
                  <QField label="Часов одному" value={questionnaire.q2_hours_alone} />
                  <QField label="Кто ухаживает" value={questionnaire.q2_caregiver} />
                </QuestionnaireSection>

                {/* Section 3 */}
                <QuestionnaireSection title="Опыт с животными">
                  <QField label="Были питомцы раньше" value={boolLabel(questionnaire.q3_had_pets_before)} />
                  <QField label="Что с ними стало" value={questionnaire.q3_what_happened} />
                  <QField label="Опыт с собаками" value={boolLabel(questionnaire.q3_dog_experience)} />
                  <QField label="Опыт с кошками" value={boolLabel(questionnaire.q3_cat_experience)} />
                  <QField label="Опыт с особенными" value={boolLabel(questionnaire.q3_special_needs_experience)} />
                  <QField label="Почему сейчас" value={questionnaire.q3_why_now} />
                </QuestionnaireSection>

                {/* Section 4 */}
                <QuestionnaireSection title="Ответственность">
                  <QField label="Понимает требования" value={boolLabel(questionnaire.q4_understand_requirements)} />
                  <QField label="Понимает время" value={boolLabel(questionnaire.q4_understand_time)} />
                  <QField label="Понимает внимание" value={boolLabel(questionnaire.q4_understand_attention)} />
                  <QField label="Понимает обучение" value={boolLabel(questionnaire.q4_understand_training)} />
                  <QField label="Понимает ветпомощь" value={boolLabel(questionnaire.q4_understand_vet_care)} />
                  <QField label="Готов к расходам" value={boolLabel(questionnaire.q4_ready_expenses)} />
                  <QField label="Что делать с мебелью" value={questionnaire.q4_furniture_damage_plan} />
                  <QField label="Что делать со шумом" value={questionnaire.q4_noise_plan} />
                  <QField label="Что делать с пугливым" value={questionnaire.q4_shy_pet_plan} />
                  <QField label="Долгая адаптация" value={questionnaire.q4_long_adaptation_plan} />
                  <QField label="Готов к воспитанию" value={boolLabel(questionnaire.q4_ready_education)} />
                  <QField label="Изменения в жизни" value={questionnaire.q4_life_changes_plan} />
                  <QField label="Препятствия в году" value={questionnaire.q4_obstacles_next_year} />
                </QuestionnaireSection>

                {/* Section 5 */}
                <QuestionnaireSection title="Безопасность">
                  <QField label="Меры безопасности" value={arrayLabel(questionnaire.q5_safety_measures)} />
                  <QField label="Готов к стерилизации" value={boolLabel(questionnaire.q5_ready_neuter)} />
                  <QField label="Готов следовать рекомендациям" value={boolLabel(questionnaire.q5_ready_recommendations)} />
                  <QField label="Готов к адреснику" value={boolLabel(questionnaire.q5_ready_tracker)} />
                  <QField label="Готов поддерживать связь" value={boolLabel(questionnaire.q5_ready_keep_contact)} />
                </QuestionnaireSection>

                {/* Section 6 */}
                <QuestionnaireSection title="Эмоциональная часть">
                  <QField label="Ответственный хозяин — это" value={questionnaire.q6_responsible_owner_meaning} />
                  <QField label="Жизнь с питомцем" value={questionnaire.q6_life_with_pet_vision} />
                  <QField label="Почему хороший хозяин" value={questionnaire.q6_why_good_owner} />
                </QuestionnaireSection>

                {/* Section 7 */}
                <QuestionnaireSection title="Желаемые виды">
                  <QField label="Интересуют" value={arrayLabel(questionnaire.q7_desired_pets)} />
                </QuestionnaireSection>
              </div>

              {/* Chat section */}
              {currentUser && (
                <div id="application-chat" className="mt-6">
                  <Chat applicationId={app.id} currentUserId={currentUser.id} />
                </div>
              )}
            </div>
          ) : (
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 text-center text-gray-400">
              <FileText className="w-8 h-8 mx-auto mb-2 opacity-50" />
              <p>Анкета не заполнена</p>
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Risk assessment */}
          {risk ? (
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
              <div className="flex items-center gap-2 mb-4">
                <ShieldCheck className="w-5 h-5 text-primary-600" />
                <h3 className="font-semibold">Оценка рисков</h3>
              </div>

              <div className={`rounded-xl p-4 mb-4 ${getRiskColor(riskLevel)}`}>
                <div className="flex items-center justify-between mb-1">
                  <span className="text-sm font-medium">{getRiskLabel(riskLevel)}</span>
                  <span className="text-sm font-bold">{risk.riskScore}/100</span>
                </div>
                <div className="w-full bg-white/50 rounded-full h-2">
                  <div
                    className="h-2 rounded-full transition-all"
                    style={{
                      width: `${risk.riskScore}%`,
                      backgroundColor:
                        riskLevel === 'LOW'
                          ? '#16a34a'
                          : riskLevel === 'MEDIUM'
                          ? '#d97706'
                          : riskLevel === 'HIGH'
                          ? '#ea580c'
                          : '#dc2626',
                    }}
                  />
                </div>
              </div>

              <div className="bg-gray-50 rounded-xl p-3 mb-3">
                <p className="text-xs text-gray-500 mb-1">Рекомендация</p>
                <p className="text-sm font-medium">{getRecommendationLabel(risk.recommendation)}</p>
              </div>

              {risk.detailedAnalysis && (
                <div className="text-sm text-gray-700 leading-relaxed">{risk.detailedAnalysis}</div>
              )}

              {risk.riskFactorsJson && (
                <RiskFactors title="Факторы риска" items={parseJson(risk.riskFactorsJson)} />
              )}
              {risk.positiveFactorsJson && (
                <RiskFactors title="Положительные факторы" items={parseJson(risk.positiveFactorsJson)} variant="positive" />
              )}
              {risk.recommendationsJson && (
                <RiskFactors title="Рекомендации" items={parseJson(risk.recommendationsJson)} variant="neutral" />
              )}
            </div>
          ) : (
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 text-center text-gray-400">
              <AlertTriangle className="w-8 h-8 mx-auto mb-2 opacity-50" />
              <p>Оценка рисков не проведена</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function QuestionnaireSection({ title, children }: { title: string; children: React.ReactNode }) {
  const [open, setOpen] = useState(false)
  return (
    <div className="border border-gray-100 rounded-xl overflow-hidden">
      <button
        onClick={() => setOpen(!open)}
        className="w-full flex items-center justify-between px-4 py-3 bg-gray-50 hover:bg-gray-100 transition text-left"
      >
        <span className="font-medium text-sm">{title}</span>
        <span className="text-gray-400 text-sm">{open ? '−' : '+'}</span>
      </button>
      {open && <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-3">{children}</div>}
    </div>
  )
}

function QField({ label, value }: { label: string; value?: string | number | null }) {
  if (value === undefined || value === null || value === '') return null
  return (
    <div>
      <p className="text-xs text-gray-500">{label}</p>
      <p className="text-sm font-medium text-gray-900">{String(value)}</p>
    </div>
  )
}

function boolLabel(v?: boolean | null): string {
  if (v === true) return 'Да'
  if (v === false) return 'Нет'
  return '—'
}

function arrayLabel(v?: string[] | null): string {
  if (!v || v.length === 0) return '—'
  return v.join(', ')
}

function parseJson(s: string): string[] {
  try {
    const parsed = JSON.parse(s)
    return Array.isArray(parsed) ? parsed.map((i) => (typeof i === 'string' ? i : i?.description || i?.title || JSON.stringify(i))) : []
  } catch {
    return []
  }
}

function RiskFactors({
  title,
  items,
  variant = 'negative',
}: {
  title: string
  items: string[]
  variant?: 'negative' | 'positive' | 'neutral'
}) {
  if (!items.length) return null
  const color =
    variant === 'positive' ? 'text-green-700 bg-green-50' : variant === 'neutral' ? 'text-gray-700 bg-gray-50' : 'text-red-700 bg-red-50'
  return (
    <div className="mt-3">
      <p className="text-xs font-medium text-gray-500 mb-1.5">{title}</p>
      <ul className="space-y-1">
        {items.map((item, i) => (
          <li key={i} className={`text-xs rounded-lg px-2.5 py-1.5 ${color}`}>
            {item}
          </li>
        ))}
      </ul>
    </div>
  )
}
