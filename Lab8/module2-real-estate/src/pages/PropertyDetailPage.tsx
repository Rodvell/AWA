// =============================================================================
// PÁGINA: DETALLE DE PROPIEDAD - Real Estate React
// =============================================================================
// Página que muestra información detallada de una propiedad.
//
// ## useParams()
// Hook de React Router que extrae parámetros de la URL.
// La ruta /property/:id define un parámetro dinámico 'id'.
// =============================================================================

import type React from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Bed, Bath, Square, Calendar, Tag } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { getPropertyById, deleteProperty } from '@/lib/storage';
import {
  PROPERTY_TYPE_LABELS,
  OPERATION_TYPE_LABELS,
  AMENITY_LABELS,
  type Amenity,
} from '@/types/property';
import { formatPrice, formatArea } from '@/lib/utils';

/**
 * Página de detalle de una propiedad.
 */
export function PropertyDetailPage(): React.ReactElement {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  // Obtenemos la propiedad por ID
  const property = id ? getPropertyById(id) : undefined;

  // Si no existe la propiedad, mostramos error
  if (!property) {
    return (
      <div className="container mx-auto px-4 py-8 text-center">
        <h1 className="text-2xl font-bold mb-4">Propiedad no encontrada</h1>
        <p className="text-muted-foreground mb-6">
          La propiedad que buscas no existe o ha sido eliminada.
        </p>
        <Button asChild>
          <Link to="/">Volver al listado</Link>
        </Button>
      </div>
    );
  }

  /**
   * Maneja la eliminación de la propiedad.
   */
  const handleDelete = (): void => {
    if (window.confirm('¿Estás seguro de eliminar esta propiedad?')) {
      deleteProperty(property.id);
      navigate('/');
    }
  };

  // Imagen principal o placeholder
  const mainImage =
    property.images[0] ??
    `https://placehold.co/1200x600/e2e8f0/64748b?text=${encodeURIComponent(property.propertyType)}`;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header con navegación */}
      <div className="mb-6">
        <Button asChild variant="ghost" className="mb-4">
          <Link to="/">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver al listado
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Columna principal */}
        <div className="lg:col-span-2 space-y-6">
          {/* Imagen principal */}
          <div className="relative rounded-lg overflow-hidden">
            <img
              src={mainImage}
              alt={property.title}
              className="w-full h-[400px] object-cover"
            />
            <span
              className={`absolute top-4 left-4 px-4 py-2 text-sm font-semibold rounded-full ${
                property.operationType === 'venta'
                  ? 'bg-green-500 text-white'
                  : 'bg-blue-500 text-white'
              }`}
            >
              {OPERATION_TYPE_LABELS[property.operationType]}
            </span>
          </div>

          {/* Galería de imágenes adicionales */}
          {property.images.length > 1 && (
            <div className="grid grid-cols-4 gap-2">
              {property.images.slice(1).map((img, index) => (
                <img
                  key={index}
                  src={img}
                  alt={`${property.title} - Imagen ${index + 2}`}
                  className="w-full h-24 object-cover rounded-lg"
                />
              ))}
            </div>
          )}

          {/* Descripción */}
          <Card>
            <CardContent className="p-6">
              <h2 className="text-xl font-semibold mb-4">Descripción</h2>
              <p className="text-muted-foreground whitespace-pre-line">
                {property.description}
              </p>
            </CardContent>
          </Card>

          {/* Amenidades */}
          {property.amenities.length > 0 && (
            <Card>
              <CardContent className="p-6">
                <h2 className="text-xl font-semibold mb-4">Amenidades</h2>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                  {property.amenities.map((amenity) => (
                    <div
                      key={amenity}
                      className="flex items-center gap-2 text-muted-foreground"
                    >
                      <span className="text-green-500">✓</span>
                      {AMENITY_LABELS[amenity as Amenity]}
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Columna lateral */}
        <div className="space-y-6">
          {/* Precio y tipo */}
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center gap-2 mb-2">
                <Tag className="h-5 w-5 text-primary" />
                <span className="text-3xl font-bold text-primary">
                  {formatPrice(property.price)}
                </span>
              </div>
              {property.operationType === 'alquiler' && (
                <p className="text-muted-foreground mb-4">por mes</p>
              )}

              <div className="text-sm text-muted-foreground mb-4">
                {PROPERTY_TYPE_LABELS[property.propertyType]}
              </div>

              <h1 className="text-xl font-semibold mb-4">{property.title}</h1>

              {/* Ubicación */}
              <div className="flex items-start gap-2 text-muted-foreground mb-6">
                <MapPin className="h-4 w-4 mt-1 shrink-0" />
                <div>
                  <p>{property.address}</p>
                  <p>{property.city}</p>
                </div>
              </div>

              {/* Características */}
              <div className="grid grid-cols-3 gap-4 py-4 border-t border-b">
                {property.bedrooms > 0 && (
                  <div className="text-center">
                    <Bed className="h-5 w-5 mx-auto mb-1 text-muted-foreground" />
                    <p className="font-semibold">{property.bedrooms}</p>
                    <p className="text-xs text-muted-foreground">Habitaciones</p>
                  </div>
                )}
                {property.bathrooms > 0 && (
                  <div className="text-center">
                    <Bath className="h-5 w-5 mx-auto mb-1 text-muted-foreground" />
                    <p className="font-semibold">{property.bathrooms}</p>
                    <p className="text-xs text-muted-foreground">Baños</p>
                  </div>
                )}
                <div className="text-center">
                  <Square className="h-5 w-5 mx-auto mb-1 text-muted-foreground" />
                  <p className="font-semibold">{formatArea(property.area)}</p>
                  <p className="text-xs text-muted-foreground">Área</p>
                </div>
              </div>

              {/* Fecha de publicación */}
              <div className="flex items-center gap-2 text-sm text-muted-foreground mt-4">
                <Calendar className="h-4 w-4" />
                <span>
                  Publicado el{' '}
                  {new Date(property.createdAt).toLocaleDateString('es-ES', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Acciones */}
          <Card>
            <CardContent className="p-6 space-y-3">
              <Button className="w-full" size="lg">
                Contactar al vendedor
              </Button>
              <Button variant="outline" className="w-full">
                Agendar visita
              </Button>
              <Button
                variant="destructive"
                className="w-full"
                onClick={handleDelete}
              >
                Eliminar propiedad
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
