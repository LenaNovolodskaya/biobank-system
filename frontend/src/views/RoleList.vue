<template>
  <div class="role-page">
    <div class="page-header">
      <h2>Роли</h2>
    </div>

    <div v-if="error" class="alert alert-danger">{{ error }}</div>

    <div
      class="card table-card"
      :class="{ 'empty-table': !loading && roles.length === 0 }"
    >
      <p v-if="loading">Загрузка...</p>
      <div v-else class="table-wrapper">
        <table class="role-table">
          <thead>
            <tr>
              <th class="id-col">ID</th>
              <th class="name-col">Название роли</th>
              <th>Разрешения</th>
              <th class="actions-col">Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="role in roles" :key="role.roleId">
              <td class="mono">{{ role.roleId }}</td>
              <td class="role-name">{{ role.roleName }}</td>
              <td class="permissions-cell">
                {{
                  (role.permissionNames || [])
                    .map(getPermissionLabel)
                    .join(", ")
                }}
              </td>
              <td>
                <button
                  class="btn btn-secondary btn-sm"
                  @click="openEditModal(role)"
                >
                  Настроить
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
          <h3>Разрешения роли: {{ editForm.roleName }}</h3>
          <button
            class="btn btn-secondary"
            type="button"
            @click="closeEditModal"
          >
            Закрыть
          </button>
        </div>

        <div class="form-group">
          <label>Действия</label>
          <div class="checkbox-grid">
            <label
              v-for="perm in permissions"
              :key="perm.permissionId"
              class="checkbox-label"
            >
              <input
                type="checkbox"
                :value="perm.permissionId"
                v-model="editForm.permissionIds"
              />
              {{
                perm.permissionLabel || getPermissionLabel(perm.permissionName)
              }}
            </label>
          </div>
        </div>

        <div class="modal-actions">
          <button
            type="button"
            class="btn btn-secondary"
            @click="closeEditModal"
          >
            Отмена
          </button>
          <button type="button" class="btn btn-primary" @click="saveRole">
            Сохранить
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import { resolvePermissionLabel } from "@/utils/permissionLabels";

interface Role {
  roleId: number;
  roleName: string;
  permissionNames?: string[];
}

interface Permission {
  permissionId: number;
  permissionName: string;
  permissionLabel?: string | null;
}

export default defineComponent({
  name: "RoleList",
  data() {
    return {
      roles: [] as Role[],
      permissions: [] as Permission[],
      loading: false,
      error: "" as string,
      showEditModal: false,
      editForm: {
        roleId: 0,
        roleName: "",
        permissionIds: [] as number[],
      },
    };
  },
  async mounted() {
    await this.loadPermissions();
    await this.loadRoles();
  },
  methods: {
    getPermissionLabel(code: string) {
      return resolvePermissionLabel(code);
    },
    async loadRoles() {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await axios.get("/roles");
        this.roles = data;
      } catch {
        this.error = "Ошибка загрузки ролей";
      } finally {
        this.loading = false;
      }
    },
    async loadPermissions() {
      try {
        const { data } = await axios.get("/permissions");
        this.permissions = data;
      } catch {
        // ignore
      }
    },
    openEditModal(role: Role) {
      const permissionIds = this.permissions
        .filter((p) => (role.permissionNames || []).includes(p.permissionName))
        .map((p) => p.permissionId);
      this.editForm = {
        roleId: role.roleId,
        roleName: role.roleName,
        permissionIds,
      };
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
    },
    async saveRole() {
      try {
        await axios.put(`/roles/${this.editForm.roleId}/permissions`, {
          permissionIds: this.editForm.permissionIds,
        });
        this.closeEditModal();
        await this.loadRoles();
      } catch {
        this.error = "Ошибка сохранения";
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
.role-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 760px;
}
.role-table th,
.role-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
}
.role-table th {
  background: var(--surface);
  text-align: left;
  font-weight: 700;
  position: sticky;
  top: 0;
  z-index: 1;
}
.role-table tbody tr:hover {
  background: rgba(216, 205, 189, 0.35);
}
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    "Liberation Mono", "Courier New", monospace;
}
.id-col {
  width: 80px;
}
.actions-col {
  width: 160px;
}
.name-col {
  width: 220px;
}
.role-name {
  font-weight: 700;
}
.permissions-cell {
  max-width: 400px;
  font-size: 0.9rem;
  color: var(--text-secondary);
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
.form-group label {
  display: block;
  font-weight: 700;
  margin-bottom: 0.5rem;
}
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(240px, 1fr));
  gap: 0.5rem 1rem;
  margin-bottom: 1rem;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
.alert-danger {
  color: #c0392b;
  padding: 1rem;
  background: #fde8e8;
  border-radius: 8px;
}
</style>
