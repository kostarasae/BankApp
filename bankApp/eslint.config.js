import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  globalIgnores(['dist']),
  {
    // The components are .jsx, so linting only .ts/.tsx checked almost nothing —
    // including the react-hooks rules, which are the ones worth having here.
    files: ['**/*.{js,jsx,ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      globals: globals.browser,
    },
    rules: {
      // Both of these fire on patterns that work correctly here, so they are kept
      // visible as warnings rather than failing the build:
      //
      // set-state-in-effect flags the load-on-mount hooks. Fetching in an effect and
      // setting loading state is what they are for; the properly reactive alternative
      // is a data-fetching library, which is its own piece of work (see J.2).
      'react-hooks/set-state-in-effect': 'warn',
      // only-export-components flags AuthContext exporting useAuth next to the
      // provider. It only affects hot reload during development.
      'react-refresh/only-export-components': 'warn',
    },
  },
  {
    // Tests run under Vitest, which supplies describe/it/expect as globals
    files: ['**/*.{test,spec}.{js,jsx,ts,tsx}', 'src/test/**'],
    languageOptions: {
      globals: { ...globals.browser, ...globals.node },
    },
  },
])