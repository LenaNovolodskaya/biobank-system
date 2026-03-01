<template>
  <div id="app">
    <nav class="navbar" v-if="isAuthenticated">
      <div class="nav-brand">Система учёта биообразцов</div>
      <ul class="nav-links">
        <li><router-link to="/samples">Образцы</router-link></li>
        <li><router-link to="/storage">Хранилища</router-link></li>
        <li><router-link to="/researches">Исследования</router-link></li>
        <li><router-link to="/visits">Визиты</router-link></li>
        <li><router-link to="/patients">Пациенты</router-link></li>
      </ul>
      <div class="nav-user">
        <div class="profile-menu">
          <router-link
            class="btn btn-secondary btn-sm nav-user-btn"
            to="/profile"
          >
            Профиль
          </router-link>
          <div class="profile-dropdown" role="menu" aria-label="Профиль">
            <router-link class="btn btn-sm profile-dropdown-btn" to="/profile">
              Информация о профиле
            </router-link>
            <router-link
              v-if="canManageUsers"
              class="btn btn-sm profile-dropdown-btn"
              to="/users"
            >
              Пользователи системы
            </router-link>
            <router-link
              v-if="canManageRoles"
              class="btn btn-sm profile-dropdown-btn"
              to="/roles"
            >
              Роли пользователей
            </router-link>
          </div>
        </div>
        <button class="btn btn-secondary btn-sm nav-user-btn" @click="logout">
          Выход
        </button>
      </div>
    </nav>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, onMounted } from "vue";
import { RouterLink, RouterView } from "vue-router";
import { useStore } from "vuex";
import { useRouter } from "vue-router";

export default defineComponent({
  name: "App",
  components: {
    RouterLink,
    RouterView,
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    onMounted(() => {
      if (store.getters.isAuthenticated) {
        store.dispatch("refreshPermissions");
      }
    });
    return {
      isAuthenticated: computed(() => store.getters.isAuthenticated),
      canManageUsers: computed(() => store.getters.canManageUsers),
      canManageRoles: computed(() => store.getters.canManageRoles),
      logout: () => {
        store.commit("clearAuth");
        router.push("/login").catch(() => undefined);
      },
    };
  },
});
</script>

<style>
:root {
  --bg: #ffffff;
  --surface: #f0e9df;
  --border: #d8cdbd;
  --text-primary: #2e2a25;
  --text-secondary: #554e44;
  --accent: #8b7563;
  --accent-dark: #6e5a4b;
  --shadow: 0 8px 20px rgba(40, 34, 28, 0.2);
  --focus-ring: 0 0 0 3px rgba(140, 118, 96, 0.35);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: var(--text-primary);
  background-color: var(--bg);
}

.navbar {
  background-color: var(--surface);
  color: var(--text-primary);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow);
}

.nav-brand {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  list-style: none;
  gap: 2rem;
}

.nav-links a {
  color: var(--text-primary);
  text-decoration: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.25s ease, color 0.25s ease;
}

.nav-links a:hover {
  background-color: var(--border);
}

.nav-links a.router-link-active {
  background-color: var(--accent-dark);
  color: var(--surface);
}

.nav-user {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.nav-user-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 1rem;
  font-weight: 600;
  line-height: 1;
}
.user-name {
  font-size: 0.9rem;
  color: var(--text-secondary);
}
.btn-sm {
  height: 34px;
  padding: 0 0.8rem;
  font-size: 0.85rem;
  box-sizing: border-box;
}

.profile-menu {
  position: relative;
  display: inline-flex;
  align-items: center;
}
.profile-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 240px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 12px;
  box-shadow: var(--shadow);
  padding: 0.4rem;
  margin-top: 8px;
  display: none;
  z-index: 20;
}
.profile-dropdown::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: -8px;
  height: 8px;
}
.profile-menu:hover .profile-dropdown,
.profile-menu:focus-within .profile-dropdown {
  display: block;
}
.profile-dropdown-btn {
  width: 100%;
  justify-content: flex-start;
  white-space: nowrap;
  border-radius: 10px;
  padding: 0.55rem 0.75rem;
  background-color: var(--surface);
  border-color: transparent;
  color: var(--text-primary);
  box-shadow: none;
  transform: none;
}
.profile-dropdown-btn:hover {
  background-color: var(--border);
  color: var(--text-primary);
}

.main-content {
  padding: 0.5rem 1.5rem;
  max-width: 98%;
  margin: 0 auto;
}

.btn {
  padding: 10px 18px;
  border: 1px solid transparent;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.25s ease, color 0.25s ease,
    box-shadow 0.25s ease, transform 0.25s ease;
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.btn-primary {
  background-color: var(--accent);
  color: var(--text-primary);
  box-shadow: var(--shadow);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--accent-dark);
  transform: translateY(-1px);
}

.btn-secondary {
  background-color: var(--border);
  color: var(--text-primary);
  border-color: var(--border);
}

.btn-secondary:hover:not(:disabled) {
  background-color: var(--accent);
  color: var(--text-primary);
}

.btn-nowrap {
  white-space: nowrap;
}

.btn-icon-danger {
  color: #7f3f32;
  width: 34px;
  height: 34px;
  padding: 0;
  flex-shrink: 0;
}
.btn-icon-danger svg {
  width: 18px;
  height: 18px;
}

select.form-control {
  appearance: none;
  background-color: var(--surface);
  padding-right: 32px;
  max-height: 240px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--accent-dark) var(--surface);
  background-image: linear-gradient(
      45deg,
      transparent 50%,
      var(--text-secondary) 50%
    ),
    linear-gradient(135deg, var(--text-secondary) 50%, transparent 50%),
    linear-gradient(to right, transparent, transparent);
  background-position: calc(100% - 18px) calc(50% - 2px),
    calc(100% - 12px) calc(50% - 2px), calc(100% - 32px) 50%;
  background-size: 6px 6px, 6px 6px, 1px 1.5em;
  background-repeat: no-repeat;
}

select.form-control option {
  white-space: normal;
}

select.form-control:focus {
  box-shadow: var(--focus-ring);
  border-color: var(--accent);
}
</style>
