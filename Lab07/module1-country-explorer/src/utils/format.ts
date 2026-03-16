// =============================================================================
// UTILIDADES DE FORMATO - Country Explorer
// =============================================================================
// Funciones utilitarias para formatear datos para mostrar en la UI.
//
// ## ¿Por qué separar la lógica de formato?
// 1. Reutilización: las mismas funciones se usan en múltiples componentes
// 2. Testing: es fácil probar funciones puras de formato
// 3. Localización: centraliza el formato para facilitar i18n
// =============================================================================

/**
 * Formatea un número grande con separadores de miles.
 *
 * ## Intl.NumberFormat
 * Es la API nativa de JavaScript para formatear números según la localidad.
 * Esto asegura que los números se muestren correctamente para cada idioma:
 * - ES: 1.000.000 (punto como separador de miles)
 * - EN: 1,000,000 (coma como separador de miles)
 *
 * @param value - Número a formatear
 * @param locale - Código de idioma (por defecto español de España)
 * @returns Número formateado como string
 *
 * @example
 * formatNumber(1234567); // "1.234.567"
 * formatNumber(1234567, 'en-US'); // "1,234,567"
 */
export function formatNumber(value: number, locale = 'es-ES'): string {
  return new Intl.NumberFormat(locale).format(value);
}

/**
 * Formatea el área de un país en km².
 *
 * @param area - Área en kilómetros cuadrados
 * @returns Área formateada con unidad
 *
 * @example
 * formatArea(505990); // "505.990 km²"
 */
export function formatArea(area: number | undefined): string {
  if (area === undefined) {
    return 'No disponible';
  }
  return `${formatNumber(area)} km²`;
}

/**
 * Formatea la población de un país de forma legible.
 *
 * Para poblaciones grandes, usa notación abreviada:
 * - Millones: "45,5M"
 * - Miles: "523K"
 *
 * @param population - Número de habitantes
 * @returns Población formateada
 *
 * @example
 * formatPopulation(47351567);  // "47,4 millones"
 * formatPopulation(523000);    // "523.000"
 */
export function formatPopulation(population: number): string {
  if (population >= 1_000_000) {
    const millions = population / 1_000_000;
    return `${millions.toFixed(1).replace('.', ',')} millones`;
  }
  return formatNumber(population);
}

/**
 * Extrae y formatea la lista de idiomas de un país.
 *
 * @param languages - Objeto de idiomas (código: nombre)
 * @returns Lista de idiomas separados por coma
 *
 * @example
 * formatLanguages({ spa: 'Spanish', cat: 'Catalan' }); // "Spanish, Catalan"
 */
export function formatLanguages(languages: Record<string, string> | undefined): string {
  if (!languages || Object.keys(languages).length === 0) {
    return 'No disponible';
  }
  return Object.values(languages).join(', ');
}

/**
 * Extrae y formatea la lista de monedas de un país.
 *
 * @param currencies - Objeto de monedas
 * @returns Lista de monedas con símbolo
 *
 * @example
 * formatCurrencies({ EUR: { name: 'Euro', symbol: '€' } }); // "Euro (€)"
 */
export function formatCurrencies(
  currencies: Record<string, { name: string; symbol?: string }> | undefined
): string {
  if (!currencies || Object.keys(currencies).length === 0) {
    return 'No disponible';
  }

  return Object.values(currencies)
    .map((currency) => {
      if (currency.symbol) {
        return `${currency.name} (${currency.symbol})`;
      }
      return currency.name;
    })
    .join(', ');
}

/**
 * Formatea la lista de capitales de un país.
 *
 * @param capitals - Array de nombres de capitales
 * @returns Capitales separadas por coma o mensaje si no hay
 */
export function formatCapitals(capitals: string[] | undefined): string {
  if (!capitals || capitals.length === 0) {
    return 'Sin capital definida';
  }
  return capitals.join(', ');
}

/**
 * Formatea las zonas horarias de un país.
 *
 * @param timezones - Array de zonas horarias
 * @returns Zonas horarias formateadas
 */
export function formatTimezones(timezones: string[]): string {
  if (timezones.length === 0) {
    return 'No disponible';
  }
  if (timezones.length === 1) {
    return timezones[0];
  }
  // Si hay muchas zonas horarias, mostramos rango
  if (timezones.length > 3) {
    return `${timezones[0]} a ${timezones[timezones.length - 1]} (${timezones.length} zonas)`;
  }
  return timezones.join(', ');
}

/**
 * Convierte el lado de conducción a español.
 *
 * @param side - 'left' o 'right'
 * @returns Texto en español
 */
export function formatDrivingSide(side: 'left' | 'right'): string {
  return side === 'left' ? 'Izquierda (UK)' : 'Derecha';
}

/**
 * Formatea el estado de país sin litoral.
 *
 * @param landlocked - Si el país no tiene salida al mar
 * @returns Texto descriptivo
 */
export function formatLandlocked(landlocked: boolean): string {
  return landlocked ? 'Sí (sin salida al mar)' : 'No (tiene costa)';
}
