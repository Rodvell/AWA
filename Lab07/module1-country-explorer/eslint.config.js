// =============================================================================
// ESLINT CONFIGURATION - Module 1: Country Explorer
// =============================================================================
// Configuración de ESLint con el nuevo formato "flat config" introducido en v9.
// Este formato reemplaza el antiguo .eslintrc.json con un archivo JavaScript.
// =============================================================================

import tseslint from 'typescript-eslint';

export default tseslint.config(
  // Configuración base de TypeScript ESLint
  ...tseslint.configs.strictTypeChecked,
  ...tseslint.configs.stylisticTypeChecked,
  {
    languageOptions: {
      parserOptions: {
        // Necesario para reglas que requieren información de tipos
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
    rules: {
      // =======================================================================
      // REGLAS PERSONALIZADAS
      // =======================================================================
      // Prohibimos el uso de 'any' - esto es crítico para aprender TypeScript
      '@typescript-eslint/no-explicit-any': 'error',

      // Preferimos interfaces sobre types para objetos (más extensibles)
      '@typescript-eslint/consistent-type-definitions': ['error', 'interface'],

      // Requiere tipos de retorno explícitos en funciones exportadas
      '@typescript-eslint/explicit-function-return-type': ['error', {
        allowExpressions: true,
        allowTypedFunctionExpressions: true,
      }],

      // Variables no utilizadas son error (excepto si empiezan con _)
      '@typescript-eslint/no-unused-vars': ['error', {
        argsIgnorePattern: '^_',
        varsIgnorePattern: '^_',
      }],
    },
  },
  {
    // Ignoramos archivos de configuración y distribución
    ignores: ['dist/**', 'node_modules/**', '*.config.js', '*.config.ts'],
  }
);
