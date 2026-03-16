// =============================================================================
// COMPONENTE: LABEL - Shadcn UI
// =============================================================================
// Componente de etiqueta accesible usando Radix UI.
//
// ## Radix UI
// Radix UI proporciona primitivos de UI accesibles y sin estilos.
// Shadcn UI los estiliza con Tailwind CSS.
// =============================================================================

import * as React from 'react';
import * as LabelPrimitive from '@radix-ui/react-label';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

/**
 * Variantes del label.
 */
const labelVariants = cva(
  'text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
);

/**
 * Componente Label accesible.
 *
 * ## Accesibilidad
 * - Se conecta automáticamente con inputs via htmlFor
 * - Soporta click para enfocar el input asociado
 * - Estados de disabled heredados del input hermano (peer-disabled)
 *
 * @example
 * <div>
 *   <Label htmlFor="email">Correo electrónico</Label>
 *   <Input id="email" type="email" />
 * </div>
 */
const Label = React.forwardRef<
  React.ComponentRef<typeof LabelPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof LabelPrimitive.Root> & VariantProps<typeof labelVariants>
>(({ className, ...props }, ref) => (
  <LabelPrimitive.Root ref={ref} className={cn(labelVariants(), className)} {...props} />
));
Label.displayName = LabelPrimitive.Root.displayName;

export { Label };
