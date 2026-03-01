<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>Профиль</h2>
    </div>

    <div v-if="loading" class="subtle">Загрузка...</div>
    <div v-else-if="error" class="subtle">{{ error }}</div>

    <div v-if="me" class="card profile-card">
      <div class="profile-stack">
        <div class="info-block profile-info">
          <h3>Информация о профиле</h3>

          <div class="kv">
            <div class="k">ID</div>
            <div class="v">{{ me.userId }}</div>
          </div>
          <div class="kv">
            <div class="k">Логин</div>
            <div class="v">{{ me.username }}</div>
          </div>
          <div class="kv">
            <div class="k">ФИО</div>
            <div class="v">{{ me.fullName }}</div>
          </div>
        </div>

        <div class="info-block profile-perms">
          <h3>Доступные действия</h3>
          <div v-if="me.permissions.length === 0" class="subtle">—</div>
          <table v-else class="perm-table">
            <tbody>
              <tr v-for="row in permissionRows" :key="row.key">
                <td class="perm-section">{{ row.section }}</td>
                <td class="perm-actions">
                  {{ row.actions.length ? row.actions.join(", ") : "—" }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";

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
  computed: {
    permissionRows(): Array<{
      key: string;
      section: string;
      actions: string[];
    }> {
      const permissions = new Set(this.me?.permissions || []);
      const actionOrder: Array<{ key: string; label: string }> = [
        { key: "view", label: "просмотр" },
        { key: "create", label: "добавление" },
        { key: "update", label: "редактирование" },
        { key: "delete", label: "удаление" },
      ];

      const getActions = (prefix: string) => {
        const allowed = new Set<string>();

        for (const a of actionOrder) {
          if (permissions.has(`${prefix}.${a.key}`)) allowed.add(a.label);
        }

        // reference.manage (и подобное) считаем расширенными правами на изменение
        if (permissions.has(`${prefix}.manage`)) {
          allowed.add("просмотр");
          allowed.add("добавление");
          allowed.add("редактирование");
          allowed.add("удаление");
        }

        return actionOrder
          .map((a) => a.label)
          .filter((label) => allowed.has(label));
      };

      const getUserActions = () => {
        const allowed: string[] = [];
        const hasAny = (codes: string[]) =>
          codes.some((c) => permissions.has(c));
        const hasManage = permissions.has("user.manage");

        if (hasManage || hasAny(["user.view"])) allowed.push("просмотр");
        if (hasManage || hasAny(["user.create"])) allowed.push("добавление");
        if (hasManage || hasAny(["user.delete"])) allowed.push("удаление");
        if (
          hasManage ||
          hasAny([
            "user.permission.manage",
            "user.permissions.manage",
            "user.permissions.update",
            "user.permission.update",
          ])
        ) {
          allowed.push("настройка разрешений");
        }

        return allowed;
      };

      const getRoleActions = () => {
        const allowed: string[] = [];
        const hasAny = (codes: string[]) =>
          codes.some((c) => permissions.has(c));
        const hasManage = permissions.has("role.manage");

        if (hasManage || hasAny(["role.view"])) allowed.push("просмотр");
        if (hasManage || hasAny(["role.create"])) allowed.push("добавление");
        if (hasManage || hasAny(["role.delete"])) allowed.push("удаление");
        if (
          hasManage ||
          hasAny([
            "role.permission.manage",
            "role.permissions.manage",
            "role.permissions.update",
            "role.permission.update",
          ])
        ) {
          allowed.push("настройка разрешений");
        }

        return allowed;
      };

      return [
        {
          key: "patients",
          section: "Пациенты",
          actions: getActions("patient"),
        },
        {
          key: "researches",
          section: "Исследования",
          actions: getActions("research"),
        },
        { key: "visits", section: "Визиты", actions: getActions("visit") },
        { key: "samples", section: "Образцы", actions: getActions("sample") },
        {
          key: "storage",
          section: "Хранилище",
          actions: getActions("storage"),
        },
        {
          key: "references",
          section: "Справочники",
          actions: getActions("reference"),
        },
        {
          key: "roles",
          section: "Роли",
          actions: getRoleActions(),
        },
        {
          key: "users",
          section: "Пользователи",
          actions: getUserActions(),
        },
      ];
    },
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
  },
});
</script>

<style scoped>
.profile-page {
  max-width: 98%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
h2 {
  text-align: center;
  color: var(--text-primary);
  margin: 0;
}
.profile-card {
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.subtle {
  color: var(--text-secondary);
  font-size: 0.95rem;
}
.profile-stack {
  display: grid;
  grid-template-columns: 25% 1fr;
  gap: 1rem;
}
.info-block {
  background: #f7f2ea;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 1rem;
}
.info-block h3 {
  margin: 0;
  margin-bottom: 0.75rem;
}
.kv {
  display: grid;
  grid-template-columns: 25% 1fr;
  gap: 0.5rem 1rem;
  padding: 0.35rem 0;
}
.k {
  color: var(--text-primary);
  font-weight: 700;
}
.v {
  color: var(--text-primary);
  font-weight: 400;
}

.perm-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
}
.perm-table td {
  padding: 0.65rem 0.75rem;
  border-bottom: 1px solid var(--border);
  vertical-align: top;
}
.perm-table tr:last-child td {
  border-bottom: none;
}
.perm-section {
  width: 25%;
  font-weight: 700;
  color: var(--text-primary);
  background: #f3ede4;
}
.perm-actions {
  color: var(--text-secondary);
  font-weight: 600;
}
@media (max-width: 980px) {
  .profile-stack {
    grid-template-columns: 1fr;
  }
  .perm-section {
    width: 100%;
  }
}
</style>
