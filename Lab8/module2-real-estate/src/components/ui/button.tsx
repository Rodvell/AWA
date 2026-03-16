// =============================================================================
// COMPONENTE: BUTTON - Shadcn UI
// =============================================================================
// Componente de botón con múltiples variantes y tamaños.
//
// ## Class Variance Authority (CVA)
// CVA es una librería que facilita crear componentes con variantes.
// Define un conjunto de estilos base y variantes que se combinan.
// =============================================================================

import * as React from 'react';
import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

/**
 * Variantes del botón usando CVA.
 *
 * ## ¿Cómo funciona CVA?
 * 1. Define estilos base que siempre se aplican
 * 2. Define variantes con opciones (default, destructive, etc.)
 * 3. Define tamaños
 * 4. Combina todo automáticamente según las props
 */
const buttonVariants = cva(
  // Estilos base que siempre se aplican
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0',
  {
    variants: {
      // Variantes de estilo visual
      variant: {
        default: 'bg-primary text-primary-foreground hover:bg-primary/90',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
        outline: 'border border-input bg-background hover:bg-accent hover:text-accent-foreground',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
        ghost: 'hover:bg-accent hover:text-accent-foreground',
        link: 'text-primary underline-offset-4 hover:underline',
      },
      // Variantes de tamaño
      size: {
        default: 'h-10 px-4 py-2',
        sm: 'h-9 rounded-md px-3',
        lg: 'h-11 rounded-md px-8',
        icon: 'h-10 w-10',
      },
    },
    // Valores por defecto si no se especifican
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  }
);

/**
 * Props del componente Button.
 *
 * ## asChild Pattern
 * Cuando asChild es true, el Button renderiza su hijo directo
 * en lugar de un <button>. Útil para:
 * - Links que parecen botones: <Button asChild><Link href="/">Home</Link></Button>
 * - Componentes personalizados que necesitan estilos de botón
 */
export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

/**
 * Componente Button reutilizable con variantes.
 *
 * @example
 * // Botón por defecto
 * <Button>Click me</Button>
 *
 * // Botón destructivo grande
 * <Button variant="destructive" size="lg">Eliminar</Button>
 *
 * // Botón como link
 * <Button asChild>
 *   <Link to="/about">Sobre nosotros</Link>
 * </Button>
 */
const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    // Si asChild es true, usamos Slot que renderiza el hijo
    // Si no, renderizamos un button normal
    const Comp = asChild ? Slot : 'button';

    return (
      <Comp className={cn(buttonVariants({ variant, size, className }))} ref={ref} {...props} />
    );
  }
);
Button.displayName = 'Button';

export { Button, buttonVariants };
