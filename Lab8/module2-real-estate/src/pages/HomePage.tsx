// =============================================================================
// PÁGINA: HOME - Real Estate React
// =============================================================================
// Página principal que muestra la lista de propiedades con filtros.
//
// ## Gestión de Estado en React 19
// Usamos useState para el estado local de filtros y propiedades.
// En aplicaciones más grandes, consideraríamos:
// - Context API para estado compartido
// - Zustand/Jotai para estado global simple
// - TanStack Query para datos del servidor
// =============================================================================

import type React from 'react';
import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Search, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { PropertyCard } from '@/components/PropertyCard';
import { filterProperties, deleteProperty, initializeWithSampleData } from '@/lib/storage';
import type { Property, PropertyFilters } from '@/types/property';
import {
  PROPERTY_TYPES,
  OPERATION_TYPES,
  PROPERTY_TYPE_LABELS,
  OPERATION_TYPE_LABELS,
} from '@/types/property';

/**
 * Página principal con lista de propiedades y filtros.
 */
export function HomePage(): React.ReactElement {
  // =========================================================================
  // ESTADO
  // =========================================================================
  // - properties: Lista de propiedades filtradas
  // - filters: Criterios de búsqueda actuales
  // =========================================================================
  const [properties, setProperties] = useState<Property[]>([]);
  const [filters, setFilters] = useState<PropertyFilters>({});

  // =========================================================================
  // CARGAR PROPIEDADES
  // =========================================================================
  // useCallback memoriza la función para evitar recrearla en cada render.
  // Esto es importante cuando la función se pasa como dependencia de useEffect.
  // =========================================================================
  const loadProperties = useCallback(() => {
    const filtered = filterProperties(filters);
    setProperties(filtered);
  }, [filters]);

  // =========================================================================
  // EFECTOS
  // =========================================================================
  // useEffect ejecuta código después del render.
  // Aquí lo usamos para:
  // 1. Inicializar datos de ejemplo si no hay datos
  // 2. Cargar propiedades cuando cambian los filtros
  // =========================================================================
  useEffect(() => {
    // Inicializar con datos de ejemplo si está vacío
    initializeWithSampleData();

    // Pequeño delay para asegurar que los datos se cargaron
    const timer = setTimeout(() => {
      loadProperties();
    }, 100);

    return () => clearTimeout(timer);
  }, [loadProperties]);

  // =========================================================================
  // HANDLERS
  // =========================================================================

  /**
   * Actualiza un filtro específico.
   */
  const handleFilterChange = (key: keyof PropertyFilters, value: string | number): void => {
    setFilters((prev) => ({
      ...prev,
      [key]: value === 'all' ? undefined : value,
    }));
  };

  /**
   * Limpia todos los filtros.
   */
  const handleClearFilters = (): void => {
    setFilters({});
  };

  /**
   * Elimina una propiedad.
   */
  const handleDelete = (id: string): void => {
    if (window.confirm('¿Estás seguro de eliminar esta propiedad?')) {
      deleteProperty(id);
      loadProperties();
    }
  };

  // Verificamos si hay filtros activos
  const hasFilters = Object.values(filters).some(
    (v) => v !== undefined && v !== '' && v !== 0
  );

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
        <div>
          <h1 className="text-3xl font-bold">Propiedades Disponibles</h1>
          <p className="text-muted-foreground">
            {properties.length} {properties.length === 1 ? 'propiedad encontrada' : 'propiedades encontradas'}
          </p>
        </div>

        <Button asChild>
          <Link to="/new">
            <Plus className="h-4 w-4 mr-2" />
            Nueva Propiedad
          </Link>
        </Button>
      </div>

      {/* Filtros */}
      <div className="bg-card rounded-lg border p-4 mb-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Búsqueda por texto */}
          <div className="relative lg:col-span-2">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Buscar por título, dirección o ciudad..."
              className="pl-10"
              value={filters.search ?? ''}
              onChange={(e) => handleFilterChange('search', e.target.value)}
            />
          </div>

          {/* Tipo de propiedad */}
          <Select
            value={filters.propertyType ?? 'all'}
            onValueChange={(value) => handleFilterChange('propertyType', value)}
          >
            <SelectTrigger>
              <SelectValue placeholder="Tipo de propiedad" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todos los tipos</SelectItem>
              {PROPERTY_TYPES.map((type) => (
                <SelectItem key={type} value={type}>
                  {PROPERTY_TYPE_LABELS[type]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          {/* Tipo de operacion */}
          <Select
            value={filters.operationType ?? 'all'}
            onValueChange={(value) => handleFilterChange('operationType', value)}
          >
            <SelectTrigger>
              <SelectValue placeholder="Operacion" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Venta y Alquiler</SelectItem>
              {OPERATION_TYPES.map((type) => (
                <SelectItem key={type} value={type}>
                  {OPERATION_TYPE_LABELS[type]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        {/* Filtros adicionales y botón de limpiar */}
        <div className="flex flex-wrap gap-4 mt-4">
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Precio mín:</span>
            <Input
              type="number"
              placeholder="0"
              className="w-28"
              value={filters.minPrice ?? ''}
              onChange={(e) =>
                handleFilterChange('minPrice', e.target.value ? Number(e.target.value) : 0)
              }
            />
          </div>

          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Precio máx:</span>
            <Input
              type="number"
              placeholder="∞"
              className="w-28"
              value={filters.maxPrice ?? ''}
              onChange={(e) =>
                handleFilterChange('maxPrice', e.target.value ? Number(e.target.value) : 0)
              }
            />
          </div>

          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">Hab. mín:</span>
            <Input
              type="number"
              placeholder="0"
              className="w-20"
              min="0"
              value={filters.minBedrooms ?? ''}
              onChange={(e) =>
                handleFilterChange('minBedrooms', e.target.value ? Number(e.target.value) : 0)
              }
            />
          </div>

          {hasFilters && (
            <Button variant="ghost" size="sm" onClick={handleClearFilters}>
              <X className="h-4 w-4 mr-1" />
              Limpiar filtros
            </Button>
          )}
        </div>
      </div>

      {/* Lista de propiedades */}
      {properties.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {properties.map((property) => (
            <PropertyCard
              key={property.id}
              property={property}
              onDelete={handleDelete}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <p className="text-muted-foreground text-lg mb-4">
            No se encontraron propiedades
          </p>
          {hasFilters ? (
            <Button variant="outline" onClick={handleClearFilters}>
              Limpiar filtros
            </Button>
          ) : (
            <Button asChild>
              <Link to="/new">Agregar primera propiedad</Link>
            </Button>
          )}
        </div>
      )}
    </div>
  );
}
