export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  webSocketUrl: 'ws://localhost:8080',
  endpoints: {
    auth: {
      login: '/api/auth/login',
      logout: '/api/auth/logout',
      register: '/api/auth/register',
      forgotPassword: '/api/password/forgot',
      verifyToken: '/api/password/verify',
      resetPassword: '/api/password/reset',
      changePassword: '/api/password/change',
      googleAuth: '/api/oauth/google',
      verify: '/api/auth/verify'
    },
    api: {
      search: '/api/search/ticker',
      marketNews: '/api/news/market-news',
      companyNews: '/api/news/company-news',
      stockData: '/api/stocks/stock-data',
      stock: '/api/stocks/stock',
      stockClosePrice: '/api/stocks/stock-close-price'
    },
    watchlist: {
      create: '/api/watchlist/create',
      delete: '/api/watchlist/delete',
      getAll: '/api/watchlist/getAll',
      get: '/api/watchlist'
    },
    alert: {
      create: '/api/alert/create',
      delete: '/api/alert/delete'
    },
    websocket: {
      livePrices: '/live-prices'
    }
  },
  googleOAuth: {
    clientId: '695068855627-args0gpepg67h3b980qjdop5qgevr016.apps.googleusercontent.com',
    redirectUri: 'http://localhost:4200/auth/callback',
    scope: 'openid profile email'
  }
};
