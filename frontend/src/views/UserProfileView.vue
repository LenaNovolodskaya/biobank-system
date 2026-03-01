<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>Профиль</h2>
    </div>

    <div v-if="loading" class="subtle">Загрузка...</div>
    <div v-else-if="error" class="subtle">{{ error }}</div>

    <div v-if="user" class="card profile-card">
      <div class="profile-stack">
        <div class="info-block profile-info">
          <h3>Информация о профиле</h3>

          <div class="kv">
            <div class="k">ID</div>
            <div class="v">{{ user.userId }}</div>
          </div>
          <div class="kv">
            <div class="k">Логин</div>
            <div class="v">{{ user.username }}</div>
          </div>
          <div class="kv">
            <div class="k">ФИО</div>
            <div class="v">{{ user.fullName }}</div>
          </div>
          <router-link to="/users" class="btn btn-secondary btn-sm back-btn">
            Назад к списку пользователей
          </router-link>
        </div>

        <div class="info-block profile-perms">
          <div class="info-title-row">
            <h3>Доступные действия</h3>
            <div class="profile-actions">
              <button
                v-if="canConfigurePermissions || canAssignRoles"
                type="button"
                class="btn btn-secondary btn-sm"
                @click="openEditModal"
              >
                {{
                  canAssignRoles
                    ? "Настроить разрешения"
                    : "Настроить разрешения"
                }}
              </button>
              <button
                v-if="canDelete"
                type="button"
                class="btn btn-secondary btn-sm btn-icon-danger"
                aria-label="Удалить пользователя"
                title="Удалить пользователя"
                @click="confirmDeleteUser"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path
                    d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                    fill="currentColor"
                  />
                </svg>
              </button>
            </div>
          </div>
          <div v-if="user.permissions.length === 0" class="subtle">—</div>
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

    <div
      v-if="showEditModal"
      class="modal-overlay"
      @click.self="closeEditModal"
    >
      <div class="modal form-modal">
        <div class="modal-header">
          <h3>Настройка разрешений</h3>
          <button
            class="btn btn-secondary"
            type="button"
            @click="closeEditModal"
          >
            Закрыть
          </button>
        </div>

        <form class="form-grid" @submit.prevent="saveUserPermissions">
          <div v-if="canAssignRoles" class="form-group full-width">
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

          <div v-if="canConfigurePermissions" class="form-group full-width">
            <label>Действия пользователя</label>
            <p class="hint">
              По умолчанию активны действия, выданные ролью. Здесь можно точечно
              разрешить или запретить действие для конкретного пользователя.
            </p>
            <div v-if="permLoading" class="subtle">Загрузка действий...</div>
            <div v-else class="perm-blocks">
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
                        :disabled="permLoading || saving"
                        :checked="a.checked"
                        @change="onPermissionToggle(a.permissionId, $event)"
                      />
                      <span class="perm-action-label">{{ a.label }}</span>
                    </label>
                    <span
                      class="perm-badge"
                      :class="{ inherited: a.inherited }"
                    >
                      {{ a.inherited ? "роль" : "вне роли" }}
                    </span>
                  </div>
                </div>
              </section>
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
import { defineComponent, computed } from "vue";
import { useStore } from "vuex";
import axios from "axios";
import { resolvePermissionLabel } from "@/utils/permissionLabels";

type UserResponse = {
  userId: number;
  username: string;
  fullName: string;
  roleNames?: string[];
};

type UserPermissionMatrixItem = {
  permissionId: number;
  permissionName: string;
  permissionLabel: string | null;
  inherited: boolean;
  effective: boolean;
};

type UserProfile = UserResponse & { permissions: string[] };

type Role = {
  roleId: number;
  roleName: string;
  permissionNames?: string[];
};

