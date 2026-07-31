import { useCallback, useEffect, useState } from 'react'
import {
  BadgeCheck,
  Building2,
  Check,
  Clock3,
  MapPin,
  PawPrint,
  RefreshCw,
  X,
} from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { supabase } from '../lib/supabase'
import type { BreederProfile, SaleListing } from '../lib/types'

type ListingWithBreeder = SaleListing & {
  breeder_profiles?: Pick<
    BreederProfile,
    'kennel_name' | 'city' | 'verification_status'
  > | null
}

export default function BreederModerationPage() {
  const { user } = useAuth()
  const [profiles, setProfiles] = useState<BreederProfile[]>([])
  const [listings, setListings] = useState<ListingWithBreeder[]>([])
  const [notes, setNotes] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [workingId, setWorkingId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const loadQueue = useCallback(async () => {
    setLoading(true)
    setError(null)

    const [profilesResult, listingsResult] = await Promise.all([
      supabase
        .from('breeder_profiles')
        .select('*')
        .eq('verification_status', 'pending')
        .order('created_at', { ascending: true }),
      supabase
        .from('sale_listings')
        .select('*, breeder_profiles(kennel_name, city, verification_status)')
        .eq('status', 'pending')
        .order('created_at', { ascending: true }),
    ])

    const firstError = profilesResult.error ?? listingsResult.error
    if (firstError) {
      setError(firstError.message)
    } else {
      setProfiles((profilesResult.data ?? []) as BreederProfile[])
      setListings((listingsResult.data ?? []) as ListingWithBreeder[])
    }
    setLoading(false)
  }, [])

  useEffect(() => {
    if (user?.role === 'admin') loadQueue()
  }, [loadQueue, user?.role])

  async function moderateProfile(
    profile: BreederProfile,
    status: 'verified' | 'rejected',
  ) {
    setWorkingId(profile.id)
    const { error } = await supabase
      .from('breeder_profiles')
      .update({
        verification_status: status,
        moderation_note: notes[profile.id]?.trim() || null,
      })
      .eq('id', profile.id)

    setWorkingId(null)
    if (error) setError(error.message)
    else loadQueue()
  }

  async function moderateListing(
    listing: ListingWithBreeder,
    status: 'available' | 'rejected',
  ) {
    setWorkingId(listing.id)
    const { error } = await supabase
      .from('sale_listings')
      .update({
        status,
        moderation_note: notes[listing.id]?.trim() || null,
      })
      .eq('id', listing.id)

    setWorkingId(null)
    if (error) setError(error.message)
    else loadQueue()
  }

  if (user?.role !== 'admin') {
    return (
      <div className="bg-white border border-warm-200 rounded-lg p-8 text-center">
        <BadgeCheck className="w-10 h-10 text-primary-600 mx-auto mb-3" />
        <h2 className="text-xl font-bold">Модерация доступна администратору</h2>
        <p className="text-gray-500 mt-2">
          Аккаунты приютов не могут одобрять продавцов и объявления.
        </p>
      </div>
    )
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
        <div>
          <h2 className="text-2xl font-bold">Заводчики</h2>
          <p className="text-sm text-gray-500 mt-1">
            Проверка профилей и объявлений перед публикацией
          </p>
        </div>
        <button
          onClick={loadQueue}
          disabled={loading}
          className="p-2.5 border border-warm-200 bg-white rounded-lg text-primary-700 hover:bg-primary-50 disabled:opacity-50"
          title="Обновить очередь"
        >
          <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {error && (
        <div className="mb-5 p-3 rounded-lg bg-red-50 text-red-700 border border-red-100">
          {error}
        </div>
      )}

      {loading ? (
        <div className="min-h-[50vh] flex items-center justify-center">
          <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
        </div>
      ) : (
        <div className="space-y-10">
          <section>
            <SectionHeading
              icon={Building2}
              title="Профили на проверке"
              count={profiles.length}
            />
            {profiles.length === 0 ? (
              <EmptyQueue />
            ) : (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                {profiles.map((profile) => (
                  <div
                    key={profile.id}
                    className="bg-white border border-warm-200 rounded-lg p-5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <h3 className="text-lg font-semibold">{profile.kennel_name}</h3>
                        <p className="flex items-center gap-1 text-sm text-gray-500 mt-1">
                          <MapPin className="w-4 h-4" />
                          {profile.city}
                        </p>
                      </div>
                      <QueueBadge />
                    </div>
                    <p className="text-sm text-gray-700 mt-4">{profile.description}</p>
                    <dl className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm mt-4">
                      <div>
                        <dt className="text-gray-400">Телефон</dt>
                        <dd>{profile.phone}</dd>
                      </div>
                      <div>
                        <dt className="text-gray-400">Породы</dt>
                        <dd>{profile.breeds?.join(', ') || 'Не указаны'}</dd>
                      </div>
                    </dl>
                    <ModerationControls
                      id={profile.id}
                      note={notes[profile.id] ?? ''}
                      working={workingId === profile.id}
                      onNote={(value) =>
                        setNotes((current) => ({ ...current, [profile.id]: value }))
                      }
                      onApprove={() => moderateProfile(profile, 'verified')}
                      onReject={() => moderateProfile(profile, 'rejected')}
                    />
                  </div>
                ))}
              </div>
            )}
          </section>

          <section>
            <SectionHeading
              icon={PawPrint}
              title="Объявления на проверке"
              count={listings.length}
            />
            {listings.length === 0 ? (
              <EmptyQueue />
            ) : (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
                {listings.map((listing) => {
                  const breederVerified =
                    listing.breeder_profiles?.verification_status === 'verified'
                  return (
                    <div
                      key={listing.id}
                      className="bg-white border border-warm-200 rounded-lg overflow-hidden"
                    >
                      <div className="grid grid-cols-[128px_1fr] min-h-36">
                        <div className="bg-warm-100">
                          {listing.photo_url && (
                            <img
                              src={listing.photo_url}
                              alt={listing.name}
                              className="w-full h-full object-cover"
                            />
                          )}
                        </div>
                        <div className="p-4 min-w-0">
                          <div className="flex justify-between gap-3">
                            <div className="min-w-0">
                              <h3 className="font-semibold text-lg truncate">
                                {listing.name}
                              </h3>
                              <p className="text-sm text-gray-500 truncate">
                                {listing.breed}
                              </p>
                            </div>
                            <strong className="text-primary-700 whitespace-nowrap">
                              {Number(listing.price).toLocaleString('ru-RU')} ₽
                            </strong>
                          </div>
                          <p className="text-sm mt-3">
                            {listing.breeder_profiles?.kennel_name ?? 'Профиль не найден'}
                          </p>
                          {!breederVerified && (
                            <p className="text-xs text-amber-700 mt-2">
                              Сначала подтвердите профиль заводчика
                            </p>
                          )}
                        </div>
                      </div>
                      <div className="px-5 pb-5">
                        <p className="text-sm text-gray-700 line-clamp-3">
                          {listing.description}
                        </p>
                        <ModerationControls
                          id={listing.id}
                          note={notes[listing.id] ?? ''}
                          working={workingId === listing.id}
                          approveDisabled={!breederVerified}
                          onNote={(value) =>
                            setNotes((current) => ({ ...current, [listing.id]: value }))
                          }
                          onApprove={() => moderateListing(listing, 'available')}
                          onReject={() => moderateListing(listing, 'rejected')}
                        />
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </section>
        </div>
      )}
    </div>
  )
}

function SectionHeading({
  icon: Icon,
  title,
  count,
}: {
  icon: typeof PawPrint
  title: string
  count: number
}) {
  return (
    <div className="flex items-center gap-3 mb-4">
      <Icon className="w-5 h-5 text-primary-700" />
      <h3 className="font-semibold text-lg">{title}</h3>
      <span className="text-xs font-semibold bg-primary-100 text-primary-800 px-2 py-1 rounded-full">
        {count}
      </span>
    </div>
  )
}

function QueueBadge() {
  return (
    <span className="flex items-center gap-1 text-xs font-medium bg-amber-50 text-amber-700 px-2 py-1 rounded-md">
      <Clock3 className="w-3.5 h-3.5" />
      На проверке
    </span>
  )
}

function EmptyQueue() {
  return (
    <div className="border border-dashed border-warm-300 rounded-lg py-8 text-center text-gray-400">
      Очередь пуста
    </div>
  )
}

function ModerationControls({
  id,
  note,
  working,
  approveDisabled = false,
  onNote,
  onApprove,
  onReject,
}: {
  id: string
  note: string
  working: boolean
  approveDisabled?: boolean
  onNote: (value: string) => void
  onApprove: () => void
  onReject: () => void
}) {
  return (
    <div className="mt-5 pt-4 border-t border-warm-100">
      <label htmlFor={`note-${id}`} className="block text-xs text-gray-500 mb-1.5">
        Комментарий модератора
      </label>
      <textarea
        id={`note-${id}`}
        value={note}
        onChange={(event) => onNote(event.target.value)}
        rows={2}
        className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
        placeholder="Обязателен при отклонении"
      />
      <div className="flex gap-2 mt-3">
        <button
          onClick={onApprove}
          disabled={working || approveDisabled}
          className="flex items-center gap-1.5 px-3 py-2 bg-primary-600 text-white rounded-lg text-sm font-medium hover:bg-primary-700 disabled:opacity-40"
        >
          <Check className="w-4 h-4" />
          Одобрить
        </button>
        <button
          onClick={onReject}
          disabled={working || note.trim().length === 0}
          className="flex items-center gap-1.5 px-3 py-2 border border-red-200 text-red-700 rounded-lg text-sm font-medium hover:bg-red-50 disabled:opacity-40"
        >
          <X className="w-4 h-4" />
          Отклонить
        </button>
      </div>
    </div>
  )
}
