/** @type {import('tailwindcss').Config} */
module.exports = {
  // habilitamos el modo oscuro por clase
  darkMode: "class",
  // le decimos a tailwind dónde buscar clases para compilar
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#4f46e5", // indigo-600
          hover: "#4338ca", // indigo-700
        },
        surface: {
          light: "#f1f5f9", // slate-100 para fondos
          dark: "#111827", // gray-900 para fondos oscuros
          cardLight: "#ffffff", // white
          cardDark: "#1f2937", // gray-800
        },
      },
    },
  },
  plugins: [],
};
