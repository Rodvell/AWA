// =============================================================================
// STORAGE SERVICE - Module 2: Real Estate React
// =============================================================================
// Servicio para persistir datos en localStorage.
//
// ## Educational Note: localStorage como "Base de Datos"
//
// En este módulo, localStorage simula un backend. Esto enseña conceptos
// fundamentales de persistencia de datos antes de introducir APIs reales.
//
// ### ¿Por qué usar localStorage primero?
//
// 1. **Sin configuración de servidor** - Enfoque 100% en React y formularios
// 2. **Datos persisten entre sesiones** - El usuario ve sus propiedades al volver
// 3. **Patrón CRUD idéntico** - Las operaciones son las mismas que con una API
// 4. **Manejo de errores** - Aprendemos a manejar fallos de almacenamiento
//
// ### Limitaciones de localStorage (importantes para entender)
//
// ```
// ┌─────────────────────────────────────────────────────────────────────────┐
// │                     COMPARACIÓN: localStorage vs API                    │
// ├─────────────────────────────────────────────────────────────────────────┤
// │  localStorage                        │  API REST (Módulo 3)             │
// │  ────────────────────────────────────┼────────────────────────────────  │
// │  ✗ Solo 5-10MB de almacenamiento    │  ✓ Almacenamiento ilimitado      │
// │  ✗ Solo strings (hay que serializar)│  ✓ JSON nativo                   │
// │  ✗ No sincroniza entre dispositivos │  ✓ Datos en la nube              │
// │  ✗ Usuario puede borrar datos       │  ✓ Datos seguros en servidor     │
// │  ✗ Sin búsqueda avanzada            │  ✓ Queries SQL/NoSQL             │
// │  ✓ Sin latencia de red              │  ✗ Requiere conexión             │
// │  ✓ Funciona offline                 │  ✗ Falla sin internet            │
// └─────────────────────────────────────────────────────────────────────────┘
// ```
//
// ### Evolución en el Curso
//
// En el **Módulo 3**, reemplazaremos estas funciones por llamadas fetch():
// - `getAllProperties()` → `fetch('/api/properties')`
// - `createProperty()` → `fetch('/api/properties', { method: 'POST' })`
// - `updateProperty()` → `fetch('/api/properties/:id', { method: 'PUT' })`
// - `deleteProperty()` → `fetch('/api/properties/:id', { method: 'DELETE' })`
//
// ¡El frontend casi no cambia! Solo la capa de datos es diferente.
// =============================================================================

import type { Property, PropertyFilters, CreatePropertyInput } from '@/types/property';
import { generateId } from './utils';

// Clave para almacenar propiedades en localStorage
const STORAGE_KEY = 'real_estate_properties';

// =============================================================================
// OPERACIONES CRUD
// =============================================================================

/**
 * Obtiene todas las propiedades del localStorage.
 *
 * ## Deserialización
 * localStorage solo almacena strings, así que usamos JSON.parse()
 * para convertir el string de vuelta a objetos JavaScript.
 *
 * @returns Array de propiedades
 */
export function getAllProperties(): Property[] {
  try {
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) return [];

    // Parseamos y validamos que sea un array
    const parsed: unknown = JSON.parse(data);
    if (!Array.isArray(parsed)) {
      console.error('Datos corruptos en localStorage, reiniciando...');
      return [];
    }

    return parsed as Property[];
  } catch (error) {
    console.error('Error al leer propiedades:', error);
    return [];
  }
}

/**
 * Obtiene una propiedad por su ID.
 *
 * @param id - ID de la propiedad
 * @returns La propiedad o undefined si no existe
 */
export function getPropertyById(id: string): Property | undefined {
  const properties = getAllProperties();
  return properties.find((p) => p.id === id);
}

/**
 * Crea una nueva propiedad.
 *
 * ## Flujo de creación:
 * 1. Genera ID único
 * 2. Agrega timestamps
 * 3. Guarda en localStorage
 *
 * @param input - Datos de la nueva propiedad
 * @returns La propiedad creada con ID y timestamps
 */
