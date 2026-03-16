// =============================================================================
// COMPONENTE: TEXTAREA - Shadcn UI
// =============================================================================
// Componente de área de texto estilizado.
// =============================================================================

import * as React from 'react';
import { cn } from '@/lib/utils';

/**
 * Props del componente Textarea.
 */
export type TextareaProps = React.TextareaHTMLAttributes<HTMLTextAreaElement>;

/**
 * Componente Textarea estilizado.
 *
 * @example
 * <Textarea
 *   placeholder="Escribe una descripción..."
 *   rows={5}
 *   {...register('description')}
 * />
 */
const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, ...props }, ref) => {
    return (
      <textarea
        className={cn(
          'flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm',
          'placeholder:text-muted-foreground',
          'ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
          'disabled:cursor-not-allowed disabled:opacity-50',
          className
        )}
        ref={ref}
        {...props}
      />
    );
  }
);
Textarea.displayName = 'Textarea';

export { Textarea };
