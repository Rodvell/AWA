// =============================================================================
// VITE CONFIGURATION - Module 2: Real Estate React
// =============================================================================
// Configuración de Vite para un proyecto React 19 con Tailwind CSS v4.
//
// ## ¿Por qué Vite para React?
// - HMR (Hot Module Replacement) instantáneo
// - Soporte nativo para JSX y TypeScript
// - Build optimizado con tree-shaking automático
// - Configuración mínima comparada con webpack
// =============================================================================

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

export default defineConfig({
  // =========================================================================
  // PLUGINS
  // =========================================================================
  // - React: Fast Refresh y transformación de JSX
  // - Tailwind CSS v4: Procesamiento de CSS sin PostCSS separado
  // =========================================================================
  plugins: [react(), tailwindcss()],

  // =========================================================================
  // RESOLVE - Alias de rutas
  // =========================================================================
  // Configuramos @ como alias para src/ para imports más limpios:
  // import { Button } from '@/components/ui/button'
  // en lugar de:
  // import { Button } from '../../../components/ui/button'
  // =========================================================================
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },

  // Configuración del servidor de desarrollo
  server: {
    port: 3001,
    open: true,
  },

  // Configuración de build
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
});
