<template>
  <div class="references-page">
    <div class="page-header">
      <h2>Справочники</h2>
    </div>

    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <div class="ref-sections">
      <section class="card ref-section">
        <div class="section-header">
          <h3>Национальности</h3>
          <div class="action-buttons">
            <button
              v-if="canCreate"
              class="icon-button"
              title="Добавить"
              aria-label="Добавить"
              @click="openNationalityModal"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
              </svg>
            </button>
          </div>
        </div>
        <ul class="ref-list">
          <li
            v-for="item in nationalities"
            :key="item.nationalityId"
            class="ref-item"
          >
            <span>{{ item.nationalityName }}</span>
            <div class="item-actions">
              <button
                v-if="canUpdate"
                class="icon-button small"
                title="Изменить"
                @click="editNationality(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button
                v-if="canDelete"
                class="icon-button small danger"
                title="Удалить"
                @click="deleteNationality(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                    fill="currentColor"
                  />
                </svg>
              </button>
            </div>
          </li>
        </ul>
      </section>

      <section class="card ref-section">
        <div class="section-header">
          <h3>Типы операций с образцами</h3>
          <div class="action-buttons">
            <button
              v-if="canCreate"
              class="icon-button"
              title="Добавить"
              aria-label="Добавить"
              @click="openTransactionTypeModal"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
              </svg>
            </button>
          </div>
        </div>
        <ul class="ref-list">
          <li
            v-for="item in transactionTypes"
            :key="item.transactionTypeId"
            class="ref-item"
          >
            <span>{{ item.transactionTypeName }}</span>
            <div class="item-actions">
              <button
                v-if="canUpdate"
                class="icon-button small"
                title="Изменить"
                @click="editTransactionType(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button
                v-if="canDelete"
                class="icon-button small danger"
                title="Удалить"
                @click="deleteTransactionType(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                    fill="currentColor"
                  />
                </svg>
              </button>
            </div>
          </li>
        </ul>
      </section>

      <section class="card ref-section">
        <div class="section-header">
          <h3>Типы образцов</h3>
          <div class="action-buttons">
            <button
              v-if="canCreate"
              class="icon-button"
              title="Добавить"
              aria-label="Добавить"
              @click="openSampleTypeModal"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
              </svg>
            </button>
          </div>
        </div>
        <ul class="ref-list">
          <li
            v-for="item in sampleTypes"
            :key="item.sampleTypeId"
            class="ref-item"
          >
            <img
              v-if="item.iconPath"
              :src="sampleTypeIconUrl(item.iconPath)"
              :alt="item.sampleTypeName"
              class="type-icon"
            />
            <span>{{ item.sampleTypeName }}</span>
            <div class="item-actions">
              <button
                v-if="canUpdate"
                class="icon-button small"
                title="Изменить"
                @click="editSampleType(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button
                v-if="canDelete"
                class="icon-button small danger"
                title="Удалить"
                @click="deleteSampleType(item)"
              >
                <svg viewBox="0 0 24 24">
                  <path
                    d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                    fill="currentColor"
                  />
                </svg>
              </button>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <div v-if="refModalOpen" class="modal-overlay" @click.self="closeRefModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ refModalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeRefModal">
            Закрыть
          </button>
        </div>
        <form class="form-grid" @submit.prevent="submitRefModal">
          <div class="form-group">
            <label for="refName">Название *</label>
            <input
              id="refName"
              v-model="refModalName"
              type="text"
              required
              class="form-control"
              placeholder="Введите значение"
            />
          </div>
          <div v-if="refModalType === 'sample-type'" class="form-group">
            <label for="refIconPath">Путь к иконке</label>
            <input
              id="refIconPath"
              v-model="refModalIconPath"
              type="text"
              class="form-control"
              placeholder="например: blood.svg"
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ refModalId ? "Обновить" : "Добавить" }}
            </button>
            <button
              v-if="!refModalId"
              type="button"
              class="btn btn-secondary"
              @click="resetRefForm"
            >
              Очистить
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

interface Nationality {
  nationalityId: number;
  nationalityName: string;
}

interface SampleType {
  sampleTypeId: number;
  sampleTypeName: string;
  iconPath: string | null;
}

interface TransactionType {
  transactionTypeId: number;
  transactionTypeName: string;
}

