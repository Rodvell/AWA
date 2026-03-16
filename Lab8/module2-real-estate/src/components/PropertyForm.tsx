// =============================================================================
// COMPONENTE: PROPERTY FORM - Module 2: Real Estate React
// =============================================================================
//
// ## Educational Note: Formularios Modernos con React Hook Form + Zod
//
// Este componente demuestra el patrón estándar de la industria para
// formularios complejos en React. La combinación RHF + Zod resuelve
// los problemas históricos de los formularios en React.
//
// ### El Problema de los Formularios en React
//
// ```
// ┌─────────────────────────────────────────────────────────────────────────┐
// │                    EVOLUCIÓN DE FORMULARIOS EN REACT                    │
// ├─────────────────────────────────────────────────────────────────────────┤
// │                                                                          │
// │   ERA 1: Controlled Components (useState por cada campo)                │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   const [name, setName] = useState('');                                  │
// │   const [email, setEmail] = useState('');                                │
// │   const [age, setAge] = useState(0);                                     │
// │   // ... 10 más estados, 10 más handlers, re-render en cada keystroke   │
// │   ✗ Mucho boilerplate  ✗ Re-renders excesivos  ✗ Validación manual     │
// │                                                                          │
// │   ERA 2: Librerías (Formik, React Final Form)                           │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   <Formik><Field name="email" /></Formik>                                │
// │   ✓ Menos código  ✗ Bundle grande  ✗ API compleja  ✗ Re-renders        │
// │                                                                          │
// │   ERA 3: React Hook Form (actual)                                        │
// │   ─────────────────────────────────────────────────────────────────────  │
// │   const { register } = useForm();                                        │
// │   <input {...register('email')} />                                       │
// │   ✓ Mínimo boilerplate  ✓ Sin re-renders  ✓ Bundle pequeño  ✓ TypeScript│
// │                                                                          │
// └─────────────────────────────────────────────────────────────────────────┘
// ```
//
// ### ¿Por qué un Custom Resolver en lugar de @hookform/resolvers?
//
// Aunque existe `@hookform/resolvers/zod`, implementamos un resolver
// manual por razones educativas:
//
// 1. **Entender el flujo** - Ver cómo Zod y RHF se conectan internamente
// 2. **Control total** - Personalizar formato de errores, agregar logging
// 3. **Menos dependencias** - Una librería menos que mantener
// 4. **Depuración fácil** - Podemos agregar console.log y breakpoints
//
// ```typescript
// // El resolver es una función async que RHF llama en cada submit:
// resolver: async (values) => {
//   const result = schema.safeParse(values);
//   if (result.success) return { values: result.data, errors: {} };
//   return { values: {}, errors: mapZodErrors(result.error) };
// }
// ```
//
// ### Flujo de Datos del Formulario
//
// ```
// Usuario escribe → Input (no re-render) → Submit → Resolver → Zod
//                                                      │
//                   ┌─────────────────────────────────┘
//                   │
//         ┌────────▼────────┐
//         │  ¿Validación OK? │
//         └────────┬────────┘
//                  │
//        ┌────────┴────────┐
//        ▼                 ▼
//   [SUCCESS]          [ERROR]
//   onSubmit()      errors → UI
//   Guardar datos   Mostrar mensajes
// ```
//
// =============================================================================

import type React from 'react';
import { useForm } from 'react-hook-form';

import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  createPropertySchema,
  type CreatePropertyInput,
  PROPERTY_TYPES,
  OPERATION_TYPES,
  PROPERTY_TYPE_LABELS,
  OPERATION_TYPE_LABELS,
} from '@/types/property';

/**
 * Props del formulario de propiedad.
 */
interface PropertyFormProps {
  /** Valores iniciales (para edición) */
  defaultValues?: Partial<CreatePropertyInput>;
  /** Callback al enviar el formulario */
  onSubmit: (data: CreatePropertyInput) => void;
  /** Indica si está guardando */
  isSubmitting?: boolean;
}

/**
 * Formulario para crear o editar una propiedad inmobiliaria.
 *
 * ## Estructura del formulario:
 * 1. Información básica (título, descripción)
 * 2. Tipo y operación
 * 3. Precio
 * 4. Ubicación
 * 5. Características (habitaciones, baños, área)
 *
 * @example
 * <PropertyForm
 *   onSubmit={(data) => createProperty(data)}
 *   isSubmitting={loading}
 * />
 */
