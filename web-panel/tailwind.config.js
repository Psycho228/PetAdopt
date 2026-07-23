/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#edf7f3',
          100: '#d7ede5',
          200: '#b1dccf',
          300: '#82c4b2',
          400: '#55a58f',
          500: '#3d8f7a',
          600: '#2f7d6b',
          700: '#276657',
          800: '#235247',
          900: '#1f443c',
        },
        secondary: {
          50: '#fff4ee',
          100: '#ffe1d4',
          200: '#ffc4aa',
          300: '#ffa277',
          400: '#ff8a5b',
          500: '#f36d3f',
          600: '#d95128',
          700: '#b43e1e',
          800: '#91341f',
          900: '#762f20',
        },
        warm: {
          50: '#fffbf6',
          100: '#f7f3ec',
          200: '#ede6db',
          300: '#ddd3c5',
        },
      },
    },
  },
  plugins: [],
}
