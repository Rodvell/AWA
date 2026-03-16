// =============================================================================
// PUNTO DE ENTRADA - Module 2: Real Estate React
// =============================================================================
// Este archivo es el punto de entrada de la aplicación React.
//
// ## StrictMode
// StrictMode es un componente de React que activa verificaciones adicionales
// durante el desarrollo:
// - Detecta efectos con cleanup incorrecto
// - Detecta uso de APIs obsoletas
// - Ayuda a encontrar bugs sutiles
// =============================================================================

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import 'sonner/dist/styles.css';
import App from './App';
import './index.css';

// Obtenemos el elemento root del DOM
const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('No se encontró el elemento #root en el DOM');
}

// =========================================================================
// CREANDO LA RAÍZ DE REACT 19
// =========================================================================
// En React 18+, usamos createRoot() en lugar de ReactDOM.render().
// Esto habilita las características de Concurrent Mode.
// =========================================================================
createRoot(rootElement).render(
  <StrictMode>
    {/* BrowserRouter habilita la navegación del lado del cliente */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