export function PropertyForm({
  defaultValues,
  onSubmit,
  isSubmitting = false,
}: PropertyFormProps): React.ReactElement {
  // =========================================================================
  // CONFIGURACIÓN DE REACT HOOK FORM
  // =========================================================================
  // initialize useForm with a custom resolver for maximum control and safety.
  //
  // ## ¿Por qué un Custom Resolver?
  // Aunque existen librerías como @hookform/resolvers, implementarlo manualmente
  // nos da:
  // 1. Control total sobre el formato de errores
  // 2. Independencia de versiones específicas de librerías externas
  // 3. Tipado estricto y seguro (Type Safety)
  // =========================================================================
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CreatePropertyInput>({
    // Custom Resolver: Conecta Zod con React Hook Form manualmente
    resolver: async (values) => {
      try {
        // 1. Validamos los datos usando el método seguro de Zod
        const result = createPropertySchema.safeParse(values);

        // 2. Si es exitoso, retornamos los datos limpios
        if (result.success) {
          return {
            values: result.data,
            errors: {},
          };
        }

        // 3. Si hay errores, los transformamos al formato que RHF espera
        // Zod devuelve 'issues', mapeamos cada uno a un objeto de error
        const errors = result.error.issues.reduce(
          (allErrors, currentError) => ({
            ...allErrors,
            // path[0] es el nombre del campo (ej: 'title', 'price')
            [currentError.path[0]]: {
              type: currentError.code,
              message: currentError.message,
            },
          }),
          {} as Record<string, { type: string; message: string }>
        );

        return {
          values: {},
          errors,
        };
      } catch (error) {
        // 4. Capturamos errores inesperados para evitar crashes
        console.error('Error crítico de validación:', error);
        return {
          values: {},
          errors: {
            root: {
              type: 'server',
              message: 'Error inesperado al validar el formulario',
            },
          },
        };
      }
    },
    defaultValues: {
      title: '',
      description: '',
      propertyType: 'apartamento',
      operationType: 'venta',
      price: 0,
      address: '',
      city: '',
      bedrooms: 1,
      bathrooms: 1,
      area: 50,
      amenities: [],
      images: [],
      ...defaultValues,
    },
    mode: 'onTouched', // Valida al interactuar con el campo (mejor UX)
    reValidateMode: 'onChange', // Re-valida al corregir
  });

  // Observamos valores para lógica visual (no validación)
  const propertyType = watch('propertyType');
  const operationType = watch('operationType');
  const descriptionValue = watch('description');

  /**
   * Callback de error: Se ejecuta solo si la validación falla.
   * Proporciona feedback visual inmediato al usuario via Toast.
   */
  const onValidationError = (): void => {
    toast.error('Por favor corrige los errores señalados en el formulario');
  };

  return (
    // handleSubmit gestiona el flujo: Si valida ejecuta onSubmit, si no onValidationError
    <form
      onSubmit={handleSubmit(onSubmit, onValidationError)}
      className="space-y-6"
    >
      {/* Sección: Información Básica */}
      <Card>
        <CardHeader>
          <CardTitle>Información Básica</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Título */}
          <div className="space-y-2">
            <Label htmlFor="title">Título de la propiedad *</Label>
            <Input
              id="title"
              placeholder="Ej: Elegante apartamento con vista al mar"
              {...register('title')}
            />
            {errors.title && (
              <p className="text-sm text-destructive">{errors.title.message}</p>
            )}
          </div>

          {/* Descripcion */}
          <div className="space-y-2">
            <Label htmlFor="description">Descripcion *</Label>
            <Textarea
              id="description"
              placeholder="Describe la propiedad en detalle (minimo 50 caracteres)..."
              rows={5}
              {...register('description')}
            />
            <div className="flex justify-between items-center">
              <span className={`text-sm ${(descriptionValue?.length ?? 0) < 50 ? 'text-destructive' : 'text-muted-foreground'}`}>
                {descriptionValue?.length ?? 0}/50 caracteres minimos
              </span>
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description.message}</p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Sección: Tipo y Operación */}
      <Card>
        <CardHeader>
          <CardTitle>Tipo y Operación</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Tipo de propiedad */}
          <div className="space-y-2">
            <Label>Tipo de propiedad *</Label>
            <Select
              value={propertyType}
              onValueChange={(value) =>
                setValue('propertyType', value as CreatePropertyInput['propertyType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un tipo" />
              </SelectTrigger>
              <SelectContent>
                {PROPERTY_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {PROPERTY_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.propertyType && (
              <p className="text-sm text-destructive">{errors.propertyType.message}</p>
            )}
          </div>

          {/* Tipo de operación */}
          <div className="space-y-2">
            <Label>Tipo de operación *</Label>
            <Select
              value={operationType}
              onValueChange={(value) =>
                setValue('operationType', value as CreatePropertyInput['operationType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona operación" />
              </SelectTrigger>
              <SelectContent>
                {OPERATION_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {OPERATION_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.operationType && (
              <p className="text-sm text-destructive">{errors.operationType.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Precio */}
      <Card>
        <CardHeader>
          <CardTitle>Precio</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label htmlFor="price">
              Precio {operationType === 'alquiler' ? '(mensual)' : ''} *
            </Label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                $
              </span>
              <Input
                id="price"
                type="number"
                className="pl-7"
                placeholder="0"
                {...register('price', { valueAsNumber: true })}
              />
            </div>
            {errors.price && (
              <p className="text-sm text-destructive">{errors.price.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Ubicación */}
      <Card>
        <CardHeader>
          <CardTitle>Ubicación</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="address">Dirección *</Label>
            <Input
              id="address"
              placeholder="Calle, número, piso..."
              {...register('address')}
            />
            {errors.address && (
              <p className="text-sm text-destructive">{errors.address.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="city">Ciudad *</Label>
            <Input id="city" placeholder="Ej: Madrid" {...register('city')} />
            {errors.city && (
              <p className="text-sm text-destructive">{errors.city.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Características */}
      <Card>
        <CardHeader>
          <CardTitle>Características</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-2">
            <Label htmlFor="bedrooms">Habitaciones</Label>
            <Input
              id="bedrooms"
              type="number"
              min="0"
              {...register('bedrooms', { valueAsNumber: true })}
            />
            {errors.bedrooms && (
              <p className="text-sm text-destructive">{errors.bedrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="bathrooms">Baños</Label>
            <Input
              id="bathrooms"
              type="number"
              min="0"
              {...register('bathrooms', { valueAsNumber: true })}
            />
            {errors.bathrooms && (
              <p className="text-sm text-destructive">{errors.bathrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="area">Área (m²)</Label>
            <Input
              id="area"
              type="number"
              min="1"
              {...register('area', { valueAsNumber: true })}
            />
            {errors.area && (
              <p className="text-sm text-destructive">{errors.area.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Botón de envío */}

      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Guardando...' : 'Guardar Propiedad'}
      </Button>
    </form>
  );
}
