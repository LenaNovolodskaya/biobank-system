<template>
  <div class="user-page">
    <div class="page-header">
      <h2>Пользователи</h2>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div
      class="card table-card"
      :class="{ 'empty-table': !loading && users.length === 0 }"
    >
      <p v-if="loading">Загрузка...</p>
      <div v-else class="table-wrapper">
        <table class="user-table">
          <thead>
            <tr>
              <th class="id-col">ID</th>
              <th class="username-col">Имя пользователя</th>
              <th>ФИО</th>
              <th class="roles-col">Роли</th>
              <th class="active-col">Активен</th>
              <th class="actions-col">Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.userId">
              <td class="mono">{{ user.userId }}</td>
              <td class="mono">{{ user.username }}</td>
              <td>{{ user.fullName }}</td>
              <td class="roles-cell">
                {{ (user.roleNames || []).join(", ") }}
              </td>
              <td>{{ user.isActive ? "Да" : "Нет" }}</td>
              <td>
                <button
                  class="btn btn-secondary btn-sm"
                  @click="openEditModal(user)"
                >
                  Редактировать
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div
      v-if="showEditModal"
      class="modal-overlay"
      @click.self="closeEditModal"
    >
      <div class="modal form-modal">
        <div class="modal-header">
          <h3>Редактирование пользователя</h3>
          <button
            class="btn btn-secondary"
            type="button"
            @click="closeEditModal"
          >
            Закрыть
          </button>
        </div>

        <form class="form-grid" @submit.prevent="saveUser">
          <div class="form-group">
            <label>ФИО</label>
            <input
              v-model="editForm.fullName"
              type="text"
              class="form-control"
            />
          </div>

          <div class="form-group">
            <label class="checkbox-inline">
              <input v-model="editForm.isActive" type="checkbox" />
              Активен
            </label>
          </div>

          <div class="form-group full-width">
            <label>Роли</label>
            <div class="checkbox-grid">
              <label
                v-for="role in roles"
                :key="role.roleId"
                class="checkbox-label"
              >
                <input
                  type="checkbox"
                  :value="role.roleId"
                  v-model="editForm.roleIds"
                  @change="onRolesChanged"
                />
                {{ role.roleName }}
              </label>
            </div>
          </div>

          <div class="form-group full-width">
            <label>Действия пользователя</label>
            <p class="hint">
              По умолчанию активны действия, выданные ролью. Здесь можно точечно
              разрешить или запретить действие для конкретного пользователя.
            </p>
            <div v-if="permLoading" class="subtle">Загрузка действий...</div>
            <div v-else class="perm-grid">
              <label
                v-for="p in permissionsMatrix"
                :key="p.permissionId"
                class="perm-item"
                :title="p.inherited ? 'Выдано ролью' : 'Не выдано ролью'"
              >
                <input
                  type="checkbox"
                  :checked="permissionSelection[p.permissionId] ?? p.effective"
                  @change="onPermissionToggle(p.permissionId, $event)"
                />
                <span class="perm-text">{{
                  resolvePermissionLabel(p.permissionName, p.permissionLabel)
                }}</span>
                <span class="perm-badge" :class="{ inherited: p.inherited }">
                  {{ p.inherited ? "роль" : "вне роли" }}
                </span>
              </label>
            </div>
          </div>

          <div class="modal-actions full-width">
            <button
              type="button"
              class="btn btn-secondary"
              @click="closeEditModal"
            >
              Отмена
            </button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? "Сохранение..." : "Сохранить" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import { useStore } from "vuex";
import { resolvePermissionLabel as resolvePermissionLabelFn } from "@/utils/permissionLabels";

interface User {
  userId: number;
  username: string;
  fullName: string;
  isActive: boolean;
  roleNames?: string[];
}

interface Role {
  roleId: number;
  roleName: string;
  permissionNames?: string[];
}

interface UserPermissionMatrixItem {
  permissionId: number;
  permissionName: string;
  permissionLabel: string | null;
  inherited: boolean;
  effective: boolean;
}

