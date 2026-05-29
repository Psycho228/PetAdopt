/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0efff',
          100: '#e0defe',
          200: '#c2bdfd',
          300: '#a49cfc',
          400: '#867bfb',
          500: '#6C63FF',
          600: '#5A52E0',
          700: '#4841b8',
          800: '#363090',
          900: '#242068',
        },
      },
    },
  },
  plugins: [],
}
