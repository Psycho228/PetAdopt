import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { supabase } from '../lib/supabase'
import type { User as AppUser } from '../lib/types'
import type { User, Session } from '@supabase/supabase-js'

interface AuthContextType {
  user: AppUser | null
  session: Session | null
  loading: boolean
  signIn: (email: string, password: string) => Promise<{ error?: string }>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  session: null,
  loading: true,
  signIn: async () => ({}),
  signOut: async () => {},
})

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<Session | null>(null)
  const [user, setUser] = useState<AppUser | null>(null)
  const [loading, setLoading] = useState(true)

  const loadProfile = useCallback(async (userId: string) => {
    console.log('[Auth] Loading profile for:', userId)
    const { data, error } = await supabase
      .from('users')
      .select('*')
      .eq('id', userId)
      .single()

    console.log('[Auth] Profile result:', { data, error })

    if (error) {
      console.error('[Auth] Profile load error:', error.message)
      setUser(null)
      setLoading(false)
      return
    }

    const profile = data as AppUser | null
    if (profile && (profile.role === 'shelter' || profile.role === 'admin')) {
      console.log('[Auth] Access granted, role:', profile.role)
      setUser(profile)
    } else {
      console.log('[Auth] Access denied. Role:', profile?.role)
      setUser(null)
    }
    setLoading(false)
  }, [])

  useEffect(() => {
    // Check initial session
    supabase.auth.getSession().then(({ data: { session } }) => {
      console.log('[Auth] Initial session check:', session?.user?.id)
      setSession(session)
      if (session?.user) {
        loadProfile(session.user.id)
      } else {
        setLoading(false)
      }
    })

    // Listen for auth changes
    const { data: listener } = supabase.auth.onAuthStateChange((_event, session) => {
      console.log('[Auth] State change:', _event, 'user:', session?.user?.id)
      setSession(session)
      if (session?.user) {
        loadProfile(session.user.id)
      } else {
        setUser(null)
        setLoading(false)
      }
    })

    return () => listener.subscription.unsubscribe()
  }, [loadProfile])

  async function signIn(email: string, password: string) {
    console.log('[Auth] Signing in:', email)
    setLoading(true)

    try {
      const { error, data } = await supabase.auth.signInWithPassword({ email, password })
      if (error) {
        console.error('[Auth] Sign in error:', error.message)
        setLoading(false)
        return { error: error.message }
      }

      console.log('[Auth] Sign in success, user:', data.user?.id)

      // Load profile immediately after sign in
      const { data: profile, error: profileError } = await supabase
        .from('users')
        .select('*')
        .eq('id', data.user.id)
        .single()

      console.log('[Auth] Profile after signin:', { profile, error: profileError })

      if (profileError || !profile) {
        await supabase.auth.signOut()
        setLoading(false)
        return { error: 'Профиль не найден. Обратитесь к администратору.' }
      }

      const p = profile as AppUser
      if (p.role !== 'shelter' && p.role !== 'admin') {
        await supabase.auth.signOut()
        setLoading(false)
        return { error: `Доступ запрещён. Ваша роль: ${p.role || 'не указана'}. Требуется: shelter или admin.` }
      }

      console.log('[Auth] Sign in complete, role:', p.role)
      setUser(p)
      setSession(data.session)
      setLoading(false)
      return {}
    } catch (err: any) {
      console.error('[Auth] Unexpected error:', err)
      setLoading(false)
      return { error: err?.message || 'Ошибка авторизации' }
    }
  }

  async function signOut() {
    await supabase.auth.signOut()
    setUser(null)
    setSession(null)
  }

  return (
    <AuthContext.Provider value={{ user, session, loading, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
