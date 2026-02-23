<template>
  <div class="register-page">
    <div class="register-card">
      <h1>Регистрация</h1>
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label for="username">Имя пользователя</label>
          <input
            id="username"
            v-model="username"
            type="text"
            required
            minlength="3"
            autocomplete="username"
          />
        </div>
        <div class="form-group">
          <label for="fullName">ФИО</label>
          <input
            id="fullName"
            v-model="fullName"
            type="text"
            required
            autocomplete="name"
          />
        </div>
        <div class="form-group">
          <label for="password">Пароль</label>
          <input
            id="password"
            v-model="password"
            type="password"
            required
            minlength="6"
            autocomplete="new-password"
          />
          <small>Минимум 6 символов</small>
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? "Регистрация..." : "Зарегистрироваться" }}
        </button>
      </form>
      <p class="login-link">
        Уже есть аккаунт? <router-link to="/login">Войти</router-link>
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
  name: "RegisterView",
  data() {
    return {
      username: "",
      fullName: "",
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
    async handleRegister() {
      this.error = "";
      this.loading = true;
      try {
        const { data } = await axios.post("/auth/register", {
          username: this.username,
          fullName: this.fullName,
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
        const err = e as { response?: { data?: { message?: string } } };
        this.error = err.response?.data?.message || "Ошибка регистрации";
      } finally {
        this.loading = false;
      }
    },
  },
});
</script>

<style scoped>
.register-page {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.register-card {
  background: var(--surface);
  padding: 2rem;
  border-radius: 12px;
  box-shadow: var(--shadow);
  width: 100%;
  max-width: 400px;
}
.register-card h1 {
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
.form-group small {
  display: block;
  margin-top: 0.3rem;
  color: var(--text-secondary);
  font-size: 0.85rem;
}
.error {
  color: #c0392b;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}
.register-form button {
  width: 100%;
  margin-top: 0.5rem;
}
.login-link {
  margin-top: 1.5rem;
  text-align: center;
  font-size: 0.95rem;
}
.login-link a {
  color: var(--accent-dark);
  font-weight: 600;
}
</style>