export function createProperty(input: CreatePropertyInput): Property {
  const properties = getAllProperties();

  const newProperty: Property = {
    ...input,
    id: generateId(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  properties.push(newProperty);
  saveProperties(properties);

  return newProperty;
}

/**
 * Actualiza una propiedad existente.
 *
 * @param id - ID de la propiedad a actualizar
 * @param input - Datos a actualizar
 * @returns La propiedad actualizada o null si no existe
 */
export function updateProperty(id: string, input: Partial<CreatePropertyInput>): Property | null {
  const properties = getAllProperties();
  const index = properties.findIndex((p) => p.id === id);

  if (index === -1) return null;

  const existingProperty = properties[index];
  if (!existingProperty) return null;

  const updatedProperty: Property = {
    ...existingProperty,
    ...input,
    updatedAt: new Date().toISOString(),
  };

  properties[index] = updatedProperty;
  saveProperties(properties);

  return updatedProperty;
}

/**
 * Elimina una propiedad.
 *
 * @param id - ID de la propiedad a eliminar
 * @returns true si se eliminó, false si no existía
 */
export function deleteProperty(id: string): boolean {
  const properties = getAllProperties();
  const filtered = properties.filter((p) => p.id !== id);

  if (filtered.length === properties.length) {
    return false; // No se encontró la propiedad
  }

  saveProperties(filtered);
  return true;
}

// =============================================================================
// FILTRADO Y BÚSQUEDA
// =============================================================================

/**
 * Filtra propiedades según los criterios especificados.
 *
 * ## Implementación de filtros
 * Cada filtro es opcional. Solo aplicamos los que tienen valor.
 * Esto permite combinaciones flexibles de criterios.
 *
 * @param filters - Criterios de filtrado
 * @returns Propiedades que cumplen todos los criterios
 */
export function filterProperties(filters: PropertyFilters): Property[] {
  let properties = getAllProperties();

  // Filtro de búsqueda por texto (título, descripción, dirección)
  if (filters.search) {
    const searchLower = filters.search.toLowerCase();
    properties = properties.filter(
      (p) =>
        p.title.toLowerCase().includes(searchLower) ||
        p.description.toLowerCase().includes(searchLower) ||
        p.address.toLowerCase().includes(searchLower) ||
        p.city.toLowerCase().includes(searchLower)
    );
  }

  // Filtro por tipo de propiedad
  if (filters.propertyType) {
    properties = properties.filter((p) => p.propertyType === filters.propertyType);
  }

  // Filtro por tipo de operación
  if (filters.operationType) {
    properties = properties.filter((p) => p.operationType === filters.operationType);
  }

  // Filtro por precio mínimo
  if (filters.minPrice !== undefined && filters.minPrice > 0) {
    properties = properties.filter((p) => p.price >= filters.minPrice!);
  }

  // Filtro por precio máximo
  if (filters.maxPrice !== undefined && filters.maxPrice > 0) {
    properties = properties.filter((p) => p.price <= filters.maxPrice!);
  }

  // Filtro por habitaciones mínimas
  if (filters.minBedrooms !== undefined && filters.minBedrooms > 0) {
    properties = properties.filter((p) => p.bedrooms >= filters.minBedrooms!);
  }

  // Filtro por ciudad
  if (filters.city) {
    properties = properties.filter((p) => p.city.toLowerCase().includes(filters.city!.toLowerCase()));
  }

  // Ordenar por fecha de creación (más recientes primero)
  return properties.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
}

// =============================================================================
// FUNCIONES AUXILIARES
// =============================================================================

/**
 * Guarda las propiedades en localStorage.
 *
 * @param properties - Array de propiedades a guardar
 */
function saveProperties(properties: Property[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(properties));
  } catch (error) {
    // localStorage puede fallar si está lleno o deshabilitado
    console.error('Error al guardar propiedades:', error);
    throw new Error('No se pudieron guardar los datos. El almacenamiento podría estar lleno.');
  }
}

/**
 * Inicializa el storage con datos de ejemplo si está vacío.
 * Útil para desarrollo y demostración.
 */
export function initializeWithSampleData(): void {
  const existing = getAllProperties();
  if (existing.length > 0) return; // Ya hay datos

  // Importamos y guardamos datos de ejemplo
  void import('@/data/sampleProperties').then((module) => {
    const sampleData = module.sampleProperties;
    saveProperties(sampleData as Property[]);
    console.log('Datos de ejemplo cargados');
  });
}
