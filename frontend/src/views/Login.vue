<template>
  <div class="login-page">
    <div class="login-card">
      <h1>Вход в систему</h1>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Имя пользователя</label>
          <input
            id="username"
            v-model="username"
            type="text"
            required
            autocomplete="username"
          />
        </div>
        <div class="form-group">
          <label for="password">Пароль</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            autocomplete="current-password"
          />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? "Вход..." : "Войти" }}
        </button>
      </form>
      <p class="register-link">
        Нет аккаунта?
        <router-link to="/register">Зарегистрироваться</router-link>
      </p>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import axios from "axios";

export default defineComponent({
  name: "LoginView",
  data() {
    return {
      username: "",
      password: "",
      error: "" as string,
      loading: false,
    };
  },
  setup() {
    const router = useRouter();
    const store = useStore();
    return { router, store };
  },
  methods: {
    async handleLogin() {
      this.error = "";
      this.loading = true;
      try {
        const { data } = await axios.post("/auth/login", {
          username: this.username,
          password: this.password,
        });
        this.store.commit("setAuth", {
          token: data.token,
          username: data.username,
          fullName: data.fullName,
          userId: data.userId,
          permissions: data.permissions || [],
        });
        this.router.push("/");
      } catch (e: unknown) {
        const err = e as {
          response?: { data?: { message?: string }; status?: number };
        };
        this.error =
          err.response?.data?.message ||
          (err.response?.status === 401
            ? "Неверное имя пользователя или пароль"
            : "Ошибка входа");
      } finally {
        this.loading = false;
      }
    },
  },
});
</script>

<style scoped>
.login-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  background: var(--surface);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow);
  width: 100%;
  max-width: 400px;
}
.login-card h1 {
  margin-bottom: 1.5rem;
  font-size: 1.5rem;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  font-weight: 500;
}
.form-group input {
  width: 100%;
  padding: 0.6rem 0.8rem;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 1rem;
}
.form-group input:focus {
  outline: none;
  box-shadow: var(--focus-ring);
  border-color: var(--accent);
}
.error {
  color: #c0392b;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}
.login-form button {
  width: 100%;
  margin-top: 0.5rem;
}
.register-link {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.95rem;
}
.register-link a {
  color: var(--accent-dark);
  font-weight: 600;
}
</style>
