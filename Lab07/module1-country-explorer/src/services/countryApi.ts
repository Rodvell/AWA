// =============================================================================
// SERVICIO DE API - Country Explorer
// =============================================================================
// Este módulo encapsula toda la lógica de comunicación con la API externa.
//
// ## Fetch API
// fetch() es la API nativa del navegador para hacer peticiones HTTP.
// Es una API basada en Promesas, lo que significa que podemos usar
// async/await para manejar las respuestas de forma más legible.
//
// ## ¿Por qué separar la lógica de API?
// 1. Facilita el testing (podemos mockear este módulo)
// 2. Centraliza el manejo de errores de red
// 3. Permite cambiar la implementación sin afectar el resto del código
// =============================================================================

import type { Country } from '../types/country';

// URL base de la REST Countries API
const BASE_URL = 'https://restcountries.com/v3.1';

// Campos que queremos recibir de la API (optimiza el tamaño de respuesta)
const FIELDS = [
  'name',
  'cca3',
  'capital',
  'region',
  'subregion',
  'population',
  'area',
  'flags',
  'coatOfArms',
  'languages',
  'currencies',
  'timezones',
  'continents',
  'borders',
  'maps',
  'landlocked',
  'demonyms',
  'car',
].join(',');

/**
 * Error personalizado para errores de la API.
 *
 * ## ¿Por qué crear una clase de error personalizada?
 * JavaScript tiene una clase Error genérica, pero crear clases específicas
 * nos permite:
 * 1. Diferenciar tipos de errores en el catch
 * 2. Agregar información adicional (código de estado, etc.)
 * 3. Mejorar los mensajes de error para el usuario
 */
export class ApiError extends Error {
  constructor(
    message: string,
    public readonly statusCode?: number,
    public readonly originalError?: unknown
  ) {
    super(message);
    // Necesario para que instanceof funcione correctamente con clases ES6
    this.name = 'ApiError';
  }
}

/**
 * Busca países por nombre.
 *
 * ## Async/Await explicado
 * - `async` marca la función como asíncrona, permitiendo usar `await`
 * - `await` pausa la ejecución hasta que la Promise se resuelve
 * - Si la Promise se rechaza, se lanza una excepción que podemos capturar
 *
 * @param name - Nombre del país a buscar (parcial o completo)
 * @returns Array de países que coinciden con la búsqueda
 * @throws {ApiError} Si hay un error de red o la API devuelve un error
 *
 * @example
 * ```typescript
 * // Búsqueda exitosa
 * const countries = await searchCountries('spain');
 * console.log(countries[0].name.common); // "Spain"
 *
 * // Manejo de errores
 * try {
 *   await searchCountries('xyz123');
 * } catch (error) {
 *   if (error instanceof ApiError) {
 *     console.error('Error de API:', error.message);
 *   }
 * }
 * ```
 */
export async function searchCountries(name: string): Promise<Country[]> {
  // Validación de entrada antes de hacer la petición
  const trimmedName = name.trim();
  if (trimmedName.length === 0) {
    return [];
  }

  // Construimos la URL con el parámetro de búsqueda
  // encodeURIComponent escapa caracteres especiales para URLs
  const url = `${BASE_URL}/name/${encodeURIComponent(trimmedName)}?fields=${FIELDS}`;

  try {
    // =========================================================================
    // FETCH API - Petición HTTP
    // =========================================================================
    // fetch() devuelve una Promise que se resuelve cuando la respuesta llega.
    // IMPORTANTE: fetch() solo rechaza la Promise en errores de RED, no en
    // respuestas HTTP con código de error (404, 500, etc.).
    // =========================================================================
    const response = await fetch(url);

    // Verificamos si la respuesta fue exitosa (código 200-299)
    if (!response.ok) {
      // 404 significa que no se encontraron países con ese nombre
      if (response.status === 404) {
        return [];
      }

      // Otros errores HTTP los convertimos en ApiError
      throw new ApiError(`Error del servidor: ${response.status} ${response.statusText}`, response.status);
    }

    // =========================================================================
    // PARSEANDO JSON
    // =========================================================================
    // response.json() también devuelve una Promise porque necesita leer
    // todo el cuerpo de la respuesta antes de parsearlo.
    // =========================================================================
    const data: unknown = await response.json();

    // Validamos que la respuesta sea un array
    if (!Array.isArray(data)) {
      throw new ApiError('Respuesta inesperada de la API');
    }

    // TypeScript infiere el tipo correcto gracias al tipo de retorno
    return data as Country[];
  } catch (error) {
    // Re-lanzamos ApiError sin modificar
    if (error instanceof ApiError) {
      throw error;
    }

    // Errores de red (sin conexión, CORS, etc.)
    if (error instanceof TypeError) {
      throw new ApiError('Error de conexión. Verifica tu conexión a internet.', undefined, error);
    }

    // Errores desconocidos
    throw new ApiError('Error inesperado al buscar países', undefined, error);
  }
}

/**
 * Obtiene los detalles de un país por su código CCA3.
 *
 * @param code - Código de 3 letras del país (ISO 3166-1 alpha-3)
 * @returns El país encontrado o null si no existe
 * @throws {ApiError} Si hay un error de red
 */
export async function getCountryByCode(code: string): Promise<Country | null> {
  const url = `${BASE_URL}/alpha/${encodeURIComponent(code)}?fields=${FIELDS}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      if (response.status === 404) {
        return null;
      }
      throw new ApiError(`Error del servidor: ${response.status}`, response.status);
    }

    // La API devuelve un array incluso para búsquedas por código
    const data: unknown = await response.json();

    if (Array.isArray(data) && data.length > 0) {
      return data[0] as Country;
    }

    return data as Country;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError('Error al obtener detalles del país', undefined, error);
  }
}

/**
 * Obtiene todos los países (útil para la carga inicial o exploración).
 *
 * NOTA: Esta función puede devolver ~250 países, úsala con cuidado.
 */
export async function getAllCountries(): Promise<Country[]> {
  const url = `${BASE_URL}/all?fields=${FIELDS}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new ApiError(`Error del servidor: ${response.status}`, response.status);
    }

    const data: unknown = await response.json();

    if (!Array.isArray(data)) {
      throw new ApiError('Respuesta inesperada de la API');
    }

    return data as Country[];
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError('Error al obtener la lista de países', undefined, error);
  }
}

/**
 * Obtiene países por región geográfica.
 *
 * @param region - Región a filtrar (Africa, Americas, Asia, Europe, Oceania)
 */
export async function getCountriesByRegion(
  region: 'Africa' | 'Americas' | 'Asia' | 'Europe' | 'Oceania'
): Promise<Country[]> {
  const url = `${BASE_URL}/region/${region}?fields=${FIELDS}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new ApiError(`Error del servidor: ${response.status}`, response.status);
    }

    const data: unknown = await response.json();

    if (!Array.isArray(data)) {
      throw new ApiError('Respuesta inesperada de la API');
    }

    return data as Country[];
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new ApiError('Error al obtener países por región', undefined, error);
  }
}