export default defineComponent({
  name: "UserProfileView",
  setup() {
    const store = useStore();
    return {
      canConfigurePermissions: computed(
        () =>
          store.getters.hasPermission("user.permissions.manage") ||
          store.getters.hasPermission("user.manage")
      ),
      canAssignRoles: computed(
        () =>
          store.getters.hasPermission("user.permissions.manage") ||
          store.getters.hasPermission("user.manage")
      ),
      canDelete: computed(
        () =>
          store.getters.hasPermission("user.delete") ||
          store.getters.hasPermission("user.manage")
      ),
    };
  },
  data() {
    return {
      user: null as UserProfile | null,
      loading: false,
      error: "" as string,
      showEditModal: false,
      roles: [] as Role[],
      permissionsMatrix: [] as UserPermissionMatrixItem[],
      permissionSelection: {} as Record<number, boolean>,
      permLoading: false,
      saving: false,
      editForm: {
        userId: 0,
        roleIds: [] as number[],
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
        inherited: boolean;
        checked: boolean;
        title: string;
      }>;
    }> {
      const items = this.permissionsMatrix || [];
      const lookup = new Map<string, UserPermissionMatrixItem>();
      items.forEach((p) => lookup.set(p.permissionName, p));

      const get = (code: string) => lookup.get(code);
      const isChecked = (p: UserPermissionMatrixItem) =>
        this.permissionSelection[p.permissionId] ?? p.effective;
      const titleOf = (p: UserPermissionMatrixItem) =>
        resolvePermissionLabel(p.permissionName, p.permissionLabel);

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
              inherited: p.inherited,
              checked: isChecked(p),
              title: titleOf(p),
            };
          })
          .filter(Boolean) as Array<{
          key: string;
          label: string;
          permissionId: number;
          inherited: boolean;
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
              inherited: p.inherited,
              checked: isChecked(p),
              title: titleOf(p),
            };
          })
          .filter(Boolean) as Array<{
          key: string;
          label: string;
          permissionId: number;
          inherited: boolean;
          checked: boolean;
          title: string;
        }>;
        if (actions.length === 0) return null;
        return { key: blockKey, title, actions };
      };

      const blocks = [
        buildCrudBlock("patients", "Пациенты", "patient"),
        buildCrudBlock("researches", "Исследования", "research"),
        buildCrudBlock("visits", "Визиты", "visit"),
        buildCrudBlock("samples", "Образцы", "sample"),
        buildCrudBlock("storage", "Хранилище", "storage"),
        buildCrudBlock("references", "Справочники", "reference"),
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
          inherited: boolean;
          checked: boolean;
          title: string;
        }>;
      }>;

      return blocks;
    },
    permissionRows(): Array<{
      key: string;
      section: string;
      actions: string[];
    }> {
      return this.permissionBlocks.map((block) => ({
        key: block.key,
        section: block.title,
        actions: block.actions.filter((a) => a.checked).map((a) => a.label),
      }));
    },
  },
  mounted() {
    this.load();
  },
  watch: {
    "$route.params.userId"() {
      this.load();
    },
  },
  methods: {
    getUserId(): number | null {
      const raw = this.$route.params.userId;
      const value = typeof raw === "string" ? Number(raw) : Number.NaN;
      return Number.isFinite(value) ? value : null;
    },
    async load() {
      const userId = this.getUserId();
      if (!userId) {
        this.error = "Некорректный идентификатор пользователя";
        this.user = null;
        return;
      }

      this.loading = true;
      this.error = "";
      this.user = null;
      try {
        await this.loadRoles();
        let userData: UserResponse | null = null;
        try {
          const { data } = await axios.get(`/users/${userId}`);
          userData = data;
        } catch {
          // fallback ниже
        }
        if (!userData) {
          const { data } = await axios.get("/users");
          userData = (data || []).find(
            (u: UserResponse) => u.userId === userId
          );
        }
        if (!userData) {
          this.error = "Пользователь не найден";
          return;
        }

        let permissionsMatrix: UserPermissionMatrixItem[] = [];
        try {
          const { data: matrix } = await axios.get(
            `/users/${userId}/permissions-matrix`
          );
          permissionsMatrix = matrix as UserPermissionMatrixItem[];
        } catch {
          // ignore - user may not have permission
        }
        const permissions = permissionsMatrix
          .filter((p) => p.effective)
          .map((p) => p.permissionName)
          .slice()
          .sort();

        this.user = {
          userId: userData.userId,
          username: userData.username,
          fullName: userData.fullName,
          permissions,
        };

        const roleIds = this.roles
          .filter((r) => (userData?.roleNames || []).includes(r.roleName))
          .map((r) => r.roleId);
        this.editForm = { userId: userData.userId, roleIds };
        this.permissionsMatrix = permissionsMatrix;
        this.permissionSelection = permissionsMatrix.reduce(
          (acc, p) => ({ ...acc, [p.permissionId]: p.effective }),
          {} as Record<number, boolean>
        );
      } catch {
        this.error = "Не удалось загрузить профиль пользователя";
      } finally {
        this.loading = false;
      }
    },
    async loadRoles() {
      try {
        const { data } = await axios.get("/roles");
        this.roles = data;
      } catch {
        this.roles = [];
      }
    },
    async loadUserPermissionsMatrix(userId: number, roleIds?: number[]) {
      this.permLoading = true;
      try {
        const config: {
          params?: object;
          paramsSerializer?: (p: object) => string;
        } =
          roleIds && roleIds.length > 0
            ? {
                params: { roleIds },
                paramsSerializer: (p: { roleIds?: number[] }) =>
                  (p.roleIds || [])
                    .map((id) => `roleIds=${encodeURIComponent(id)}`)
                    .join("&"),
              }
            : {};
        const { data } = await axios.get(
          `/users/${userId}/permissions-matrix`,
          config
        );
        this.permissionsMatrix = data;
        const isPreview = roleIds && roleIds.length > 0;
        const next: Record<number, boolean> = {};
        (data as UserPermissionMatrixItem[]).forEach((p) => {
          next[p.permissionId] = isPreview ? p.inherited : p.effective;
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
    async onRolesChanged() {
      if (!this.showEditModal || !this.editForm.userId) return;
      if (this.canConfigurePermissions) {
        await this.loadUserPermissionsMatrix(
          this.editForm.userId,
          this.editForm.roleIds
        );
      }
    },
    openEditModal() {
      if (!this.user) return;
      this.showEditModal = true;
    },
    closeEditModal() {
      this.showEditModal = false;
    },
    confirmDeleteUser() {
      if (!this.user) return;
      if (!confirm(`Удалить пользователя «${this.user.fullName}»?`)) return;
      this.deleteUser();
    },
    async deleteUser() {
      if (!this.user) return;
      try {
        await axios.delete(`/users/${this.user.userId}`);
        this.$router.push("/users");
      } catch {
        this.error = "Ошибка удаления пользователя";
      }
    },
    async saveUserPermissions() {
      if (!this.user) return;
      this.saving = true;
      try {
        await axios.put(`/users/${this.user.userId}/roles`, {
          roleIds: this.editForm.roleIds,
        });

        if (this.canConfigurePermissions) {
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

          await axios.put(`/users/${this.user.userId}/permission-overrides`, {
            overrides,
          });
        }

        await this.load();
        this.closeEditModal();
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
  margin: 0 0 0.75rem 0;
}
.profile-info {
  display: flex;
  flex-direction: column;
}
.profile-info .back-btn {
  margin-top: auto;
}
.info-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 0.75rem;
}
.profile-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}
.info-title-row h3 {
  margin: 0;
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
.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(180px, 1fr));
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
  gap: 1rem;
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
  margin-right: 0.5rem;
  flex-shrink: 0;
}
.perm-action-label {
  font-weight: 600;
}
.perm-action .perm-badge {
  flex-shrink: 0;
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
@media (max-width: 980px) {
  .profile-stack {
    grid-template-columns: 1fr;
  }
  .perm-section {
    width: 100%;
  }
}
</style>