export default defineComponent({
  name: "ReferencesView",
  setup() {
    const store = useStore();
    return {
      canCreate: computed(
        () =>
          store.getters.hasPermission("reference.create") ||
          store.getters.hasPermission("reference.manage")
      ),
      canUpdate: computed(
        () =>
          store.getters.hasPermission("reference.update") ||
          store.getters.hasPermission("reference.manage")
      ),
      canDelete: computed(
        () =>
          store.getters.hasPermission("reference.delete") ||
          store.getters.hasPermission("reference.manage")
      ),
    };
  },
  data() {
    return {
      nationalities: [] as Nationality[],
      sampleTypes: [] as SampleType[],
      transactionTypes: [] as TransactionType[],
      successMessage: "",
      errorMessage: "",
      refModalOpen: false,
      refModalType: null as
        | "nationality"
        | "sample-type"
        | "transaction-type"
        | null,
      refModalTitle: "",
      refModalId: null as number | null,
      refModalName: "",
      refModalIconPath: "",
    };
  },
  created() {
    this.fetchData();
  },
  methods: {
    sampleTypeIconUrl(iconPath: string): string {
      return `/img/sample-types/${iconPath}`;
    },
    async fetchData() {
      try {
        const [natRes, typesRes, txTypesRes] = await Promise.all([
          axios.get("/references/nationalities"),
          axios.get("/references/sample-types"),
          axios.get("/references/transaction-types"),
        ]);
        this.nationalities = natRes.data;
        this.sampleTypes = typesRes.data;
        this.transactionTypes = txTypesRes.data;
      } catch (err) {
        console.error(err);
        this.errorMessage = "Не удалось загрузить справочники";
      }
    },
    openNationalityModal() {
      this.refModalType = "nationality";
      this.refModalTitle = "Добавить национальность";
      this.refModalId = null;
      this.refModalName = "";
      this.refModalIconPath = "";
      this.refModalOpen = true;
    },
    openTransactionTypeModal() {
      this.refModalType = "transaction-type";
      this.refModalTitle = "Добавить тип операции";
      this.refModalId = null;
      this.refModalName = "";
      this.refModalIconPath = "";
      this.refModalOpen = true;
    },
    openSampleTypeModal() {
      this.refModalType = "sample-type";
      this.refModalTitle = "Добавить тип образца";
      this.refModalId = null;
      this.refModalName = "";
      this.refModalIconPath = "";
      this.refModalOpen = true;
    },
    resetRefForm() {
      this.refModalName = "";
      this.refModalIconPath = "";
    },
    editNationality(item: Nationality) {
      this.refModalType = "nationality";
      this.refModalTitle = "Изменить национальность";
      this.refModalId = item.nationalityId;
      this.refModalName = item.nationalityName;
      this.refModalIconPath = "";
      this.refModalOpen = true;
    },
    editTransactionType(item: TransactionType) {
      this.refModalType = "transaction-type";
      this.refModalTitle = "Изменить тип операции";
      this.refModalId = item.transactionTypeId;
      this.refModalName = item.transactionTypeName;
      this.refModalIconPath = "";
      this.refModalOpen = true;
    },
    editSampleType(item: SampleType) {
      this.refModalType = "sample-type";
      this.refModalTitle = "Изменить тип образца";
      this.refModalId = item.sampleTypeId;
      this.refModalName = item.sampleTypeName;
      this.refModalIconPath = item.iconPath || "";
      this.refModalOpen = true;
    },
    async deleteNationality(item: Nationality) {
      if (!window.confirm(`Удалить «${item.nationalityName}»?`)) return;
      try {
        await axios.delete(`/references/nationalities/${item.nationalityId}`);
        this.successMessage = "Национальность удалена";
        await this.fetchData();
      } catch (err) {
        this.errorMessage = "Ошибка при удалении";
      }
    },
    async deleteTransactionType(item: TransactionType) {
      if (!window.confirm(`Удалить «${item.transactionTypeName}»?`)) return;
      try {
        await axios.delete(
          `/references/transaction-types/${item.transactionTypeId}`
        );
        this.successMessage = "Тип операции удалён";
        await this.fetchData();
      } catch (err) {
        this.errorMessage = "Ошибка при удалении";
      }
    },
    async deleteSampleType(item: SampleType) {
      if (!window.confirm(`Удалить тип «${item.sampleTypeName}»?`)) return;
      try {
        await axios.delete(`/references/sample-types/${item.sampleTypeId}`);
        this.successMessage = "Тип образца удалён";
        await this.fetchData();
      } catch (err) {
        this.errorMessage = "Ошибка при удалении";
      }
    },
    closeRefModal() {
      this.refModalOpen = false;
    },
    async submitRefModal() {
      this.errorMessage = "";
      this.successMessage = "";
      try {
        if (this.refModalType === "nationality") {
          if (this.refModalId) {
            await axios.put(`/references/nationalities/${this.refModalId}`, {
              name: this.refModalName,
            });
            this.successMessage = "Национальность обновлена";
          } else {
            await axios.post("/references/nationalities", {
              name: this.refModalName,
            });
            this.successMessage = "Национальность добавлена";
          }
        } else if (this.refModalType === "transaction-type") {
          if (this.refModalId) {
            await axios.put(
              `/references/transaction-types/${this.refModalId}`,
              { name: this.refModalName }
            );
            this.successMessage = "Тип операции обновлён";
          } else {
            await axios.post("/references/transaction-types", {
              name: this.refModalName,
            });
            this.successMessage = "Тип операции добавлен";
          }
        } else if (this.refModalType === "sample-type") {
          const body: Record<string, string> = { name: this.refModalName };
          if (this.refModalIconPath.trim())
            body.iconPath = this.refModalIconPath.trim();
          if (this.refModalId) {
            await axios.put(
              `/references/sample-types/${this.refModalId}`,
              body
            );
            this.successMessage = "Тип образца обновлён";
          } else {
            await axios.post("/references/sample-types", body);
            this.successMessage = "Тип образца добавлен";
          }
        }
        this.closeRefModal();
        await this.fetchData();
      } catch (err) {
        this.errorMessage = "Ошибка при сохранении";
      }
    },
  },
});
</script>

<style scoped>
.references-page {
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

.ref-sections {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.card {
  background-color: var(--surface);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
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
}

.icon-button svg {
  width: 18px;
  height: 18px;
}

.icon-button.small {
  width: 28px;
  height: 28px;
}

.icon-button.small svg {
  width: 14px;
  height: 14px;
}

.icon-button.danger {
  color: #7f3f32;
}

.ref-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ref-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
}

.ref-item:last-child {
  border-bottom: none;
}

.type-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.item-actions {
  margin-left: auto;
  display: flex;
  gap: 6px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(22, 35, 43, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal {
  background: var(--surface);
  border-radius: 12px;
  padding: 20px;
  min-width: 340px;
  box-shadow: var(--shadow);
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-weight: 600;
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: #e9dfd2;
}

.form-actions {
  margin-top: 16px;
}

.alert {
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.alert-success {
  background: #cfc1ad;
  color: var(--text-primary);
}

.alert-danger {
  background: rgba(127, 63, 50, 0.12);
  color: #733d31;
}
</style>