export default defineComponent({
  name: "UserList",
  data() {
    return {
      users: [] as User[],
      roles: [] as Role[],
      loading: false,
      error: "" as string,
      showEditModal: false,
      permissionsMatrix: [] as UserPermissionMatrixItem[],
      permissionSelection: {} as Record<number, boolean>,
      permLoading: false,
      saving: false,
      editForm: {
        userId: 0,
        fullName: "",
        isActive: true,
        roleIds: [] as number[],
      },
    };
  },
  setup() {
    const store = useStore();
    return { store };
  },
  async mounted() {
    await this.loadRoles();
    await this.loadUsers();
  },
  methods: {
    resolvePermissionLabel(code: string, label?: string | null) {
      return resolvePermissionLabelFn(code, label);
    },
    async loadUsers() {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await axios.get("/users");
        this.users = data;
      } catch (e: unknown) {
        this.error = "Ошибка загрузки пользователей";
      } finally {
        this.loading = false;
      }
    },
    async loadRoles() {
      try {
        const { data } = await axios.get("/roles");
        this.roles = data;
      } catch {
        // ignore
      }
    },
    async loadUserPermissionsMatrix(userId: number, roleIds?: number[]) {
      this.permLoading = true;
      try {
        const { data } = await axios.get(
          `/users/${userId}/permissions-matrix`,
          {
            params: roleIds && roleIds.length > 0 ? { roleIds } : undefined,
          }
        );
        this.permissionsMatrix = data;
        // init selection from effective permissions
        const prev = this.permissionSelection;
        const next: Record<number, boolean> = {};
        data.forEach((p: UserPermissionMatrixItem) => {
          next[p.permissionId] =
            typeof prev[p.permissionId] === "boolean"
              ? prev[p.permissionId]
              : p.effective;
        });
        this.permissionSelection = next;
      } finally {
        this.permLoading = false;
      }
    },
    onPermissionToggle(permissionId: number, ev: Event) {
      const checked = (ev.target as HTMLInputElement).checked;
      this.permissionSelection = {
        ...this.permissionSelection,
        [permissionId]: checked,
      };
    },
    async openEditModal(user: User) {
      const roleIds = this.roles
        .filter((r) => (user.roleNames || []).includes(r.roleName))
        .map((r) => r.roleId);
      this.editForm = {
        userId: user.userId,
        fullName: user.fullName,
        isActive: user.isActive ?? true,
        roleIds,
      };
      this.showEditModal = true;
      await this.loadUserPermissionsMatrix(user.userId, roleIds);
    },
    async onRolesChanged() {
      if (!this.showEditModal || !this.editForm.userId) return;
      await this.loadUserPermissionsMatrix(
        this.editForm.userId,
        this.editForm.roleIds
      );
    },
    closeEditModal() {
      this.showEditModal = false;
    },
    async saveUser() {
      this.saving = true;
      try {
        await axios.put(`/users/${this.editForm.userId}`, {
          fullName: this.editForm.fullName,
          isActive: this.editForm.isActive,
        });
        await axios.put(`/users/${this.editForm.userId}/roles`, {
          roleIds: this.editForm.roleIds,
        });

        // build overrides: only differences vs inherited
        const overrides = this.permissionsMatrix
          .map((p) => {
            const desired =
              this.permissionSelection[p.permissionId] ?? p.effective;
            if (desired === p.inherited) return null;
            return {
              permissionId: p.permissionId,
              allowed: desired,
            };
          })
          .filter(Boolean);

        await axios.put(`/users/${this.editForm.userId}/permission-overrides`, {
          overrides,
        });

        this.closeEditModal();
        this.loadUsers();
      } catch {
        this.error = "Ошибка сохранения";
      } finally {
        this.saving = false;
      }
    },
  },
});
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.75rem;
}
.table-wrapper {
  width: 100%;
  overflow: auto;
}
.table-card.empty-table .table-wrapper {
  min-height: 160px;
}
.user-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}
.user-table th,
.user-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
}
.user-table th {
  background: var(--surface);
  text-align: left;
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 1;
}
.user-table tbody tr:hover {
  background: rgba(216, 205, 189, 0.35);
}
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    "Liberation Mono", "Courier New", monospace;
}
.roles-cell {
  color: var(--text-secondary);
  font-size: 0.92rem;
}
.id-col {
  width: 80px;
}
.active-col {
  width: 110px;
}
.actions-col {
  width: 160px;
}
.username-col {
  width: 180px;
}
.roles-col {
  width: 220px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.form-modal {
  background: var(--surface);
  border-radius: 12px;
  box-shadow: var(--shadow);
  min-width: 720px;
  max-width: min(980px, calc(100vw - 48px));
  max-height: calc(100vh - 120px);
  overflow: auto;
  padding: 1.25rem;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}
.full-width {
  grid-column: 1 / -1;
}
.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.35rem;
}
.checkbox-inline {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
}
.form-control {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 0.6rem 0.75rem;
  background: #f7f2ea;
}
.form-control:focus {
  outline: none;
  box-shadow: var(--focus-ring);
  border-color: var(--accent);
}
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  gap: 0.5rem 1rem;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}
.hint {
  margin: 0 0 0.75rem 0;
  color: var(--text-secondary);
  font-size: 0.92rem;
}
.subtle {
  color: var(--text-secondary);
}
.perm-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 0.5rem;
}
.perm-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.55rem 0.7rem;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #f7f2ea;
}
.perm-text {
  font-weight: 600;
  flex: 1;
}
.perm-badge {
  font-size: 0.8rem;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
  border: 1px solid var(--border);
  color: var(--text-secondary);
  background: var(--surface);
}
.perm-badge.inherited {
  border-color: rgba(110, 90, 75, 0.35);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 0.5rem;
}
.alert-danger {
  color: #c0392b;
  padding: 1rem;
  background: #fde8e8;
  border-radius: 8px;
}
</style>
