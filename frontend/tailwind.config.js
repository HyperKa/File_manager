/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                'db-black': '#09090b',
                'db-card': '#111113',
                'db-yellow': '#fbbf24',
            }
        },
    },
    plugins: [],
}