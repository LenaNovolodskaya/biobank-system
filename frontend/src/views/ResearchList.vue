<template>
  <div class="research-page">
    <div class="page-header">
      <h2>Исследования</h2>
      <button
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

    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <div class="card table-card">
      <p v-if="loading">Загрузка...</p>
      <div v-else-if="researches.length === 0" class="empty-state">
        Нет исследований
      </div>
      <div v-else class="table-wrapper" @click="activeHeaderHelp = null">
        <table class="research-table">
          <thead>
            <tr>
              <th>
                <div class="header-help" @click.stop>
                  <span>№</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца №"
                    @click.stop="toggleHeaderHelp('number')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'number'"
                    class="help-popover"
                  >
                    Регистрационный номер исследования
                  </div>
                </div>
              </th>
              <th>
                <div class="header-help" @click.stop>
                  <span>Название</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Название"
                    @click.stop="toggleHeaderHelp('name')"
                  >
                    ?
                  </button>
                  <div v-if="activeHeaderHelp === 'name'" class="help-popover">
                    Название темы исследования
                  </div>
                </div>
              </th>
              <th>
                <div class="header-help" @click.stop>
                  <span>Группа</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Группа"
                    @click.stop="toggleHeaderHelp('group')"
                  >
                    ?
                  </button>
                  <div v-if="activeHeaderHelp === 'group'" class="help-popover">
                    Группа пациентов, включённых в исследование
                  </div>
                </div>
              </th>
              <th>
                <div class="header-help" @click.stop>
                  <span>Финансирование</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Финансирование"
                    @click.stop="toggleHeaderHelp('financing')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'financing'"
                    class="help-popover"
                  >
                    Финансирование темы исследования
                  </div>
                </div>
              </th>
              <th class="header-help-cell">
                <div class="header-help" @click.stop>
                  <span>Подразделение</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Подразделение"
                    @click.stop="toggleHeaderHelp('department')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'department'"
                    class="help-popover"
                  >
                    Наименование подразделения, выполняющего тему исследования
                  </div>
                </div>
              </th>
              <th>
                <div class="header-help" @click.stop>
                  <span>Активно</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Активно"
                    @click.stop="toggleHeaderHelp('active')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'active'"
                    class="help-popover"
                  >
                    Статус активности темы исследования
                  </div>
                </div>
              </th>
              <th class="action-col"></th>
              <th class="action-col"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="research in researches"
              :key="research.researchId"
              :class="{ selected: research.researchId === selectedResearchId }"
              @click="selectResearch(research.researchId)"
            >
              <td>{{ research.researchNumber }}</td>
              <td class="wrap-cell">{{ research.researchName }}</td>
              <td class="wrap-cell">
                {{ getReferenceName(researchGroups, research.researchGroupId) }}
              </td>
              <td class="wrap-cell">
                {{
                  getReferenceName(financingSources, research.financingSourceId)
                }}
              </td>
              <td class="wrap-cell">
                {{ getReferenceName(departments, research.departmentId) }}
              </td>
              <td>{{ research.isActive ? "Да" : "Нет" }}</td>
              <td class="action-col">
                <button
                  class="icon-button"
                  type="button"
                  aria-label="Обновить"
                  title="Обновить"
                  @click.stop="editResearchRow(research.researchId)"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path
                      d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                      fill="currentColor"
                    />
                  </svg>
                </button>
              </td>
              <td class="action-col">
                <button
                  class="icon-button danger"
                  type="button"
                  aria-label="Удалить"
                  title="Удалить"
                  @click.stop="deleteResearchRow(research.researchId)"
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

    <div v-if="modalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal form-modal">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeModal">Закрыть</button>
        </div>

        <form class="form-grid" @submit.prevent="submitModal">
          <div class="form-group">
            <label for="modalNumber">Номер *</label>
            <input
              id="modalNumber"
              v-model="modalResearch.researchNumber"
              required
              type="text"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalName">Название *</label>
            <input
              id="modalName"
              v-model="modalResearch.researchName"
              required
              type="text"
              class="form-control"
            />
          </div>
          <div class="form-group with-action">
            <label for="modalGroup">Группа</label>
            <div class="input-action">
              <select
                id="modalGroup"
                v-model="modalResearch.researchGroupId"
                class="form-control"
              >
                <option value="">Не указано</option>
                <option
                  v-for="group in researchGroups"
                  :key="group.id"
                  :value="group.id"
                >
                  {{ group.name }}
                </option>
              </select>
              <div class="icon-actions">
                <button
                  type="button"
                  class="icon-button"
                  @click="openRefModal('group')"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button"
                  :disabled="!modalResearch.researchGroupId"
                  @click="openRefModal('group', modalResearch.researchGroupId)"
                  aria-label="Обновить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path
                      d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                      fill="currentColor"
                    />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button danger"
                  :disabled="!modalResearch.researchGroupId"
                  @click="
                    deleteRefQuick('group', modalResearch.researchGroupId)
                  "
                  aria-label="Удалить"
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
          </div>
          <div class="form-group with-action">
            <label for="modalFinance">Финансирование</label>
            <div class="input-action">
              <select
                id="modalFinance"
                v-model="modalResearch.financingSourceId"
                class="form-control"
              >
                <option value="">Не указано</option>
                <option
                  v-for="source in financingSources"
                  :key="source.id"
                  :value="source.id"
                >
                  {{ source.name }}
                </option>
              </select>
              <div class="icon-actions">
                <button
                  type="button"
                  class="icon-button"
                  @click="openRefModal('financing')"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button"
                  :disabled="!modalResearch.financingSourceId"
                  @click="
                    openRefModal('financing', modalResearch.financingSourceId)
                  "
                  aria-label="Обновить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path
                      d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                      fill="currentColor"
                    />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button danger"
                  :disabled="!modalResearch.financingSourceId"
                  @click="
                    deleteRefQuick('financing', modalResearch.financingSourceId)
                  "
                  aria-label="Удалить"
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
          </div>
          <div class="form-group with-action">
            <label for="modalDepartment">Подразделение</label>
            <div class="input-action">
              <select
                id="modalDepartment"
                v-model="modalResearch.departmentId"
                class="form-control"
              >
                <option value="">Не указано</option>
                <option
                  v-for="department in departments"
                  :key="department.id"
                  :value="department.id"
                >
                  {{ department.name }}
                </option>
              </select>
              <div class="icon-actions">
                <button
                  type="button"
                  class="icon-button"
                  @click="openRefModal('department')"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button"
                  :disabled="!modalResearch.departmentId"
                  @click="
                    openRefModal('department', modalResearch.departmentId)
                  "
                  aria-label="Обновить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path
                      d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                      fill="currentColor"
                    />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button danger"
                  :disabled="!modalResearch.departmentId"
                  @click="
                    deleteRefQuick('department', modalResearch.departmentId)
                  "
                  aria-label="Удалить"
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
          </div>
          <div class="form-group checkbox">
            <input
              id="modalActive"
              v-model="modalResearch.isActive"
              type="checkbox"
            />
            <label for="modalActive">Активно</label>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ modalMode === "edit" ? "Обновить" : "Добавить" }}
            </button>
          </div>
        </form>
      </div>
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
            <label for="refSelect">Существующая запись</label>
            <select
              id="refSelect"
              v-model.number="refModalId"
              class="form-control"
              @change="fillRefModalName"
            >
              <option :value="null">Не указано</option>
              <option
                v-for="item in refModalItems"
                :key="item.id"
                :value="item.id"
              >
                {{ item.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="refName">Название</label>
            <input
              id="refName"
              v-model="refModalName"
              type="text"
              class="form-control"
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ refModalMode === "edit" ? "Обновить" : "Добавить" }}
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

