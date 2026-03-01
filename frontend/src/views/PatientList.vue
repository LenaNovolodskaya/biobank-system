<template>
  <div class="patient-list-container">
    <div class="page-header">
      <h2>Пациенты</h2>
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

    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <p v-if="loading">Загрузка...</p>
    <div v-else-if="patients.length === 0" class="empty-state">
      Нет пациентов в системе
    </div>
    <div v-else class="patients-grid">
      <div
        v-for="patient in patients"
        :key="patient.patientId"
        class="patient-card"
        :class="{ selected: patient.patientId === selectedPatientId }"
        @click="selectPatient(patient.patientId)"
      >
        <div class="card-action-buttons" v-if="canUpdate || canDelete">
          <button
            v-if="canUpdate"
            class="icon-button"
            type="button"
            aria-label="Обновить"
            title="Обновить"
            @click.stop="editPatientFromCard(patient.patientId)"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                fill="currentColor"
              />
            </svg>
          </button>
          <button
            v-if="canDelete"
            class="icon-button danger"
            type="button"
            aria-label="Удалить"
            title="Удалить"
            @click.stop="deletePatientFromCard(patient.patientId)"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                fill="currentColor"
              />
            </svg>
          </button>
        </div>
        <h3>Пациент {{ patient.patientId }}</h3>
        <div class="barcode-block">
          <BarcodeSvg :value="patient.patientBarcode" />
          <div class="barcode-text">{{ patient.patientBarcode }}</div>
        </div>
        <p><strong>ID:</strong> {{ patient.patientId }}</p>
        <p><strong>Пол:</strong> {{ getGenderText(patient.gender) }}</p>
        <p>
          <strong>Дата рождения:</strong> {{ formatDate(patient.birthDate) }}
        </p>
        <p>
          <strong>Национальность:</strong>
          {{ patient.nationalityName || "Не указано" }}
        </p>
        <p>
          <strong>Основной диагноз:</strong>
          {{ getDiagnosisDisplay(patient.mainDiagnosisId) }}
        </p>
        <div class="comorbid-toggle-block">
          <button
            type="button"
            class="btn-link"
            @click.stop="toggleComorbidVisibility(patient.patientId)"
          >
            {{ getComorbidToggleLabel(patient) }}
          </button>
          <div
            v-if="showComorbidPatientId === patient.patientId"
            class="comorbid-list"
          >
            <template v-if="getComorbidDiagnosisLabels(patient).length > 0">
              <div
                v-for="(label, idx) in getComorbidDiagnosisLabels(patient)"
                :key="idx"
                class="comorbid-item"
              >
                {{ label }}
              </div>
            </template>
            <div v-else class="comorbid-empty">—</div>
          </div>
        </div>
        <p class="nowrap">
          <strong>Дата регистрации:</strong>
          {{ formatDateTime(patient.createdAtPatient) }}
        </p>
        <p>
          <strong>Согласие:</strong>
          {{ patient.informedConsent ? "Да" : "Нет" }}
        </p>
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
            <label for="patientBarcode">Штрихкод пациента *</label>
            <input
              id="patientBarcode"
              v-model="modalPatient.patientBarcode"
              type="text"
              required
              class="form-control"
              placeholder="Введите штрихкод"
            />
          </div>

          <div class="form-group">
            <label>Пол пациента *</label>
            <div class="gender-options">
              <label class="radio-label">
                <input
                  v-model="modalPatient.gender"
                  type="radio"
                  value="MALE"
                  required
                />
                Мужской
              </label>
              <label class="radio-label">
                <input
                  v-model="modalPatient.gender"
                  type="radio"
                  value="FEMALE"
                  required
                />
                Женский
              </label>
              <label class="radio-label">
                <input
                  v-model="modalPatient.gender"
                  type="radio"
                  value="UNKNOWN"
                  required
                />
                Не указано
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="birthDate">Дата рождения *</label>
            <input
              id="birthDate"
              v-model="modalPatient.birthDate"
              type="date"
              required
              class="form-control"
              :max="maxDate"
            />
          </div>

          <div class="form-group">
            <label for="createdAtPatient">Дата регистрации *</label>
            <input
              id="createdAtPatient"
              v-model="modalPatient.createdAtPatient"
              type="datetime-local"
              required
              class="form-control"
            />
          </div>

          <div class="form-group with-action">
            <label for="nationality">Национальность</label>
            <div class="input-action">
              <select
                id="nationality"
                v-model="modalPatient.nationalityId"
                class="form-control"
              >
                <option :value="null">Не указано</option>
                <option
                  v-for="nationality in nationalities"
                  :key="nationality.nationalityId"
                  :value="nationality.nationalityId"
                >
                  {{ nationality.nationalityName }}
                </option>
              </select>
              <div
                v-if="canCreateRef || canUpdateRef || canDeleteRef"
                class="icon-actions"
              >
                <button
                  v-if="canCreateRef"
                  type="button"
                  class="icon-button"
                  @click="openNationalityModal()"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  v-if="canUpdateRef"
                  type="button"
                  class="icon-button"
                  :disabled="!modalPatient.nationalityId"
                  @click="openNationalityModal(modalPatient.nationalityId)"
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
                  v-if="canDeleteRef"
                  type="button"
                  class="icon-button danger"
                  :disabled="!modalPatient.nationalityId"
                  @click="deleteNationalityQuick(modalPatient.nationalityId)"
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
            <label for="mainDiagnosis">Основной диагноз</label>
            <div class="input-action">
              <select
                id="mainDiagnosis"
                v-model="modalPatient.mainDiagnosisId"
                class="form-control"
              >
                <option :value="null">Не указано</option>
                <option
                  v-for="diagnosis in diagnoses"
                  :key="diagnosis.diagnosisId"
                  :value="diagnosis.diagnosisId"
                >
                  {{ diagnosis.diagnosisName }}
                </option>
              </select>
              <div
                v-if="canCreateRef || canUpdateRef || canDeleteRef"
                class="icon-actions"
              >
                <button
                  v-if="canCreateRef"
                  type="button"
                  class="icon-button"
                  @click="openDiagnosisModal(null, 'main')"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  v-if="canUpdateRef"
                  type="button"
                  class="icon-button"
                  :disabled="!modalPatient.mainDiagnosisId"
                  @click="
                    openDiagnosisModal(modalPatient.mainDiagnosisId, null)
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
                  v-if="canDeleteRef"
                  type="button"
                  class="icon-button danger"
                  :disabled="!modalPatient.mainDiagnosisId"
                  @click="deleteDiagnosisQuick(modalPatient.mainDiagnosisId)"
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
            <label for="modalComorbidDiagnoses">Сопутствующие диагнозы</label>
            <div class="input-action">
              <div id="modalComorbidDiagnoses" class="multi-select">
                <label
                  v-for="diagnosis in diagnoses"
                  :key="diagnosis.diagnosisId"
                  class="checkbox-label"
                >
                  <input
                    type="checkbox"
                    :value="diagnosis.diagnosisId"
                    v-model="modalPatient.comorbidDiagnosisIds"
                  />
                  {{ diagnosis.diagnosisName }}
                </label>
              </div>
              <div
                v-if="canCreateRef || canUpdateRef || canDeleteRef"
                class="icon-actions"
              >
                <button
                  v-if="canCreateRef"
                  type="button"
                  class="icon-button"
                  @click="openDiagnosisModal(null, 'comorbid')"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  v-if="canUpdateRef"
                  type="button"
                  class="icon-button"
                  :disabled="!getSingleComorbidDiagnosisId()"
                  @click="
                    openDiagnosisModal(getSingleComorbidDiagnosisId(), null)
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
                  v-if="canDeleteRef"
                  type="button"
                  class="icon-button danger"
                  :disabled="!getSingleComorbidDiagnosisId()"
                  @click="deleteDiagnosisQuick(getSingleComorbidDiagnosisId())"
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

          <div class="form-group">
            <label class="checkbox-label">
              <input v-model="modalPatient.informedConsent" type="checkbox" />
              Информированное согласие на использование биоматериала
            </label>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ modalMode === "edit" ? "Обновить" : "Добавить" }}
            </button>
            <button type="button" class="btn btn-secondary" @click="resetForm">
              Очистить
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
            <label for="refName">Название</label>
            <input
              id="refName"
              v-model="refNationalityName"
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
    <div
      v-if="diagModalOpen"
      class="modal-overlay"
      @click.self="closeDiagnosisModal"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>{{ diagModalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeDiagnosisModal">
            Закрыть
          </button>
        </div>
        <form class="form-grid" @submit.prevent="submitDiagnosisModal">
          <div class="form-group">
            <label for="diagIcd">Код МКБ‑10</label>
            <input
              id="diagIcd"
              v-model="diagDiagnosisCode"
              type="text"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="diagName">Диагноз *</label>
            <input
              id="diagName"
              v-model="diagDiagnosisName"
              type="text"
              required
              class="form-control"
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ diagModalMode === "edit" ? "Обновить" : "Добавить" }}
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
import BarcodeSvg from "@/components/BarcodeSvg.vue";

