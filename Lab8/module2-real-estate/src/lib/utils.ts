// =============================================================================
// UTILIDADES - Module 2: Real Estate React
// =============================================================================
// Funciones utilitarias compartidas en toda la aplicación.
//
// ## cn() - Class Name Utility
// Esta función es el corazón de Shadcn UI. Combina:
// - clsx: Para condicionales de clases
// - tailwind-merge: Para resolver conflictos de Tailwind
// =============================================================================

import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Combina clases de Tailwind CSS de forma inteligente.
 *
 * ## ¿Por qué necesitamos esto?
 * Tailwind tiene muchas clases que se sobrescriben entre sí:
 * - `p-4 p-8` → Conflicto, ¿cuál aplica?
 * - `text-red-500 text-blue-500` → Conflicto de colores
 *
 * `cn()` resuelve estos conflictos manteniendo solo la última clase.
 *
 * @param inputs - Clases a combinar (strings, arrays, objetos condicionales)
 * @returns String con las clases combinadas y conflictos resueltos
 *
 * @example
 * // Básico
 * cn('p-4', 'text-red-500'); // "p-4 text-red-500"
 *
 * // Con conflictos (la última gana)
 * cn('p-4', 'p-8'); // "p-8"
 *
 * // Condicionales
 * cn('base', isActive && 'bg-blue-500'); // "base bg-blue-500" si isActive
 *
 * // Override de props
 * cn('p-4 bg-red-500', className); // className puede sobrescribir
 */
export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}

/**
 * Genera un ID único usando timestamp y random.
 *
 * ## Nota sobre IDs
 * En producción, usaríamos UUIDs (crypto.randomUUID()) o IDs del servidor.
 * Este método simple es suficiente para desarrollo y localStorage.
 *
 * @returns ID único como string
 */
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
}

/**
 * Formatea un precio como moneda.
 *
 * @param price - Precio a formatear
 * @param currency - Código de moneda (default: USD)
 * @returns Precio formateado
 *
 * @example
 * formatPrice(150000); // "$150,000"
 * formatPrice(150000, 'EUR'); // "€150,000"
 */
export function formatPrice(price: number, currency = 'USD'): string {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency,
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(price);
}

/**
 * Formatea área en metros cuadrados.
 *
 * @param area - Área en m²
 * @returns Área formateada
 */
export function formatArea(area: number): string {
  return `${new Intl.NumberFormat('es-ES').format(area)} m²`;
}

/**
 * Trunca un texto a una longitud máxima.
 *
 * @param text - Texto a truncar
 * @param maxLength - Longitud máxima
 * @returns Texto truncado con "..." si excede
 */
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return `${text.substring(0, maxLength).trim()}...`;
}
