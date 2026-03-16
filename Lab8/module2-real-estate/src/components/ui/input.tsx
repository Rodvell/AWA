// =============================================================================
// COMPONENTE: INPUT - Shadcn UI
// =============================================================================
// Componente de input estilizado con Tailwind CSS.
//
// ## forwardRef
// Usamos forwardRef para permitir que componentes padres accedan
// al elemento DOM del input (necesario para focus, validación, etc.)
// =============================================================================

import * as React from 'react';
import { cn } from '@/lib/utils';

/**
 * Props del componente Input.
 * Hereda todas las props nativas de <input>.
 */
export type InputProps = React.InputHTMLAttributes<HTMLInputElement>;

/**
 * Componente Input estilizado.
 *
 * ## Estilos aplicados:
 * - Border con estado de focus usando ring
 * - Placeholder con color muted
 * - Estado disabled con opacidad reducida
 * - Soporte para archivos con estilos especiales
 *
 * @example
 * <Input
 *   type="email"
 *   placeholder="correo@ejemplo.com"
 *   {...register('email')}
 * />
 */
const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        className={cn(
          // Estilos base
          'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm',
          // Estilos de placeholder
          'placeholder:text-muted-foreground',
          // Estilos de focus usando ring (mejor que outline)
          'ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
          // Estados
          'disabled:cursor-not-allowed disabled:opacity-50',
          // Estilos especiales para input de archivos
          'file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground',
          // Clases adicionales del usuario
          className
        )}
        ref={ref}
        {...props}
      />
    );
  }
);
Input.displayName = 'Input';

export { Input };