interface Patient {
  patientId: number;
  patientBarcode: string;
  gender: string;
  birthDate: string;
  nationalityId?: number | null;
  nationalityName?: string;
  mainDiagnosisId?: number | null;
  comorbidDiagnosisIds?: number[];
  informedConsent: boolean;
  createdAtPatient: string;
}

interface PatientForm {
  patientBarcode: string;
  gender: string;
  birthDate: string;
  createdAtPatient: string;
  nationalityId: number | null;
  mainDiagnosisId: number | null;
  comorbidDiagnosisIds: number[];
  informedConsent: boolean;
}

interface Nationality {
  nationalityId: number;
  nationalityName: string;
}

interface DiagnosisRef {
  diagnosisId: number;
  icd10Code: string | null;
  diagnosisName: string;
}

export default defineComponent({
  name: "PatientList",
  components: {
    BarcodeSvg,
  },
  setup() {
    const store = useStore();
    return {
      canCreate: computed(() => store.getters.hasPermission("patient.create")),
      canUpdate: computed(() => store.getters.hasPermission("patient.update")),
      canDelete: computed(() => store.getters.hasPermission("patient.delete")),
      canCreateRef: computed(
        () =>
          store.getters.hasPermission("reference.create") ||
          store.getters.hasPermission("reference.manage")
      ),
      canUpdateRef: computed(
        () =>
          store.getters.hasPermission("reference.update") ||
          store.getters.hasPermission("reference.manage")
      ),
      canDeleteRef: computed(
        () =>
          store.getters.hasPermission("reference.delete") ||
          store.getters.hasPermission("reference.manage")
      ),
    };
  },
  data() {
    return {
      patients: [] as Patient[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      selectedPatientId: null as number | null,
      modalOpen: false,
      modalMode: "create" as "create" | "edit",
      modalTitle: "",
      modalPatient: {
        patientBarcode: "",
        gender: "UNKNOWN",
        birthDate: "",
        createdAtPatient: "",
        nationalityId: null,
        mainDiagnosisId: null,
        comorbidDiagnosisIds: [],
        informedConsent: true,
      } as PatientForm,
      nationalities: [] as Nationality[],
      diagnoses: [] as DiagnosisRef[],
      refModalOpen: false,
      refModalTitle: "",
      refModalMode: "create" as "create" | "edit",
      refNationalityId: null as number | null,
      refNationalityName: "",
      diagModalOpen: false,
      diagModalTitle: "",
      diagModalMode: "create" as "create" | "edit",
      diagDiagnosisId: null as number | null,
      diagDiagnosisCode: "",
      diagDiagnosisName: "",
      diagnosisAddContext: null as "main" | "comorbid" | null,
      showComorbidPatientId: null as number | null,
    };
  },
  computed: {
    maxDate(): string {
      const today = new Date();
      return today.toISOString().split("T")[0];
    },
  },
  created() {
    this.fetchPatients();
    this.fetchNationalities();
    this.fetchDiagnoses();
  },
  methods: {
    async fetchPatients() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const response = await axios.get("/patients");
        this.patients = [...response.data].sort(
          (a: Patient, b: Patient) => a.patientId - b.patientId
        );
      } catch (error) {
        console.error("Ошибка при загрузке пациентов:", error);
        this.errorMessage = "Не удалось загрузить список пациентов";
      } finally {
        this.loading = false;
      }
    },
    async fetchNationalities() {
      try {
        const response = await axios.get("/references/nationalities");
        const filtered = response.data.filter(
          (item: Nationality) =>
            item.nationalityName.trim().toLowerCase() !== "не указано"
        );
        this.nationalities = this.sortByNameWithUnknown(
          filtered,
          (item) => item.nationalityName
        );
      } catch (error) {
        console.error("Ошибка при загрузке национальностей:", error);
      }
    },
    async fetchDiagnoses() {
      try {
        const response = await axios.get("/references/diagnoses");
        this.diagnoses = [...response.data].sort(
          (a: DiagnosisRef, b: DiagnosisRef) =>
            a.diagnosisName.localeCompare(b.diagnosisName, "ru-RU")
        );
      } catch (error) {
        console.error("Ошибка при загрузке диагнозов:", error);
      }
    },
    openDiagnosisModal(
      presetId: number | null = null,
      addContext: "main" | "comorbid" | null = null
    ) {
      this.diagDiagnosisId = presetId;
      this.diagnosisAddContext = addContext;
      this.diagDiagnosisCode = "";
      this.diagDiagnosisName = "";
      this.diagModalMode = presetId ? "edit" : "create";
      this.diagModalTitle =
        this.diagModalMode === "edit" ? "Обновить диагноз" : "Добавить диагноз";
      if (this.diagDiagnosisId) {
        this.fillDiagnosisModal();
      }
      this.diagModalOpen = true;
    },
    closeDiagnosisModal() {
      this.diagModalOpen = false;
      this.diagnosisAddContext = null;
    },
    fillDiagnosisModal() {
      const diagnosis = this.diagnoses.find(
        (item) => item.diagnosisId === this.diagDiagnosisId
      );
      this.diagDiagnosisCode = diagnosis?.icd10Code || "";
      this.diagDiagnosisName = diagnosis?.diagnosisName || "";
    },
    async submitDiagnosisModal() {
      if (!this.diagDiagnosisName.trim()) {
        return;
      }
      if (this.diagModalMode === "edit") {
        await this.updateDiagnosisModal();
        return;
      }
      try {
        const response = await axios.post("/references/diagnoses", {
          icd10Code: this.diagDiagnosisCode,
          diagnosisName: this.diagDiagnosisName,
        });
        const created = response.data as DiagnosisRef | undefined;
        if (created?.diagnosisId) {
          if (this.diagnosisAddContext === "main") {
            this.modalPatient.mainDiagnosisId = created.diagnosisId;
          } else if (this.diagnosisAddContext === "comorbid") {
            if (
              !this.modalPatient.comorbidDiagnosisIds.includes(
                created.diagnosisId
              )
            ) {
              this.modalPatient.comorbidDiagnosisIds = [
                ...this.modalPatient.comorbidDiagnosisIds,
                created.diagnosisId,
              ];
            }
          }
        }
        await this.fetchDiagnoses();
        this.closeDiagnosisModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении диагноза";
      }
    },
    async updateDiagnosisModal() {
      if (!this.diagDiagnosisId || !this.diagDiagnosisName.trim()) {
        return;
      }
      try {
        await axios.put(`/references/diagnoses/${this.diagDiagnosisId}`, {
          icd10Code: this.diagDiagnosisCode,
          diagnosisName: this.diagDiagnosisName,
        });
        await this.fetchDiagnoses();
        this.closeDiagnosisModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении диагноза";
      }
    },
    async deleteDiagnosisQuick(id: number | null) {
      if (!id) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранный диагноз?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/references/diagnoses/${id}`);
        if (this.modalPatient.mainDiagnosisId === id) {
          this.modalPatient.mainDiagnosisId = null;
        }
        this.modalPatient.comorbidDiagnosisIds =
          this.modalPatient.comorbidDiagnosisIds.filter((x) => x !== id);
        await this.fetchDiagnoses();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении диагноза";
      }
    },
    openNationalityModal(presetId: number | null = null) {
      this.refNationalityId = presetId;
      this.refNationalityName = "";
      this.refModalMode = presetId ? "edit" : "create";
      this.refModalTitle =
        this.refModalMode === "edit"
          ? "Обновить национальность"
          : "Добавить национальность";
      if (this.refNationalityId) {
        this.fillRefModalName();
      }
      this.refModalOpen = true;
    },
    closeRefModal() {
      this.refModalOpen = false;
    },
    fillRefModalName() {
      const item = this.nationalities.find(
        (entry) => entry.nationalityId === this.refNationalityId
      );
      this.refNationalityName = item?.nationalityName || "";
    },
    async submitRefModal() {
      if (!this.refNationalityName.trim()) {
        return;
      }
      if (this.refModalMode === "edit") {
        await this.updateRefModal();
        return;
      }
      try {
        const response = await axios.post("/references/nationalities", {
          name: this.refNationalityName,
        });
        const created = response.data as Nationality | undefined;
        if (created?.nationalityId) {
          this.modalPatient.nationalityId = created.nationalityId;
        }
        await this.fetchNationalities();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении национальности";
      }
    },
    async updateRefModal() {
      if (!this.refNationalityId || !this.refNationalityName.trim()) {
        return;
      }
      try {
        await axios.put(`/references/nationalities/${this.refNationalityId}`, {
          name: this.refNationalityName,
        });
        await this.fetchNationalities();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении национальности";
      }
    },
    async deleteRefModal() {
      if (!this.refNationalityId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранную национальность?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(
          `/references/nationalities/${this.refNationalityId}`
        );
        if (this.modalPatient.nationalityId === this.refNationalityId) {
          this.modalPatient.nationalityId = null;
        }
        await this.fetchNationalities();
        this.closeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении национальности";
      }
    },
    async deleteNationalityQuick(id: number | null) {
      if (!id) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранную национальность?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/references/nationalities/${id}`);
        if (this.modalPatient.nationalityId === id) {
          this.modalPatient.nationalityId = null;
        }
        await this.fetchNationalities();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении национальности";
      }
    },
    selectPatient(patientId: number) {
      this.selectedPatientId =
        this.selectedPatientId === patientId ? null : patientId;
    },
    editPatientFromCard(patientId: number) {
      this.selectedPatientId = patientId;
      this.openEditModal();
    },
    deletePatientFromCard(patientId: number) {
      this.selectedPatientId = patientId;
      this.deletePatient();
    },
    openCreateModal() {
      this.modalMode = "create";
      this.modalTitle = "Добавить пациента";
      this.resetForm();
      this.modalPatient.createdAtPatient = this.getNowDateTimeLocal();
      this.modalOpen = true;
    },
    openEditModal() {
      if (!this.selectedPatientId) return;
      const patient = this.patients.find(
        (item) => item.patientId === this.selectedPatientId
      );
      if (!patient) return;
      this.modalMode = "edit";
      this.modalTitle = "Обновить пациента";
      this.modalPatient = {
        patientBarcode: patient.patientBarcode,
        gender: patient.gender,
        birthDate: patient.birthDate,
        createdAtPatient: this.toDateTimeLocal(patient.createdAtPatient),
        nationalityId: patient.nationalityId ?? null,
        mainDiagnosisId: patient.mainDiagnosisId ?? null,
        comorbidDiagnosisIds: patient.comorbidDiagnosisIds ?? [],
        informedConsent: patient.informedConsent,
      };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitModal() {
      if (this.modalMode === "create") {
        await this.createPatient();
      } else {
        await this.updatePatient();
      }
    },
    async createPatient() {
      this.loading = true;
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.post("/patients", this.modalPatient);
        this.successMessage = "Пациент добавлен";
        this.closeModal();
        await this.fetchPatients();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении пациента"
        );
      } finally {
        this.loading = false;
      }
    },
    async updatePatient() {
      if (!this.selectedPatientId) return;
      this.loading = true;
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.put(
          `/patients/${this.selectedPatientId}`,
          this.modalPatient
        );
        this.successMessage = "Пациент обновлён";
        this.closeModal();
        await this.fetchPatients();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении пациента"
        );
      } finally {
        this.loading = false;
      }
    },
    async deletePatient() {
      if (!this.selectedPatientId) return;
      if (!window.confirm("Удалить выбранного пациента?")) return;
      this.loading = true;
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.delete(`/patients/${this.selectedPatientId}`);
        this.successMessage = "Пациент удалён";
        this.selectedPatientId = null;
        await this.fetchPatients();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении пациента"
        );
      } finally {
        this.loading = false;
      }
    },
    resolveErrorMessage(error: unknown, fallbackMessage: string) {
      if (axios.isAxiosError(error)) {
        const responseData = error.response?.data as
          | { message?: string }
          | undefined;
        if (responseData?.message) {
          return responseData.message;
        }
        if (error.response?.status === 400) {
          return "Неверные данные. Проверьте заполнение полей.";
        }
      }
      return fallbackMessage;
    },
    toggleComorbidVisibility(patientId: number) {
      this.showComorbidPatientId =
        this.showComorbidPatientId === patientId ? null : patientId;
    },
    getComorbidToggleLabel(patient: Patient) {
      return this.showComorbidPatientId === patient.patientId
        ? "Скрыть сопутствующие диагнозы"
        : "Показать сопутствующие диагнозы";
    },
    getComorbidDiagnosisLabels(patient: Patient) {
      const ids = patient.comorbidDiagnosisIds ?? [];
      if (ids.length === 0) return [];
      return ids
        .map((id) => this.getDiagnosisDisplay(id))
        .filter((label) => label && label !== "—");
    },
    getSingleComorbidDiagnosisId() {
      if (this.modalPatient.comorbidDiagnosisIds.length !== 1) {
        return null;
      }
      return this.modalPatient.comorbidDiagnosisIds[0] ?? null;
    },
    resetForm() {
      this.modalPatient = {
        patientBarcode: "",
        gender: "UNKNOWN",
        birthDate: "",
        createdAtPatient: this.getNowDateTimeLocal(),
        nationalityId: null,
        mainDiagnosisId: null,
        comorbidDiagnosisIds: [],
        informedConsent: true,
      };
    },
    sortByNameWithUnknown<T>(items: T[], getName: (item: T) => string): T[] {
      return [...items].sort((a, b) => {
        const nameA = getName(a).trim();
        const nameB = getName(b).trim();
        const isUnknownA = nameA.toLowerCase() === "не указано";
        const isUnknownB = nameB.toLowerCase() === "не указано";
        if (isUnknownA && !isUnknownB) return -1;
        if (!isUnknownA && isUnknownB) return 1;
        return nameA.localeCompare(nameB, "ru-RU");
      });
    },
    getNowDateTimeLocal(): string {
      const now = new Date();
      const pad = (value: number) => String(value).padStart(2, "0");
      const date = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(
        now.getDate()
      )}`;
      return `${date}T08:00`;
    },
    toDateTimeLocal(dateTimeString: string | null | undefined): string {
      if (!dateTimeString) {
        return this.getNowDateTimeLocal();
      }
      const normalized = dateTimeString.replace(" ", "T");
      return normalized.slice(0, 16);
    },
    getGenderText(gender: string): string {
      switch (gender) {
        case "MALE":
          return "Мужской";
        case "FEMALE":
          return "Женский";
        default:
          return "Не указано";
      }
    },
    getDiagnosisDisplay(diagnosisId?: number | null) {
      if (!diagnosisId) return "Не указано";
      const diagnosis = this.diagnoses.find(
        (item) => item.diagnosisId === diagnosisId
      );
      if (!diagnosis) return "Не указано";
      const code = diagnosis.icd10Code?.trim() || "";
      const name = diagnosis.diagnosisName?.trim() || "";
      if (code && name) return `${code} — ${name}`;
      return name || code || "Не указано";
    },
    formatDate(dateString: string): string {
      return new Date(dateString).toLocaleDateString("ru-RU");
    },
    formatDateTime(dateTimeString: string): string {
      return new Date(dateTimeString).toLocaleString("ru-RU");
    },
  },
});
</script>

<style scoped>
.patient-list-container {
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
  margin-bottom: 0;
}

.card {
  background-color: var(--surface);
  border-radius: 12px;
  padding: 16px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.actions-card {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
  text-align: center;
}

.alert-success {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}

.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  background-color: var(--surface);
  border-radius: 8px;
  color: var(--text-secondary);
  border: 1px solid var(--border);
}

.patients-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.patient-card {
  min-width: 0;
  box-sizing: border-box;
  background-color: var(--surface);
  padding: 20px;
  border-radius: 8px;
  box-shadow: var(--shadow);
  border-left: 4px solid var(--accent);
  border: 1px solid var(--border);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.patient-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 18px rgba(34, 60, 70, 0.12);
}

.patient-card.selected {
  border-color: #733d31;
  box-shadow: 0 0 0 3px rgba(154, 75, 59, 0.2);
}

.comorbid-toggle-block {
  margin: 8px 0;
}

.btn-link {
  background: none;
  border: none;
  padding: 0;
  font-size: inherit;
  color: var(--accent);
  cursor: pointer;
  text-decoration: underline;
}

.btn-link:hover {
  color: var(--accent-dark);
}

.comorbid-list {
  margin-top: 8px;
  padding-left: 0;
}

.comorbid-item {
  margin-bottom: 4px;
  white-space: normal;
}

.comorbid-empty {
  color: var(--text-secondary);
}

.card-action-buttons {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  gap: 6px;
}

.header-button {
  width: 36px;
  height: 36px;
}

.patient-card h3 {
  margin-bottom: 10px;
  color: var(--text-primary);
}

.patient-card p {
  margin: 5px 0;
  color: var(--text-secondary);
}

.patient-card strong {
  color: var(--text-primary);
}

.barcode-block {
  display: grid;
  gap: 6px;
  margin-bottom: 8px;
}

.barcode-text {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 24, 30, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 2000;
}

.modal {
  background: var(--surface);
  border-radius: 12px;
  padding: 20px;
  width: min(860px, 100%);
  max-height: 85vh;
  overflow: auto;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.form-modal {
  min-height: 50vh;
  max-height: 85vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.form-grid {
  display: grid;
  gap: 16px;
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
  border-radius: 8px;
  font-size: 0.95rem;
  background: #e9dfd2;
}

.form-control:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: var(--focus-ring);
}

.nowrap {
  white-space: nowrap;
}

.with-action .input-action {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  align-items: start;
}

.multi-select {
  border: 1px solid var(--border);
  border-radius: 6px;
  background-color: #e9dfd2;
  padding: 8px;
  height: 160px;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  display: grid;
  grid-auto-rows: min-content;
  gap: 6px;
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

.gender-options {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.radio-label,
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
}
.alert-success,
.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}
</style>
