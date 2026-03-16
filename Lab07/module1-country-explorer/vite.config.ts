// =============================================================================
// VITE CONFIGURATION - Module 1: Country Explorer
// =============================================================================
// Vite es un bundler moderno que ofrece:
// - Hot Module Replacement (HMR) instantáneo
// - Soporte nativo para TypeScript
// - Optimización automática para producción
// =============================================================================

import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  // Plugins de Vite
  plugins: [tailwindcss()],

  // Directorio raíz del código fuente
  root: '.',

  // Configuración del servidor de desarrollo
  server: {
    port: 3000,
    open: true, // Abre el navegador automáticamente
  },

  // Configuración de build
  build: {
    outDir: 'dist',
    sourcemap: true, // Útil para debugging en producción
    minify: 'esbuild',
  },
});