interface Research {
  researchId: number;
  researchNumber: string;
  researchName: string;
  researchGroupId: number | null;
  financingSourceId: number | null;
  departmentId: number | null;
  isActive: boolean;
}

interface ResearchForm {
  researchNumber: string;
  researchName: string;
  researchGroupId: number | null;
  financingSourceId: number | null;
  departmentId: number | null;
  isActive: boolean;
}

interface ReferenceItem {
  id: number;
  name: string;
}

export default defineComponent({
  name: "ResearchList",
  data() {
    return {
      researches: [] as Research[],
      researchGroups: [] as ReferenceItem[],
      financingSources: [] as ReferenceItem[],
      departments: [] as ReferenceItem[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      selectedResearchId: null as number | null,
      modalOpen: false,
      modalMode: "create" as "create" | "edit",
      modalTitle: "",
      modalResearch: {
        researchNumber: "",
        researchName: "",
        researchGroupId: null,
        financingSourceId: null,
        departmentId: null,
        isActive: true,
      } as ResearchForm,
      refModalOpen: false,
      refModalTitle: "",
      refModalType: "" as "group" | "financing" | "department" | "",
      refModalMode: "create" as "create" | "edit",
      refModalId: null as number | null,
      refModalName: "",
      activeHeaderHelp: null as
        | "number"
        | "name"
        | "group"
        | "financing"
        | "department"
        | "active"
        | null,
    };
  },
  computed: {
    refModalItems(): ReferenceItem[] {
      if (this.refModalType === "group") {
        return this.researchGroups;
      }
      if (this.refModalType === "financing") {
        return this.financingSources;
      }
      if (this.refModalType === "department") {
        return this.departments;
      }
      return [];
    },
  },
  created() {
    this.fetchResearches();
    this.fetchReferences();
  },
  methods: {
    toggleHeaderHelp(
      key: "number" | "name" | "group" | "financing" | "department" | "active"
    ) {
      this.activeHeaderHelp = this.activeHeaderHelp === key ? null : key;
    },
    async fetchResearches() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const response = await axios.get("/researches");
        this.researches = [...response.data].sort(
          (a: Research, b: Research) => a.researchId - b.researchId
        );
      } catch (error) {
        this.errorMessage = "Не удалось загрузить исследования";
      } finally {
        this.loading = false;
      }
    },
    async fetchReferences() {
      try {
        const [groupsResponse, financingResponse, departmentsResponse] =
          await Promise.all([
            axios.get("/references/research-groups"),
            axios.get("/references/financing-sources"),
            axios.get("/references/departments"),
          ]);
        this.researchGroups = groupsResponse.data.map(
          (item: { researchGroupId: number; researchGroupName: string }) => ({
            id: item.researchGroupId,
            name: item.researchGroupName,
          })
        );
        this.financingSources = financingResponse.data.map(
          (item: {
            financingSourceId: number;
            financingSourceName: string;
          }) => ({
            id: item.financingSourceId,
            name: item.financingSourceName,
          })
        );
        this.departments = departmentsResponse.data.map(
          (item: { departmentId: number; departmentName: string }) => ({
            id: item.departmentId,
            name: item.departmentName,
          })
        );
        this.researchGroups = this.sortByNameWithUnknown(this.researchGroups);
        this.financingSources = this.sortByNameWithUnknown(
          this.financingSources
        );
        this.departments = this.sortByNameWithUnknown(this.departments);
      } catch (error) {
        console.error("Ошибка при загрузке справочников:", error);
      }
    },
    sortByNameWithUnknown(items: ReferenceItem[]) {
      return [...items].sort((a, b) => {
        const nameA = a.name.trim();
        const nameB = b.name.trim();
        const isUnknownA = nameA.toLowerCase() === "не указано";
        const isUnknownB = nameB.toLowerCase() === "не указано";
        if (isUnknownA && !isUnknownB) return -1;
        if (!isUnknownA && isUnknownB) return 1;
        return nameA.localeCompare(nameB, "ru-RU");
      });
    },
    getReferenceName(list: ReferenceItem[], id: number | null) {
      if (!id) {
        return "Не указано";
      }
      const item = list.find((entry) => entry.id === id);
      return item?.name || "Не указано";
    },
    selectResearch(researchId: number) {
      this.selectedResearchId =
        this.selectedResearchId === researchId ? null : researchId;
    },
    editResearchRow(researchId: number) {
      this.selectedResearchId = researchId;
      this.openEditModal();
    },
    deleteResearchRow(researchId: number) {
      this.selectedResearchId = researchId;
      this.deleteResearch();
    },
    openCreateModal() {
      this.modalMode = "create";
      this.modalTitle = "Добавить исследование";
      this.modalResearch = {
        researchNumber: "",
        researchName: "",
        researchGroupId: null,
        financingSourceId: null,
        departmentId: null,
        isActive: true,
      };
      this.modalOpen = true;
    },
    openEditModal() {
      const research = this.researches.find(
        (item) => item.researchId === this.selectedResearchId
      );
      if (!research) {
        return;
      }
      this.modalMode = "edit";
      this.modalTitle = "Обновить исследование";
      this.modalResearch = {
        researchNumber: research.researchNumber,
        researchName: research.researchName,
        researchGroupId: research.researchGroupId,
        financingSourceId: research.financingSourceId,
        departmentId: research.departmentId,
        isActive: research.isActive,
      };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitModal() {
      if (this.modalMode === "create") {
        await this.createResearch();
      } else {
        await this.updateResearch();
      }
    },
    openRefModal(type: "group" | "financing" | "department", presetId = null) {
      this.refModalType = type;
      this.refModalName = "";
      this.refModalId = presetId;
      this.refModalMode = presetId ? "edit" : "create";
      if (type === "group") {
        this.refModalTitle =
          this.refModalMode === "edit" ? "Обновить группу" : "Добавить группу";
      } else if (type === "financing") {
        this.refModalTitle =
          this.refModalMode === "edit"
            ? "Обновить источник финансирования"
            : "Добавить источник финансирования";
      } else {
        this.refModalTitle =
          this.refModalMode === "edit"
            ? "Обновить подразделение"
            : "Добавить подразделение";
      }
      if (this.refModalId) {
        this.fillRefModalName();
      }
      this.refModalOpen = true;
    },
    closeRefModal() {
      this.refModalOpen = false;
    },
    fillRefModalName() {
      const item = this.refModalItems.find(
        (entry) => entry.id === this.refModalId
      );
      this.refModalName = item?.name || "";
    },
    async submitRefModal() {
      if (!this.refModalName.trim()) {
        return;
      }
      if (this.refModalMode === "edit") {
        await this.updateRefModal();
        return;
      }
      try {
        if (this.refModalType === "group") {
          await axios.post("/references/research-groups", {
            name: this.refModalName,
          });
        }
        if (this.refModalType === "financing") {
          await axios.post("/references/financing-sources", {
            name: this.refModalName,
          });
        }
        if (this.refModalType === "department") {
          await axios.post("/references/departments", {
            name: this.refModalName,
          });
        }
        await this.fetchReferences();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении справочника";
      }
    },
    async updateRefModal() {
      if (!this.refModalId || !this.refModalName.trim()) {
        return;
      }
      try {
        if (this.refModalType === "group") {
          await axios.put(`/references/research-groups/${this.refModalId}`, {
            name: this.refModalName,
          });
        }
        if (this.refModalType === "financing") {
          await axios.put(`/references/financing-sources/${this.refModalId}`, {
            name: this.refModalName,
          });
        }
        if (this.refModalType === "department") {
          await axios.put(`/references/departments/${this.refModalId}`, {
            name: this.refModalName,
          });
        }
        await this.fetchReferences();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении справочника";
      }
    },
    async deleteRefModal() {
      if (!this.refModalId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранную запись?");
      if (!confirmed) {
        return;
      }
      try {
        if (this.refModalType === "group") {
          await axios.delete(`/references/research-groups/${this.refModalId}`);
        }
        if (this.refModalType === "financing") {
          await axios.delete(
            `/references/financing-sources/${this.refModalId}`
          );
        }
        if (this.refModalType === "department") {
          await axios.delete(`/references/departments/${this.refModalId}`);
        }
        await this.fetchReferences();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении справочника";
      }
    },
    async deleteRefQuick(
      type: "group" | "financing" | "department",
      id: number | null
    ) {
      if (!id) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранную запись?");
      if (!confirmed) {
        return;
      }
      try {
        if (type === "group") {
          await axios.delete(`/references/research-groups/${id}`);
          if (this.modalResearch.researchGroupId === id) {
            this.modalResearch.researchGroupId = null;
          }
        }
        if (type === "financing") {
          await axios.delete(`/references/financing-sources/${id}`);
          if (this.modalResearch.financingSourceId === id) {
            this.modalResearch.financingSourceId = null;
          }
        }
        if (type === "department") {
          await axios.delete(`/references/departments/${id}`);
          if (this.modalResearch.departmentId === id) {
            this.modalResearch.departmentId = null;
          }
        }
        await this.fetchReferences();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении справочника";
      }
    },
    async createResearch() {
      try {
        await axios.post("/researches", this.modalResearch);
        this.successMessage = "Исследование добавлено";
        this.closeModal();
        await this.fetchResearches();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении исследования";
      }
    },
    async updateResearch() {
      if (!this.selectedResearchId) {
        return;
      }
      try {
        await axios.put(
          `/researches/${this.selectedResearchId}`,
          this.modalResearch
        );
        this.successMessage = "Исследование обновлено";
        this.closeModal();
        await this.fetchResearches();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении исследования";
      }
    },
    async deleteResearch() {
      if (!this.selectedResearchId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранное исследование?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/researches/${this.selectedResearchId}`);
        this.successMessage = "Исследование удалено";
        this.selectedResearchId = null;
        await this.fetchResearches();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении исследования";
      }
    },
  },
});
</script>

<style scoped>
.research-page {
  max-width: 1400px;
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
  margin-bottom: 0;
}

.card {
  background-color: var(--surface);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.form-actions {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.table-wrapper {
  width: 100%;
  overflow: auto;
}

.research-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.research-table th,
.research-table td {
  border-bottom: 1px solid var(--border);
  padding: 10px;
  text-align: left;
  white-space: nowrap;
}

.header-help {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  position: relative;
}

.help-button {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: #d8c9b6;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1;
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.help-button:hover {
  background: #cfc1ad;
  color: var(--text-primary);
  transform: translateY(-1px);
}

.help-popover {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  min-width: 220px;
  max-width: 320px;
  background: #151d23;
  color: #f2ede6;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  line-height: 1.3;
  box-shadow: 0 12px 24px rgba(16, 24, 30, 0.25);
  z-index: 5;
  white-space: normal;
}

.help-popover::before {
  content: "";
  position: absolute;
  top: -6px;
  left: 10px;
  width: 12px;
  height: 12px;
  background: #151d23;
  transform: rotate(45deg);
}

.research-table td.wrap-cell {
  white-space: normal;
  max-width: 260px;
  word-break: break-word;
}

.research-table th.action-col,
.research-table td.action-col {
  width: 44px;
  text-align: center;
}

.header-button {
  width: 36px;
  height: 36px;
}

.research-table th {
  color: var(--text-primary);
  background-color: #f0e9df;
}

.research-table tr.selected {
  background-color: #cfc1ad;
}

.empty-state {
  padding: 16px;
  background-color: #ddd1c4;
  border-radius: 8px;
  color: var(--text-secondary);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-weight: 600;
}

.with-action .input-action {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: center;
}

.icon-actions {
  display: flex;
  gap: 6px;
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
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background-color: #e9dfd2;
  transition: border-color 0.25s ease, box-shadow 0.25s ease;
}

.form-control:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: var(--focus-ring);
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
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
  width: min(860px, 100%);
  background: var(--surface);
  border-radius: 12px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  padding: 20px;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.form-modal {
  min-height: 600px;
  max-height: calc(100vh - 120px);
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.alert {
  padding: 12px;
  border-radius: 6px;
  max-width: 420px;
  margin: 0 auto 16px;
  text-align: center;
}

.alert-success {
  background-color: rgba(127, 63, 50, 0.12);
  color: #733d31;
  border: 1px solid #7f3f32;
}

.alert-danger {
  background-color: rgba(127, 63, 50, 0.12);
  color: #733d31;
  border: 1px solid #7f3f32;
}
.alert-success,
.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}
</style>
