<template>
  <div class="role-page">
    <div class="page-header">
      <h2>Роли</h2>
      <button
        v-if="canCreate"
        class="icon-button header-button"
        type="button"
        @click="openCreateModal"
        aria-label="Добавить"
        title="Добавить"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
        </svg>
      </button>
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
              <th class="permissions-col">Разрешения</th>
              <th class="action-col config-col"></th>
              <th class="action-col delete-col"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="roles.length === 0" class="empty-row">
              <td colspan="5" class="empty-state">Нет ролей</td>
            </tr>
            <tr
              v-for="role in roles"
              :key="role.roleId"
              :class="{ selected: role.roleId === selectedRoleId }"
              @click="selectRole(role.roleId)"
            >
              <td class="id-col mono">{{ role.roleId }}</td>
              <td class="name-col role-name">{{ role.roleName }}</td>
              <td class="permissions-col permissions-cell">
                <table
                  v-if="getPermissionRows(role.permissionNames || []).length"
                  class="role-perm-table"
                >
                  <tbody>
                    <tr
                      v-for="row in getPermissionRows(
                        role.permissionNames || []
                      )"
                      :key="row.key"
                    >
                      <td class="role-perm-section">{{ row.section }}</td>
                      <td class="role-perm-actions">
                        {{ row.actions.length ? row.actions.join(", ") : "—" }}
                      </td>
                    </tr>
                  </tbody>
                </table>
                <span v-else class="role-perm-empty">—</span>
              </td>
              <td class="action-col config-col">
                <button
                  v-if="canConfigurePermissions"
                  class="btn btn-secondary btn-sm btn-nowrap"
                  type="button"
                  @click.stop="openEditModal(role)"
                >
                  Настроить разрешения
                </button>
              </td>
              <td class="action-col delete-col">
                <button
                  v-if="canDelete"
                  class="btn btn-secondary btn-sm btn-icon-danger"
                  type="button"
                  aria-label="Удалить"
                  title="Удалить"
                  @click.stop="confirmDeleteRole(role)"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path
                      d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                      fill="currentColor"
                    />
                  </svg>
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
          <div class="perm-blocks">
            <section
              v-for="block in permissionBlocks"
              :key="block.key"
              class="perm-block"
            >
              <div class="perm-block-title">{{ block.title }}</div>
              <div class="perm-block-actions">
                <div
                  v-for="a in block.actions"
                  :key="a.key"
                  class="perm-action"
                  :title="a.title"
                >
                  <label class="perm-action-left">
                    <input
                      type="checkbox"
                      :checked="a.checked"
                      @change="onPermissionToggle(a.permissionId, $event)"
                    />
                    <span class="perm-action-label">{{ a.label }}</span>
                  </label>
                </div>
              </div>
            </section>
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

    <div
      v-if="showCreateModal"
      class="modal-overlay"
      @click.self="closeCreateModal"
    >
      <div class="modal form-modal">
        <div class="modal-header">
          <h3>Добавить роль</h3>
          <button
            class="btn btn-secondary"
            type="button"
            @click="closeCreateModal"
          >
            Закрыть
          </button>
        </div>
        <form @submit.prevent="saveNewRole">
          <div class="form-group">
            <label>Название роли</label>
            <input
              v-model="createForm.roleName"
              type="text"
              class="form-control"
              required
            />
          </div>
          <div class="form-group">
            <label>Действия</label>
            <div class="perm-blocks">
              <section
                v-for="block in permissionBlocks"
                :key="block.key"
                class="perm-block"
              >
                <div class="perm-block-title">{{ block.title }}</div>
                <div class="perm-block-actions">
                  <div
                    v-for="a in block.actions"
                    :key="a.key"
                    class="perm-action"
                  >
                    <label class="perm-action-left">
                      <input
                        type="checkbox"
                        :checked="
                          createForm.permissionIds.includes(a.permissionId)
                        "
                        @change="
                          onCreatePermissionToggle(a.permissionId, $event)
                        "
                      />
                      <span class="perm-action-label">{{ a.label }}</span>
                    </label>
                  </div>
                </div>
              </section>
            </div>
          </div>
          <div class="modal-actions">
            <button
              type="button"
              class="btn btn-secondary"
              @click="closeCreateModal"
            >
              Отмена
            </button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? "Сохранение..." : "Создать" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from "vue";
