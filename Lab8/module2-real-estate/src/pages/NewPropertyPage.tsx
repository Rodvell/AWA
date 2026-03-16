// =============================================================================
// PÁGINA: NUEVA PROPIEDAD - Real Estate React
// =============================================================================
// Página para crear una nueva propiedad inmobiliaria.
// =============================================================================

import type React from 'react';
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { PropertyForm } from '@/components/PropertyForm';
import { createProperty } from '@/lib/storage';
import type { CreatePropertyInput } from '@/types/property';

/**
 * Página para crear una nueva propiedad.
 *
 * ## Navegación programática
 * Usamos useNavigate() para redirigir después de guardar.
 * Esto es preferible a usar <Link> cuando necesitamos
 * ejecutar lógica antes de navegar.
 */
export function NewPropertyPage(): React.ReactElement {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * Maneja el envío del formulario.
   *
   * ## Flujo:
   * 1. Marcar como "enviando" para deshabilitar el botón
   * 2. Crear la propiedad en localStorage
   * 3. Redirigir a la página principal
   */
  const handleSubmit = (data: CreatePropertyInput): void => {
    setIsSubmitting(true);

    try {
      createProperty(data);
      // Mostramos toast de exito
      toast.success('Propiedad creada exitosamente', {
        description: data.title,
      });
      // Redirigimos al home después de crear
      navigate('/');
    } catch (error) {
      console.error('Error al crear propiedad:', error);
      toast.error('Error al guardar la propiedad', {
        description: 'Por favor intenta de nuevo.',
      });
      setIsSubmitting(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      {/* Header con botón de volver */}
      <div className="mb-8">
        <Button asChild variant="ghost" className="mb-4">
          <Link to="/">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver al listado
          </Link>
        </Button>

        <h1 className="text-3xl font-bold">Nueva Propiedad</h1>
        <p className="text-muted-foreground">
          Completa el formulario para publicar una nueva propiedad
        </p>
      </div>

      {/* Formulario */}
      <PropertyForm onSubmit={handleSubmit} isSubmitting={isSubmitting} />
    </div>
  );
}
