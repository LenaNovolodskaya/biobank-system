<template>
  <div class="profile-page">
    <div class="card profile-card">
      <div class="profile-header">
        <div>
          <h2>Профиль</h2>
          <p class="subtle" v-if="loading">Загрузка...</p>
          <p class="subtle" v-else-if="error">{{ error }}</p>
        </div>
      </div>

      <div class="profile-stack" v-if="me">
        <div class="info-block">
          <div class="info-title-row">
            <h3>Информация</h3>
            <div class="admin-actions" v-if="canManageUsers || canManageRoles">
              <router-link
                v-if="canManageUsers"
                class="btn btn-secondary btn-sm"
                to="/users"
              >
                Пользователи
              </router-link>
              <router-link
                v-if="canManageRoles"
                class="btn btn-secondary btn-sm"
                to="/roles"
              >
                Роли
              </router-link>
            </div>
          </div>

          <div class="kv">
            <div class="k">ID</div>
            <div class="v">{{ me.userId }}</div>
          </div>
          <div class="kv">
            <div class="k">Имя пользователя</div>
            <div class="v">{{ me.username }}</div>
          </div>
          <div class="kv">
            <div class="k">ФИО</div>
            <div class="v">{{ me.fullName }}</div>
          </div>
        </div>

        <div class="info-block">
          <h3>Доступные действия</h3>
          <div v-if="me.permissions.length === 0" class="subtle">—</div>
          <ul v-else class="perm-list perm-grid">
            <li v-for="p in me.permissions" :key="p" class="perm-item">
              {{ getPermissionLabel(p) }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import { useStore } from "vuex";
import { resolvePermissionLabel } from "@/utils/permissionLabels";

type MeResponse = {
  username: string;
  fullName: string;
  userId: number;
  permissions: string[];
};

export default defineComponent({
  name: "ProfileView",
  data() {
    return {
      me: null as MeResponse | null,
      loading: false,
      error: "" as string,
    };
  },
  setup() {
    const store = useStore();
    return {
      canManageUsers: store.getters.canManageUsers,
      canManageRoles: store.getters.canManageRoles,
    };
  },
  mounted() {
    this.loadMe();
  },
  methods: {
    async loadMe() {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await axios.get("/auth/me");
        // normalize + sort for nicer UI
        const permissions = (data.permissions || []).slice().sort();
        this.me = {
          username: data.username,
          fullName: data.fullName,
          userId: data.userId,
          permissions,
        };
      } catch {
        this.error = "Не удалось загрузить профиль";
      } finally {
        this.loading = false;
      }
    },
    getPermissionLabel(code: string) {
      return resolvePermissionLabel(code);
    },
  },
});
</script>

<style scoped>
.profile-page {
  padding: 1rem 0;
}
.profile-card {
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.profile-header {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.subtle {
  color: var(--text-secondary);
  font-size: 0.95rem;
}
.profile-stack {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}
.info-block {
  background: #f7f2ea;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1rem;
}
.info-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}
.admin-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.info-block h3 {
  margin: 0;
}
.kv {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 0.5rem 1rem;
  padding: 0.35rem 0;
}
.k {
  color: var(--text-secondary);
}
.v {
  font-weight: 600;
}
.perm-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.35rem;
}
.perm-grid {
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 0.5rem;
}
.perm-item {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 0.5rem 0.75rem;
}
@media (max-width: 980px) {
  .kv {
    grid-template-columns: 140px 1fr;
  }
}
</style>
