import { useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import {
  LayoutDashboard,
  PawPrint,
  ClipboardList,
  LogOut,
  Menu,
  X,
} from 'lucide-react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'

const navItems = [
  { to: '/', label: 'Дашборд', icon: LayoutDashboard },
  { to: '/pets', label: 'Питомцы', icon: PawPrint },
  { to: '/applications', label: 'Заявки', icon: ClipboardList },
]

export default function Layout() {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  async function handleSignOut() {
    await signOut()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex bg-warm-100 text-gray-900">
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col w-64 bg-warm-50 border-r border-warm-200 fixed h-full">
        <div className="px-5 py-6 flex items-center gap-3 border-b border-warm-200">
          <div className="w-11 h-11 bg-primary-100 rounded-xl flex items-center justify-center">
            <PawPrint className="w-6 h-6 text-primary-700" />
          </div>
          <div>
            <h1 className="font-bold text-lg leading-tight text-primary-900">Хвостики</h1>
            <p className="text-xs text-gray-500">Рабочее место приюта</p>
          </div>
        </div>

        <nav className="flex-1 px-3 py-5 space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-primary-100 text-primary-800'
                    : 'text-gray-600 hover:bg-warm-200/70 hover:text-gray-900'
                }`
              }
            >
              <item.icon className="w-5 h-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-warm-200">
          <div className="flex items-center gap-3 mb-3 px-3">
            <div className="w-9 h-9 bg-secondary-100 rounded-full flex items-center justify-center text-secondary-800 font-semibold text-sm">
              {user?.name?.[0]?.toUpperCase() ?? '?'}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-medium truncate">{user?.name}</p>
              <p className="text-xs text-gray-500 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleSignOut}
            className="w-full flex items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition"
          >
            <LogOut className="w-4 h-4" />
            Выйти
          </button>
        </div>
      </aside>

      {/* Mobile header */}
      <div className="lg:hidden fixed top-0 left-0 right-0 z-50 bg-warm-50 border-b border-warm-200 px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <PawPrint className="w-5 h-5 text-primary-700" />
          <span className="font-bold text-primary-900">Хвостики</span>
        </div>
        <button
          onClick={() => setMobileOpen(!mobileOpen)}
          className="p-2 rounded-lg text-gray-600 hover:bg-warm-200"
          aria-label={mobileOpen ? 'Закрыть меню' : 'Открыть меню'}
        >
          {mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="lg:hidden fixed inset-0 z-40 bg-warm-50 pt-14">
          <nav className="p-4 space-y-1">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition ${
                    isActive
                      ? 'bg-primary-100 text-primary-800'
                      : 'text-gray-600 hover:bg-warm-200'
                  }`
                }
              >
                <item.icon className="w-5 h-5" />
                {item.label}
              </NavLink>
            ))}
            <button
              onClick={handleSignOut}
              className="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-600 hover:bg-red-50 rounded-lg transition"
            >
              <LogOut className="w-5 h-5" />
              Выйти
            </button>
          </nav>
        </div>
      )}

      {/* Main content */}
      <main className="flex-1 lg:ml-64 pt-14 lg:pt-0 bg-warm-100">
        <div className="p-4 md:p-8 max-w-7xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
