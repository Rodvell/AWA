# Stack Tecnológico - Módulo 1

## Versiones de Dependencias (Diciembre 2025)

Este documento registra las versiones exactas de las dependencias utilizadas en este módulo, actualizadas en diciembre de 2025.

---

## Dependencias de Desarrollo

| Paquete              | Version  | Proposito                                      |
| -------------------- | -------- | ---------------------------------------------- |
| typescript           | ^5.7.0   | Lenguaje con tipado estatico                   |
| vite                 | ^6.0.0   | Bundler y servidor de desarrollo               |
| tailwindcss          | 4.1.8    | Framework CSS utility-first (v4)               |
| @tailwindcss/vite    | 4.1.8    | Plugin de Vite para Tailwind v4                |
| eslint               | 9.17.0   | Linter para JavaScript/TypeScript              |
| typescript-eslint    | 8.18.2   | Plugin de ESLint para TypeScript               |
| prettier             | 3.4.2    | Formateador de codigo                          |
| @types/node          | 22.12.0  | Tipos de TypeScript para Node.js               |

---

## Notas sobre las versiones

### Tailwind CSS 4.1.8

- **Nueva arquitectura**: Tailwind v4 utiliza un motor completamente nuevo escrito en Rust
- **Configuración CSS-first**: Ya no requiere `tailwind.config.js` ni `postcss.config.js`
- **Nuevo import**: `@import 'tailwindcss'` reemplaza las directivas `@tailwind`
- **Utilidades personalizadas**: `@utility` reemplaza `@layer utilities`
- **Performance**: Builds hasta 10x más rápidos que v3

### TypeScript ^5.7.0

- Version estable de la rama 5.7.x+
- Caracteristicas utilizadas: strict mode, discriminated unions, template literal types

### Vite ^6.0.0

- Bundler ultrarapido con HMR nativo
- Integracion directa con `@tailwindcss/vite`
- No requiere configuracion adicional de PostCSS

---

## Cambios vs Versión Anterior

| Aspecto              | Antes (v3)           | Ahora (v4)            |
| -------------------- | -------------------- | --------------------- |
| Tailwind CSS         | 3.4.17               | 4.1.8                 |
| PostCSS              | Requerido            | No necesario          |
| Autoprefixer         | Requerido            | Incluido en Tailwind  |
| tailwind.config.js   | Requerido            | Eliminado             |
| postcss.config.js    | Requerido            | Eliminado             |
| CSS Import           | `@tailwind base;`    | `@import 'tailwindcss'` |
| Utilidades custom    | `@layer utilities`   | `@utility nombre`     |

---

## Requisitos del Sistema

| Requisito          | Mínimo    | Recomendado |
| ------------------ | --------- | ----------- |
| Node.js            | 20.19+    | 22.12+      |
| npm                | 10.0+     | 10.9+       |

---

## API Externa

| Servicio           | Versión   | URL                              |
| ------------------ | --------- | -------------------------------- |
| REST Countries API | v3.1      | https://restcountries.com/v3.1   |

---

## Navegadores Soportados

Tailwind CSS v4 requiere navegadores modernos con soporte para `@property` y `color-mix()`:

| Navegador          | Versión Mínima |
| ------------------ | -------------- |
| Chrome             | 111+           |
| Firefox            | 128+           |
| Safari             | 16.4+          |
| Edge               | 111+           |

---

## Verificación de Versiones

Para verificar las versiones instaladas:

```bash
# TypeScript
npx tsc --version

# Vite
npx vite --version

# Node.js
node --version

# npm
npm --version
```

---

## Actualización de Dependencias

Para actualizar a las últimas versiones:

```bash
# Ver dependencias desactualizadas
npm outdated

# Actualizar dependencias (con precaución)
npm update

# Verificar que todo funciona
npm run build
```

> **Importante**: Siempre verifica que el build funcione después de actualizar dependencias.
