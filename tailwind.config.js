module.exports = {
  content: [
    './src/main/resources/static/**/*.html',
    './src/main/resources/static/**/*.js',
    './src/main/resources/templates/**/*.html',
    './src/main/java/**/*.java'
  ],
  theme: {
    extend: {
      colors: {
        kosta: '#1f3c88',
        'kosta-text': '#eaf0f0'
      }
    }
  },
  safelist: [],
  plugins: []
}
