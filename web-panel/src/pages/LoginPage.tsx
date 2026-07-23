import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import { PawPrint, Eye, EyeOff, LogIn } from 'lucide-react'

export default function LoginPage() {
  const { user, signIn } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Redirect if already logged in
  useEffect(() => {
    console.log('[Login] user changed:', user?.email)
    if (user) {
      console.log('[Login] Redirecting to dashboard...')
      navigate('/', { replace: true })
    }
  }, [user, navigate])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)

    const result = await signIn(email, password)
    console.log('[Login] signIn result:', result)

    if (result.error) {
      setError(result.error)
      setLoading(false)
    } else {
      // Success - AuthContext will update user, useEffect will redirect
      // Fallback: force reload after short delay
      setTimeout(() => {
        if (!user) {
          console.log('[Login] Fallback reload')
          window.location.href = '/'
        }
      }, 500)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-warm-100 p-4">
      <div className="w-full max-w-md bg-warm-50 rounded-xl shadow-xl shadow-primary-900/10 border border-warm-200 p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-primary-100 rounded-xl mb-4">
            <PawPrint className="w-8 h-8 text-primary-700" />
          </div>
          <h1 className="text-2xl font-bold text-primary-900">Хвостики</h1>
          <p className="text-gray-500 mt-1">Рабочая панель приюта</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2.5 rounded-lg border border-warm-300 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition"
              placeholder="shelter@example.com"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2.5 rounded-lg border border-warm-300 focus:border-primary-500 focus:ring-2 focus:ring-primary-200 outline-none transition pr-11"
                placeholder="••••••••"
                required
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
          </div>

          {error && (
            <div className="bg-red-50 text-red-700 text-sm rounded-lg px-4 py-3">{error}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 bg-primary-600 hover:bg-primary-700 disabled:bg-primary-300 text-white font-medium py-2.5 rounded-lg transition"
          >
            {loading ? (
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <LogIn className="w-5 h-5" />
            )}
            Войти
          </button>
        </form>
      </div>
    </div>
  )
}
