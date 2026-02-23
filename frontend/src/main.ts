import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import axios from "axios";

// Настройка axios
axios.defaults.baseURL = "http://localhost:8080";
axios.defaults.headers.common["Content-Type"] = "application/json";

// JWT interceptor
axios.interceptors.request.use((config) => {
  const token = store.state.token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      store.commit("clearAuth");
      if (
        router.currentRoute.value.path !== "/login" &&
        router.currentRoute.value.path !== "/register"
      ) {
        router.push("/login");
      }
    }
    return Promise.reject(error);
  }
);

const app = createApp(App);
app.use(store);
app.use(router);
app.mount("#app");
