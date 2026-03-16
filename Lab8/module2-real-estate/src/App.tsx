// =============================================================================
// APP COMPONENT - Module 2: Real Estate React
// =============================================================================
// Componente raíz de la aplicación que configura:
// - Routing con React Router
// - Layout general
// - Providers globales (si los hubiera)
//
// ## React Router v7
// React Router es el estándar para routing en aplicaciones React.
// Usamos Routes y Route para definir las páginas de la aplicación.
// =============================================================================

import type React from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { Toaster } from '@/components/ui/sonner';
import { Home, Building2 } from 'lucide-react';
import { HomePage } from '@/pages/HomePage';
import { NewPropertyPage } from '@/pages/NewPropertyPage';
import { PropertyDetailPage } from '@/pages/PropertyDetailPage';

/**
 * Componente principal de la aplicación.
 *
 * ## Estructura:
 * - Header con navegación
 * - Main con las rutas
 * - Footer con créditos
 */
function App(): React.ReactElement {
  return (
    <>
      {/* Toaster para notificaciones - fuera del layout para evitar problemas de z-index */}
      <Toaster position="top-right" richColors closeButton />

      <div className="min-h-screen flex flex-col bg-background">
        {/* ===================================================================
          HEADER / NAVEGACIÓN
          =================================================================== */}
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container mx-auto flex h-16 items-center px-4">
            {/* Logo y nombre */}
            <Link to="/" className="flex items-center gap-2 font-bold text-xl">
              <Building2 className="h-6 w-6 text-primary" />
              <span>RealEstate</span>
            </Link>

            {/* Navegación */}
            <nav className="ml-auto flex items-center gap-4">
              <Link
                to="/"
                className="flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                <Home className="h-4 w-4" />
                Inicio
              </Link>
            </nav>
          </div>
        </header>

        {/* ===================================================================
          CONTENIDO PRINCIPAL
          ===================================================================
          Routes define las diferentes "páginas" de la aplicación.
          Cada Route mapea una URL a un componente.
          =================================================================== */}
        <main className="flex-1">
          <Routes>
            {/* Página principal - Lista de propiedades */}
            <Route path="/" element={<HomePage />} />

            {/* Página para crear nueva propiedad */}
            <Route path="/new" element={<NewPropertyPage />} />

            {/* Página de detalle de propiedad */}
            <Route path="/property/:id" element={<PropertyDetailPage />} />

            {/* Ruta 404 - Página no encontrada */}
            <Route
              path="*"
              element={
                <div className="container mx-auto px-4 py-16 text-center">
                  <h1 className="text-4xl font-bold mb-4">404</h1>
                  <p className="text-muted-foreground mb-6">Página no encontrada</p>
                  <Link
                    to="/"
                    className="text-primary hover:underline"
                  >
                    Volver al inicio
                  </Link>
                </div>
              }
            />
          </Routes>
        </main>

        {/* ===================================================================
          FOOTER
          =================================================================== */}
        <footer className="border-t py-6 mt-auto">
          <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
            <p>
              Portal Inmobiliario - Módulo 2 del Curso de Desarrollo Web
            </p>
            <p className="mt-1">
              Desarrollado con React 19, Tailwind CSS y Shadcn UI
            </p>
          </div>
        </footer>
      </div>
    </>
  );
}

export default App;