import { useStore } from "vuex";
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
  setup() {
    const store = useStore();
    return {
      canCreate: computed(
        () =>
          store.getters.hasPermission("role.create") ||
          store.getters.hasPermission("role.manage")
      ),
      canConfigurePermissions: computed(
        () =>
          store.getters.hasPermission("role.permissions.manage") ||
          store.getters.hasPermission("role.manage")
      ),
      canDelete: computed(
        () =>
          store.getters.hasPermission("role.delete") ||
          store.getters.hasPermission("role.manage")
      ),
    };
  },
  data() {
    return {
      roles: [] as Role[],
      permissions: [] as Permission[],
      loading: false,
      error: "" as string,
      showEditModal: false,
      showCreateModal: false,
      saving: false,
      selectedRoleId: null as number | null,
      editForm: {
        roleId: 0,
        roleName: "",
        permissionIds: [] as number[],
      },
      createForm: {
        roleName: "",
        permissionIds: [] as number[],
      },
    };
  },
  computed: {
    permissionBlocks(): Array<{
      key: string;
      title: string;
      actions: Array<{
        key: string;
        label: string;
        permissionId: number;
        checked: boolean;
        title: string;
      }>;
    }> {
      const perms = this.permissions || [];
      const lookup = new Map(perms.map((p) => [p.permissionName, p]));
      const selected = new Set(this.editForm.permissionIds || []);
      const get = (code: string) => lookup.get(code);
      const isChecked = (perm: Permission) => selected.has(perm.permissionId);
      const titleOf = (perm: Permission) =>
        perm.permissionLabel || this.getPermissionLabel(perm.permissionName);

      const buildCrudBlock = (key: string, title: string, prefix: string) => {
        const spec = [
          { key: "view", label: "просмотр" },
          { key: "create", label: "добавление" },
          { key: "update", label: "редактирование" },
          { key: "delete", label: "удаление" },
        ];
        const actions = spec
          .map((s) => {
            const p = get(`${prefix}.${s.key}`);
            if (!p) return null;
            return {
              key: `${prefix}.${s.key}`,
              label: s.label,
              permissionId: p.permissionId,
              checked: isChecked(p),
              title: titleOf(p),
            };
          })
          .filter(Boolean) as Array<{
          key: string;
          label: string;
          permissionId: number;
          checked: boolean;
          title: string;
        }>;
        if (actions.length === 0) return null;
        return { key, title, actions };
      };

      const buildCustomBlock = (
        blockKey: string,
        title: string,
        spec: Array<{ code: string; label: string }>
      ) => {
        const actions = spec
          .map((s, idx) => {
            const p = get(s.code);
            if (!p) return null;
            return {
              key: `${blockKey}-${s.label}-${idx}`,
              label: s.label,
              permissionId: p.permissionId,
              checked: isChecked(p),
              title: titleOf(p),
            };
          })
          .filter(Boolean) as Array<{
          key: string;
          label: string;
          permissionId: number;
          checked: boolean;
          title: string;
        }>;
        if (actions.length === 0) return null;
        return { key: blockKey, title, actions };
      };

      return [
        buildCrudBlock("patients", "Пациенты", "patient"),
        buildCrudBlock("researches", "Исследования", "research"),
        buildCrudBlock("visits", "Визиты", "visit"),
        buildCrudBlock("samples", "Образцы", "sample"),
        buildCrudBlock("storage", "Хранилище", "storage"),
        buildCustomBlock("references", "Справочники", [
          { code: "reference.view", label: "просмотр" },
          { code: "reference.create", label: "добавление" },
          { code: "reference.update", label: "редактирование" },
          { code: "reference.delete", label: "удаление" },
        ]),
        buildCustomBlock("roles", "Роли", [
          { code: "role.view", label: "просмотр" },
          { code: "role.create", label: "добавление" },
          { code: "role.delete", label: "удаление" },
          { code: "role.permissions.manage", label: "настройка разрешений" },
        ]),
        buildCustomBlock("users", "Пользователи", [
          { code: "user.view", label: "просмотр" },
          { code: "user.create", label: "добавление" },
          { code: "user.delete", label: "удаление" },
          { code: "user.permissions.manage", label: "настройка разрешений" },
        ]),
      ].filter(Boolean) as Array<{
        key: string;
        title: string;
        actions: Array<{
          key: string;
          label: string;
          permissionId: number;
          checked: boolean;
          title: string;
        }>;
      }>;
    },
  },
  async mounted() {
    await this.loadPermissions();
    await this.loadRoles();
  },
  methods: {
    getPermissionLabel(code: string) {
      return resolvePermissionLabel(code);
    },
    getPermissionRows(
      permissionNames: string[]
    ): Array<{ key: string; section: string; actions: string[] }> {
      const set = new Set(permissionNames);
      const has = (code: string) => set.has(code);

      const crudLabels = (prefix: string) => {
        const labels: string[] = [];
        if (has(`${prefix}.view`)) labels.push("просмотр");
        if (has(`${prefix}.create`)) labels.push("добавление");
        if (has(`${prefix}.update`)) labels.push("редактирование");
        if (has(`${prefix}.delete`)) labels.push("удаление");
        return labels;
      };

      const rows: Array<{ key: string; section: string; actions: string[] }> =
        [];

      const addCrud = (key: string, title: string, prefix: string) => {
        const labels = crudLabels(prefix);
        if (labels.length > 0)
          rows.push({ key, section: title, actions: labels });
      };

      addCrud("patients", "Пациенты", "patient");
      addCrud("researches", "Исследования", "research");
      addCrud("visits", "Визиты", "visit");
      addCrud("samples", "Образцы", "sample");
      addCrud("storage", "Хранилище", "storage");

      const refLabels: string[] = [];
      if (has("reference.view")) refLabels.push("просмотр");
      if (has("reference.create")) refLabels.push("добавление");
      if (has("reference.update")) refLabels.push("редактирование");
      if (has("reference.delete")) refLabels.push("удаление");
      if (refLabels.length > 0)
        rows.push({
          key: "references",
          section: "Справочники",
          actions: refLabels,
        });

      const roleActions: string[] = [];
      if (has("role.view")) roleActions.push("просмотр");
      if (has("role.create")) roleActions.push("добавление");
      if (has("role.delete")) roleActions.push("удаление");
      if (has("role.permissions.manage"))
        roleActions.push("настройка разрешений");
      if (roleActions.length > 0)
        rows.push({ key: "roles", section: "Роли", actions: roleActions });

      const userActions: string[] = [];
      if (has("user.view")) userActions.push("просмотр");
      if (has("user.create")) userActions.push("добавление");
      if (has("user.delete")) userActions.push("удаление");
      if (has("user.permissions.manage"))
        userActions.push("настройка разрешений");
      if (userActions.length > 0)
        rows.push({
          key: "users",
          section: "Пользователи",
          actions: userActions,
        });

      return rows;
    },
    selectRole(roleId: number) {
      this.selectedRoleId = this.selectedRoleId === roleId ? null : roleId;
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
    onPermissionToggle(permissionId: number, ev: Event) {
      const checked = (ev.target as HTMLInputElement).checked;
      const ids = [...(this.editForm.permissionIds || [])];
      if (checked) {
        if (!ids.includes(permissionId)) ids.push(permissionId);
      } else {
        const idx = ids.indexOf(permissionId);
        if (idx >= 0) ids.splice(idx, 1);
      }
      this.editForm.permissionIds = ids;
    },
    openCreateModal() {
      this.createForm = { roleName: "", permissionIds: [] };
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
    },
    onCreatePermissionToggle(permissionId: number, ev: Event) {
      const checked = (ev.target as HTMLInputElement).checked;
      const ids = [...this.createForm.permissionIds];
      if (checked) {
        if (!ids.includes(permissionId)) ids.push(permissionId);
      } else {
        const idx = ids.indexOf(permissionId);
        if (idx >= 0) ids.splice(idx, 1);
      }
      this.createForm.permissionIds = ids;
    },
    async saveNewRole() {
      this.saving = true;
      try {
        await axios.post("/roles", {
          roleName: this.createForm.roleName,
          permissionIds: this.createForm.permissionIds,
        });
        this.closeCreateModal();
        await this.loadRoles();
      } catch (e: unknown) {
        this.error =
          (e as { response?: { data?: { message?: string } } })?.response?.data
            ?.message || "Ошибка создания роли";
      } finally {
        this.saving = false;
      }
    },
    confirmDeleteRole(role: Role) {
      if (confirm(`Удалить роль «${role.roleName}»?`)) {
        this.deleteRole(role.roleId);
      }
    },
    async deleteRole(roleId: number) {
      try {
        await axios.delete(`/roles/${roleId}`);
        await this.loadRoles();
      } catch {
        this.error = "Ошибка удаления роли";
      }
    },
    openEditModal(role: Role) {
      this.selectedRoleId = role.roleId;
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
.role-page {
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
.page-header h2 {
  text-align: center;
  margin: 0;
}
.header-button {
  width: 36px;
  height: 36px;
}
.icon-button {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: #d8c9b6;
  color: var(--text-secondary);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}
.icon-button svg {
  width: 18px;
  height: 18px;
}
.icon-button:hover:not(:disabled) {
  background: #cfc1ad;
  color: var(--text-primary);
  transform: translateY(-1px);
}
.icon-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.icon-button.danger {
  color: #7f3f32;
}
.form-control {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 0.6rem 0.75rem;
  background: #f7f2ea;
}
.btn-danger {
  margin-left: 0.5rem;
}
.card {
  background-color: var(--surface);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
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
  table-layout: fixed;
  font-size: 0.95rem;
  min-width: 800px;
}
.role-table th,
.role-table td {
  padding: 10px;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
  text-align: left;
  white-space: normal;
  word-break: break-word;
}
.role-table th {
  color: var(--text-primary);
  background-color: #f0e9df;
  font-weight: 700;
}
.role-table td {
  white-space: pre-line;
}
.role-table tr.selected {
  background-color: #cfc1ad;
}
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    "Liberation Mono", "Courier New", monospace;
}
.role-table .id-col {
  width: 5%;
  max-width: 5%;
}
.role-table .name-col {
  width: 18%;
  max-width: 18%;
}
.role-name {
  font-weight: 700;
}
.role-table .permissions-col {
  width: 44%;
  max-width: 44%;
}
.permissions-cell {
  font-size: 0.95rem;
  color: var(--text-primary);
  vertical-align: top;
}
.role-perm-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  font-size: 0.9rem;
}
.role-perm-table td {
  padding: 0.4rem 0.5rem;
  border-bottom: 1px solid var(--border);
  vertical-align: top;
}
.role-perm-table tr:last-child td {
  border-bottom: none;
}
.role-perm-section {
  width: 28%;
  font-weight: 700;
  color: var(--text-primary);
  background: #f3ede4;
}
.role-perm-actions {
  color: var(--text-primary);
  font-weight: 600;
}
.role-perm-empty {
  color: var(--text-secondary);
}
.role-table th.action-col,
.role-table td.action-col {
  text-align: center;
  white-space: nowrap;
}
.role-table .config-col {
  width: 18%;
  min-width: 140px;
}
.role-table .delete-col {
  width: 5%;
  max-width: 60px;
}

.empty-state {
  padding: 16px;
  background-color: #ddd1c4;
  border-radius: 8px;
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
  max-height: 85vh;
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
.perm-blocks {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}
.perm-block {
  border: 1px solid var(--border);
  border-radius: 12px;
  background: #f7f2ea;
  padding: 0.75rem;
}
.perm-block-title {
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.perm-block-actions {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.perm-action {
  display: flex;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
  padding: 0.45rem 0.55rem;
  border: 1px solid rgba(216, 205, 189, 0.65);
  border-radius: 10px;
  background: var(--surface);
}
.perm-action-left {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  flex: 1;
  min-width: 0;
  cursor: pointer;
}
.perm-action-left input[type="checkbox"] {
  margin-right: 1rem;
  flex-shrink: 0;
}
.perm-action-label {
  font-weight: 600;
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
