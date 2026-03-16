// =============================================================================
// UTILIDADES DEL DOM - Country Explorer
// =============================================================================
// Funciones helper para manipular el DOM de forma segura y tipada.
//
// ## DOM API vs Virtual DOM
// En este proyecto usamos la API nativa del DOM (document.querySelector, etc.)
// en lugar de un Virtual DOM (como React o Vue). Esto nos permite:
// 1. Entender cómo funciona el navegador realmente
// 2. Apreciar las abstracciones que ofrecen los frameworks
// 3. Trabajar sin dependencias en proyectos pequeños
//
// ## Tipado del DOM en TypeScript
// TypeScript tiene tipos para todos los elementos del DOM:
// - HTMLElement: Elemento genérico
// - HTMLInputElement: <input>
// - HTMLButtonElement: <button>
// - etc.
// =============================================================================

/**
 * Obtiene un elemento del DOM de forma segura con tipado.
 *
 * ## Genéricos en TypeScript
 * El parámetro <T> permite especificar qué tipo de elemento esperamos.
 * Esto nos da autocompletado correcto (ej: .value para inputs).
 *
 * @param selector - Selector CSS del elemento
 * @returns El elemento tipado o null si no existe
 *
 * @example
 * // El tipo se infiere como HTMLInputElement | null
 * const input = getElement<HTMLInputElement>('#searchInput');
 * if (input) {
 *   console.log(input.value); // TypeScript sabe que tiene .value
 * }
 */
export function getElement<T extends HTMLElement>(selector: string): T | null {
  return document.querySelector<T>(selector);
}

/**
 * Obtiene un elemento del DOM, lanzando error si no existe.
 *
 * Útil cuando sabemos que el elemento DEBE existir (definido en HTML).
 *
 * @param selector - Selector CSS del elemento
 * @throws Error si el elemento no existe
 *
 * @example
 * // Garantiza que el elemento existe o falla rápido
 * const button = getRequiredElement<HTMLButtonElement>('#submitBtn');
 * button.addEventListener('click', handleClick);
 */
export function getRequiredElement<T extends HTMLElement>(selector: string): T {
  const element = document.querySelector<T>(selector);
  if (!element) {
    throw new Error(`Elemento requerido no encontrado: ${selector}`);
  }
  return element;
}

/**
 * Muestra un elemento removiendo la clase 'hidden'.
 *
 * ## Patrón de visibilidad con Tailwind
 * Tailwind usa la clase 'hidden' (display: none) para ocultar elementos.
 * Este patrón es más eficiente que crear/destruir elementos del DOM.
 *
 * @param element - Elemento a mostrar
 */
export function showElement(element: HTMLElement): void {
  element.classList.remove('hidden');
}

/**
 * Oculta un elemento agregando la clase 'hidden'.
 *
 * @param element - Elemento a ocultar
 */
export function hideElement(element: HTMLElement): void {
  element.classList.add('hidden');
}

/**
 * Alterna la visibilidad de un elemento.
 *
 * @param element - Elemento a alternar
 * @returns true si el elemento quedó visible, false si quedó oculto
 */
export function toggleElement(element: HTMLElement): boolean {
  element.classList.toggle('hidden');
  return !element.classList.contains('hidden');
}

/**
 * Crea un elemento HTML con clases opcionales.
 *
 * ## Template Literal Types
 * El parámetro tag usa los nombres reales de elementos HTML,
 * y TypeScript infiere el tipo de retorno correcto.
 *
 * @param tag - Nombre del elemento HTML
 * @param classes - Clases CSS a agregar
 * @returns El elemento creado
 *
 * @example
 * const div = createElement('div', 'p-4', 'bg-slate-800', 'rounded-lg');
 * // TypeScript sabe que es HTMLDivElement
 */
export function createElement<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  ...classes: string[]
): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  if (classes.length > 0) {
    element.classList.add(...classes);
  }
  return element;
}

/**
 * Limpia el contenido de un elemento.
 *
 * ## innerHTML vs textContent vs replaceChildren
 * - innerHTML = '': Puede ser lento, parsea HTML
 * - textContent = '': No remueve event listeners correctamente
 * - replaceChildren(): Metodo moderno, limpio y eficiente (recomendado)
 *
 * @param element - Elemento a limpiar
 */
export function clearElement(element: HTMLElement): void {
  element.replaceChildren();
}

/**
 * Añade un event listener con tipado correcto.
 *
 * @param element - Elemento objetivo
 * @param event - Nombre del evento
 * @param handler - Función manejadora
 * @param options - Opciones del listener
 */
export function addListener<K extends keyof HTMLElementEventMap>(
  element: HTMLElement,
  event: K,
  handler: (ev: HTMLElementEventMap[K]) => void,
  options?: AddEventListenerOptions
): void {
  element.addEventListener(event, handler, options);
}

/**
 * Ejecuta código cuando el DOM está listo.
 *
 * ## DOMContentLoaded vs load
 * - DOMContentLoaded: Se dispara cuando el HTML está parseado
 * - load: Se dispara cuando TODO (imágenes, scripts) está cargado
 *
 * Generalmente queremos DOMContentLoaded para inicializar la app rápido.
 *
 * @param callback - Función a ejecutar
 */
export function onDOMReady(callback: () => void): void {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', callback);
  } else {
    // El DOM ya está listo, ejecutar inmediatamente
    callback();
  }
}

/**
 * Debounce: retrasa la ejecución hasta que pase un tiempo sin llamadas.
 *
 * ## ¿Para qué sirve debounce?
 * Evita ejecutar funciones costosas demasiado seguido. Por ejemplo,
 * al buscar mientras el usuario escribe, no queremos hacer una petición
 * por cada tecla, sino esperar a que termine de escribir.
 *
 * @param fn - Función a ejecutar
 * @param delay - Milisegundos a esperar
 * @returns Función debounced
 *
 * @example
 * const debouncedSearch = debounce((query: string) => {
 *   searchCountries(query);
 * }, 300);
 *
 * input.addEventListener('input', (e) => {
 *   debouncedSearch(e.target.value);
 * });
 */
export function debounce<T extends (...args: Parameters<T>) => void>(
  fn: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null;

  return (...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId);
    }
    timeoutId = setTimeout(() => {
      fn(...args);
    }, delay);
  };
}
