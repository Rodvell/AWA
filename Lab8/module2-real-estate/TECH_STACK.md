# Stack Tecnológico - Módulo 2

## Versiones de Dependencias (Diciembre 2025)

Este documento registra las versiones exactas de las dependencias utilizadas en este módulo, actualizadas en diciembre de 2025.

---

## Dependencias de Produccion

| Paquete                    | Version  | Proposito                                      |
| -------------------------- | -------- | ---------------------------------------------- |
| react                      | 19.2.1   | Biblioteca UI                                  |
| react-dom                  | 19.2.1   | Renderizado React para DOM                     |
| react-router-dom           | 7.1.1    | Enrutamiento cliente                           |
| react-hook-form            | 7.54.2   | Gestion de formularios                         |
| @hookform/resolvers        | 3.9.1    | Integracion Zod con React Hook Form            |
| zod                        | 4.1.9    | Validacion de esquemas en runtime              |
| @radix-ui/react-dialog     | 1.1.4    | Componente Dialog accesible                    |
| @radix-ui/react-label      | 2.1.1    | Componente Label accesible                     |
| @radix-ui/react-select     | 2.1.4    | Componente Select accesible                    |
| @radix-ui/react-slot       | 1.1.1    | Utilidad para composición                      |
| @radix-ui/react-toast      | 1.2.4    | Notificaciones toast                           |
| class-variance-authority   | 0.7.1    | Variantes de componentes                       |
| clsx                       | 2.1.1    | Utilidad para clases condicionales             |
| tailwind-merge             | 2.6.0    | Merge de clases Tailwind                       |
| tw-animate-css             | 1.2.5    | Animaciones CSS para Tailwind v4               |
| lucide-react               | 0.469.0  | Iconos React                                   |

---

## Dependencias de Desarrollo

| Paquete              | Version  | Proposito                                      |
| -------------------- | -------- | ---------------------------------------------- |
| typescript           | 5.7.3    | Lenguaje con tipado estatico                   |
| vite                 | 6.4.1    | Bundler y servidor de desarrollo               |
| @vitejs/plugin-react | 4.3.4    | Plugin React para Vite                         |
| tailwindcss          | 4.1.8    | Framework CSS utility-first (v4)               |
| @tailwindcss/vite    | 4.1.8    | Plugin de Vite para Tailwind v4                |
| eslint               | 9.17.0   | Linter para JavaScript/TypeScript              |
| typescript-eslint    | 8.18.2   | Plugin de ESLint para TypeScript               |
| prettier             | 3.4.2    | Formateador de codigo                          |
| @types/react         | 19.2.1   | Tipos de TypeScript para React                 |
| @types/react-dom     | 19.2.1   | Tipos de TypeScript para React DOM             |
| @types/node          | 22.12.0  | Tipos de TypeScript para Node.js               |

---

## Notas sobre las versiones

### Tailwind CSS 4.1.8

- **Nueva arquitectura**: Motor Oxide escrito en Rust, builds 10x más rápidos
- **Configuración CSS-first**: Ya no requiere `tailwind.config.js`
- **Plugin Vite nativo**: `@tailwindcss/vite` integrado directamente
- **Compatibilidad Shadcn**: Variables CSS en `@theme inline`

### tw-animate-css (reemplaza tailwindcss-animate)

- Plugin de animaciones compatible con Tailwind v4
- Import directo: `@import 'tw-animate-css'`

### React 19.2.1

- Componentes de servidor (opcional)
- Hooks mejorados
- Mejor manejo de errores

---

## Cambios vs Versión Anterior

| Aspecto              | Antes (v3)           | Ahora (v4)            |
| -------------------- | -------------------- | --------------------- |
| Tailwind CSS         | 3.4.17               | 4.1.8                 |
| PostCSS              | Requerido            | No necesario          |
| Autoprefixer         | Requerido            | Incluido en Tailwind  |
| tailwind.config.js   | Requerido            | Eliminado             |
| postcss.config.js    | Requerido            | Eliminado             |
| tailwindcss-animate  | 1.0.7                | tw-animate-css 1.2.5  |
| CSS Import           | `@tailwind base;`    | `@import 'tailwindcss'` |
| Tema Shadcn          | JS config            | `@theme inline`       |

---

## Requisitos del Sistema

| Requisito          | Mínimo    | Recomendado |
| ------------------ | --------- | ----------- |
| Node.js            | 20.19+    | 22.12+      |
| npm                | 10.0+     | 10.9+       |

---

## Navegadores Soportados

Tailwind CSS v4 requiere navegadores modernos:

| Navegador          | Versión Mínima |
| ------------------ | -------------- |
| Chrome             | 111+           |
| Firefox            | 128+           |
| Safari             | 16.4+          |
| Edge               | 111+           |

---

## Verificación de Versiones

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
