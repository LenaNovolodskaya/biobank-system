<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-brand">Система учёта биообразцов</div>
      <ul class="nav-links">
        <li><router-link to="/samples">Образцы</router-link></li>
        <li><router-link to="/storage">Хранилища</router-link></li>
        <li><router-link to="/researches">Исследования</router-link></li>
        <li><router-link to="/visits">Визиты</router-link></li>
        <li><router-link to="/patients">Пациенты</router-link></li>
        <li><router-link to="/references">Справочники</router-link></li>
      </ul>
    </nav>

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { RouterLink, RouterView } from "vue-router";

export default defineComponent({
  name: "App",
  components: {
    RouterLink,
    RouterView,
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
  cursor: pointer;
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
