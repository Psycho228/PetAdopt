import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import Layout from './components/Layout'
import PetImportPage from './pages/PetImportPage'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import PetsPage from './pages/PetsPage'
import PetFormPage from './pages/PetFormPage'
import ApplicationsPage from './pages/ApplicationsPage'
import ApplicationDetailPage from './pages/ApplicationDetailPage'
import BreederModerationPage from './pages/BreederModerationPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth()
  console.log('[ProtectedRoute] loading:', loading, 'user:', user?.email)

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-warm-100">
        <div className="w-10 h-10 border-4 border-primary-200 border-t-primary-600 rounded-full animate-spin" />
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="pets" element={<PetsPage />} />
        <Route path="pets/new" element={<PetFormPage />} />
        <Route path="pets/import" element={<PetImportPage />} />
        <Route path="pets/:id" element={<PetFormPage />} />
        <Route path="applications" element={<ApplicationsPage />} />
        <Route path="applications/:id" element={<ApplicationDetailPage />} />
        <Route path="breeders" element={<BreederModerationPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
