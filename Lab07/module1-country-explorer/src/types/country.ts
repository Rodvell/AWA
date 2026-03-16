// =============================================================================
// TIPOS DE DATOS - Country Explorer
// =============================================================================
// En TypeScript, definimos interfaces para tipar los datos que recibimos de
// la API. Esto nos da:
// 1. Autocompletado en el editor (IntelliSense)
// 2. Detección de errores en tiempo de compilación
// 3. Documentación implícita del código
//
// NOTA: Usamos 'interface' para objetos porque son más extensibles que 'type'.
// Las interfaces pueden ser extendidas y fusionadas (declaration merging).
// =============================================================================

/**
 * Representa los datos completos de un país desde la REST Countries API.
 *
 * ## ¿Por qué tipamos la respuesta de la API?
 * La API REST Countries devuelve objetos con muchos campos. Al tipar solo
 * los que necesitamos, hacemos el código más mantenible y evitamos errores
 * al acceder a propiedades que podrían no existir.
 */
export interface Country {
  /** Nombre del país en diferentes formatos */
  name: CountryName;

  /** Código de país de 3 letras (ISO 3166-1 alpha-3), ej: "ESP" */
  cca3: string;

  /** Capital(es) del país - puede tener múltiples o ninguna */
  capital?: string[];

  /** Región geográfica (Europe, Americas, Asia, etc.) */
  region: string;

  /** Subregión más específica (Western Europe, South America, etc.) */
  subregion?: string;

  /** Población total del país */
  population: number;

  /** Área en km² */
  area?: number;

  /** URLs de las banderas del país */
  flags: CountryFlags;

  /** Información del escudo de armas */
  coatOfArms?: CountryCoatOfArms;

  /** Idiomas oficiales - objeto con código ISO como clave */
  languages?: Record<string, string>;

  /** Monedas oficiales - objeto con código ISO como clave */
  currencies?: Record<string, CountryCurrency>;

  /** Zona(s) horaria(s) del país */
  timezones: string[];

  /** Continente(s) donde se ubica el país */
  continents: string[];

  /** Países fronterizos (códigos CCA3) */
  borders?: string[];

  /** URL de Google Maps */
  maps: CountryMaps;

  /** Indicador de si es país sin salida al mar */
  landlocked: boolean;

  /** Gentilicio en diferentes géneros */
  demonyms?: {
    eng: {
      f: string;
      m: string;
    };
  };

  /** Lado de conducción */
  car: {
    signs?: string[];
    side: 'left' | 'right';
  };
}

/**
 * Estructura del nombre del país.
 * La API proporciona múltiples formas del nombre para diferentes usos.
 */
export interface CountryName {
  /** Nombre común (España, México) */
  common: string;

  /** Nombre oficial completo (Reino de España) */
  official: string;

  /** Nombres nativos en cada idioma oficial */
  nativeName?: Record<
    string,
    {
      common: string;
      official: string;
    }
  >;
}

/**
 * URLs de las banderas en diferentes formatos.
 */
export interface CountryFlags {
  /** URL de la bandera en formato PNG */
  png: string;

  /** URL de la bandera en formato SVG (mejor calidad) */
  svg: string;

  /** Texto alternativo descriptivo de la bandera */
  alt?: string;
}

/**
 * URLs del escudo de armas.
 */
export interface CountryCoatOfArms {
  png?: string;
  svg?: string;
}

/**
 * Información de una moneda.
 */
export interface CountryCurrency {
  /** Nombre de la moneda (Euro, Peso Mexicano) */
  name: string;

  /** Símbolo de la moneda (€, $) */
  symbol?: string;
}

/**
 * URLs de mapas del país.
 */
export interface CountryMaps {
  /** Enlace a Google Maps */
  googleMaps: string;

  /** Enlace a OpenStreetMap */
  openStreetMaps: string;
}

// =============================================================================
// TIPOS PARA EL ESTADO DE LA UI
// =============================================================================
// Estos tipos representan los diferentes estados que puede tener la interfaz.
// Usamos un patrón similar a "sealed class" en Kotlin para manejar estados.
// =============================================================================

/**
 * Estados posibles de la interfaz de usuario.
 *
 * ## Patrón de Estado Discriminado (Discriminated Union)
 * En TypeScript, usamos uniones discriminadas para representar estados
 * mutuamente excluyentes. El campo 'status' actúa como discriminador.
 *
 * Esto es similar al patrón sealed class en Kotlin:
 * ```kotlin
 * sealed class UiState {
 *   object Loading : UiState()
 *   data class Success(val data: List<Country>) : UiState()
 *   data class Error(val message: String) : UiState()
 * }
 * ```
 */
export type UiState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; data: Country[] }
  | { status: 'error'; message: string }
  | { status: 'empty' };

/**
 * Tipo auxiliar para extraer el estado de éxito.
 * Útil cuando sabemos que estamos en estado 'success'.
 */
export type SuccessState = Extract<UiState, { status: 'success' }>;

/**
 * Tipo auxiliar para extraer el estado de error.
 */
export type ErrorState = Extract<UiState, { status: 'error' }>;
