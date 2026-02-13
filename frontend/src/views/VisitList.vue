<template>
  <div class="visit-page">
    <div class="page-header">
      <h2>Визиты</h2>
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
      <div v-else-if="visits.length === 0" class="empty-state">Нет визитов</div>
      <div v-else class="table-wrapper" @click="activeHeaderHelp = null">
        <table class="visit-table">
          <thead>
            <tr>
              <th class="id-col">
                <div class="header-help" @click.stop>
                  <span>ID</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца ID"
                    @click.stop="toggleHeaderHelp('id')"
                  >
                    ?
                  </button>
                  <div v-if="activeHeaderHelp === 'id'" class="help-popover">
                    Уникальный идентификатор визита
                  </div>
                </div>
              </th>
              <th class="patient-col">
                <div class="header-help" @click.stop>
                  <span>Пациент</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Пациент"
                    @click.stop="toggleHeaderHelp('patient')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'patient'"
                    class="help-popover"
                  >
                    Штрихкод пациента, посетившего организацию для сдачи
                    биоматериала
                  </div>
                </div>
              </th>
              <th class="research-col">
                <div class="header-help" @click.stop>
                  <span>Исследование</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Исследование"
                    @click.stop="toggleHeaderHelp('research')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'research'"
                    class="help-popover"
                  >
                    Номер и название темы исследования
                  </div>
                </div>
              </th>
              <th class="number-col">
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
                    Номер визита пациента
                  </div>
                </div>
              </th>
              <th class="date-col">
                <div class="header-help" @click.stop>
                  <span>Дата</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Дата"
                    @click.stop="toggleHeaderHelp('date')"
                  >
                    ?
                  </button>
                  <div v-if="activeHeaderHelp === 'date'" class="help-popover">
                    Дата посещения (забора биоматериала)
                  </div>
                </div>
              </th>
              <th class="age-col">
                <div class="header-help" @click.stop>
                  <span>Возраст</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Возраст"
                    @click.stop="toggleHeaderHelp('age')"
                  >
                    ?
                  </button>
                  <div v-if="activeHeaderHelp === 'age'" class="help-popover">
                    Возраст пациента на момент забора биоматериала
                  </div>
                </div>
              </th>
              <th class="diagnosis-col">
                <div class="header-help" @click.stop>
                  <span>Основной диагноз</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Основной диагноз"
                    @click.stop="toggleHeaderHelp('mainDiagnosis')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'mainDiagnosis'"
                    class="help-popover"
                  >
                    Основной диагноз, поставленный пациенту
                  </div>
                </div>
              </th>
              <th class="comorbid-col">
                <div class="header-help" @click.stop>
                  <span>Сопутствующие диагнозы</span>
                  <button
                    type="button"
                    class="help-button"
                    aria-label="Описание столбца Сопутствующие диагнозы"
                    @click.stop="toggleHeaderHelp('comorbid')"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === 'comorbid'"
                    class="help-popover"
                  >
                    Сопутствующие основному диагнозу заболевания
                  </div>
                </div>
              </th>
              <th class="action-col"></th>
              <th class="action-col"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="visit in visits"
              :key="visit.visitId"
              :class="{ selected: visit.visitId === selectedVisitId }"
              @click="selectVisit(visit.visitId)"
            >
              <td class="id-col">{{ visit.visitId }}</td>
              <td class="patient-col">
                <div class="barcode-cell">
                  <BarcodeSvg
                    :value="getPatientBarcodeValue(visit.patientId)"
                  />
                  <span>{{ getPatientDisplay(visit.patientId) }}</span>
                </div>
              </td>
              <td class="wrap-cell research-col">
                {{ getResearchName(visit.researchId) }}
              </td>
              <td class="number-col">{{ visit.visitNumber }}</td>
              <td class="date-col">
                <div class="date-cell">
                  <div>{{ formatDateOnly(visit.collectionDate) }}</div>
                  <div
                    v-if="formatTimeOnly(visit.collectionDate)"
                    class="time-line"
                  >
                    {{ formatTimeOnly(visit.collectionDate) }}
                  </div>
                </div>
              </td>
              <td class="age-col">{{ visit.ageAtCollection }}</td>
              <td class="wrap-cell diagnosis-col">
                {{ getDiagnosisDisplay(visit.diagnosisId) }}
              </td>
              <td class="wrap-cell comorbid-col comorbid-cell">
                {{ getComorbidDiagnosesDisplay(visit) }}
              </td>
              <td class="action-col">
                <button
                  class="icon-button"
                  type="button"
                  aria-label="Обновить"
                  title="Обновить"
                  @click.stop="editVisitRow(visit.visitId)"
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
                  @click.stop="deleteVisitRow(visit.visitId)"
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
            <label for="modalPatient">Пациент *</label>
            <select
              id="modalPatient"
              v-model="modalVisit.patientId"
              required
              class="form-control"
            >
              <option value="">Не указано</option>
              <option
                v-for="patient in patients"
                :key="patient.patientId"
                :value="patient.patientId"
              >
                ID {{ patient.patientId }} — {{ patient.patientBarcode }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="modalResearch">Исследование *</label>
            <select
              id="modalResearch"
              v-model="modalVisit.researchId"
              required
              class="form-control"
            >
              <option value="">Не указано</option>
              <option
                v-for="research in researches"
                :key="research.researchId"
                :value="research.researchId"
              >
                {{ research.researchNumber }} — {{ research.researchName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="modalVisitNumber">Номер визита *</label>
            <input
              id="modalVisitNumber"
              v-model.number="modalVisit.visitNumber"
              type="number"
              min="1"
              required
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalCollection">Дата забора *</label>
            <input
              id="modalCollection"
              v-model="modalVisit.collectionDate"
              type="datetime-local"
              required
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalAge">Возраст *</label>
            <input
              id="modalAge"
              v-model.number="modalVisit.ageAtCollection"
              type="number"
              min="0"
              required
              class="form-control"
              readonly
            />
          </div>
          <div class="form-group with-action">
            <label for="modalDiagnosis">Основной диагноз</label>
            <div class="input-action">
              <select
                id="modalDiagnosis"
                v-model="modalVisit.diagnosisId"
                class="form-control"
              >
                <option value="">Не указано</option>
                <option
                  v-for="diagnosis in diagnoses"
                  :key="diagnosis.diagnosisId"
                  :value="diagnosis.diagnosisId"
                >
                  {{ diagnosis.diagnosisName }}
                </option>
              </select>
              <div class="icon-actions">
                <button
                  type="button"
                  class="icon-button"
                  @click="openDiagnosisModal()"
                  aria-label="Добавить"
                >
                  <svg viewBox="0 0 24 24" aria-hidden="true">
                    <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
                  </svg>
                </button>
                <button
                  type="button"
                  class="icon-button"
                  :disabled="!modalVisit.diagnosisId"
                  @click="openDiagnosisModal(modalVisit.diagnosisId)"
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
                  :disabled="!modalVisit.diagnosisId"
                  @click="deleteDiagnosisQuick(modalVisit.diagnosisId)"
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
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ modalMode === "edit" ? "Обновить" : "Добавить" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div
      v-if="refModalOpen"
      class="modal-overlay"
      @click.self="closeDiagnosisModal"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>{{ refModalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeDiagnosisModal">
            Закрыть
          </button>
        </div>
        <form class="form-grid" @submit.prevent="submitDiagnosisModal">
          <div class="form-group">
            <label for="refIcd">Код МКБ‑10</label>
            <input
              id="refIcd"
              v-model="refDiagnosisCode"
              type="text"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="refDiagName">Диагноз *</label>
            <input
              id="refDiagName"
              v-model="refDiagnosisName"
              type="text"
              required
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
import BarcodeSvg from "@/components/BarcodeSvg.vue";

interface Visit {
  visitId: number;
  patientId: number;
  researchId: number;
  visitNumber: number;
  collectionDate: string;
  ageAtCollection: number;
  diagnosisId: number | null;
  comorbidDiagnosisIds: number[];
}

interface VisitForm {
  patientId: number | null;
  researchId: number | null;
  visitNumber: number | null;
  collectionDate: string;
  ageAtCollection: number | null;
  diagnosisId: number | null;
}

interface PatientRef {
  patientId: number;
  patientBarcode: string;
  birthDate: string;
  mainDiagnosisId: number | null;
}

interface ResearchRef {
  researchId: number;
  researchNumber: string;
  researchName: string;
}

interface DiagnosisRef {
  diagnosisId: number;
  icd10Code: string | null;
  diagnosisName: string;
}

export default defineComponent({
  name: "VisitList",
  components: {
    BarcodeSvg,
  },
  data() {
    return {
      visits: [] as Visit[],
      patients: [] as PatientRef[],
      researches: [] as ResearchRef[],
      diagnoses: [] as DiagnosisRef[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      selectedVisitId: null as number | null,
      modalOpen: false,
      modalMode: "create" as "create" | "edit",
      modalTitle: "",
      modalVisit: {
        patientId: null,
        researchId: null,
        visitNumber: null,
        collectionDate: "",
        ageAtCollection: null,
        diagnosisId: null,
      } as VisitForm,
      refModalOpen: false,
      refModalTitle: "",
      refModalMode: "create" as "create" | "edit",
      refDiagnosisId: null as number | null,
      refDiagnosisCode: "",
      refDiagnosisName: "",
      activeHeaderHelp: null as
        | "id"
        | "patient"
        | "research"
        | "number"
        | "date"
        | "age"
        | "mainDiagnosis"
        | "comorbid"
        | null,
    };
  },
  created() {
    this.fetchVisits();
    this.fetchReferences();
  },
  watch: {
    "modalVisit.patientId": "handlePatientSelection",
    "modalVisit.collectionDate": "updateAgeFromSelection",
  },
  methods: {
    toggleHeaderHelp(
      key:
        | "id"
        | "patient"
        | "research"
        | "number"
        | "date"
        | "age"
        | "mainDiagnosis"
        | "comorbid"
    ) {
      this.activeHeaderHelp = this.activeHeaderHelp === key ? null : key;
    },
    async fetchVisits() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const response = await axios.get("/visits");
        this.visits = [...response.data].sort(
          (a: Visit, b: Visit) => a.visitId - b.visitId
        );
      } catch (error) {
        this.errorMessage = "Не удалось загрузить визиты";
      } finally {
        this.loading = false;
      }
    },
    async fetchReferences() {
      try {
        const [patientsResponse, researchesResponse, diagnosesResponse] =
          await Promise.all([
            axios.get("/patients"),
            axios.get("/researches"),
            axios.get("/references/diagnoses"),
          ]);
        this.patients = [...patientsResponse.data].sort(
          (a: PatientRef, b: PatientRef) => a.patientId - b.patientId
        );
        this.researches = [...researchesResponse.data].sort(
          (a: ResearchRef, b: ResearchRef) =>
            a.researchName.localeCompare(b.researchName, "ru-RU")
        );
        this.diagnoses = [...diagnosesResponse.data].sort(
          (a: DiagnosisRef, b: DiagnosisRef) =>
            a.diagnosisName.localeCompare(b.diagnosisName, "ru-RU")
        );
      } catch (error) {
        console.error("Ошибка при загрузке справочников:", error);
      }
    },
    getResearchName(researchId: number) {
      const research = this.researches.find(
        (item) => item.researchId === researchId
      );
      if (!research) {
        return "Не указано";
      }
      return `${research.researchNumber} — ${research.researchName}`;
    },
    getPatientBarcode(patientId: number) {
      const patient = this.patients.find(
        (item) => item.patientId === patientId
      );
      return patient?.patientBarcode || "Не указано";
    },
    getPatientDisplay(patientId: number) {
      const patient = this.patients.find(
        (item) => item.patientId === patientId
      );
      if (!patient) {
        return "Не указано";
      }
      const barcode = patient.patientBarcode || "Не указано";
      return `ID ${patient.patientId} - ${barcode}`;
    },
    getPatientBarcodeValue(patientId: number) {
      const patient = this.patients.find(
        (item) => item.patientId === patientId
      );
      return patient?.patientBarcode || "";
    },
    getDiagnosisDisplay(diagnosisId: number | null) {
      if (diagnosisId == null) return "—";
      const diagnosis = this.diagnoses.find(
        (item) => item.diagnosisId === diagnosisId
      );
      if (!diagnosis) return "Не указано";
      const code = diagnosis.icd10Code?.trim() || "";
      const name = diagnosis.diagnosisName?.trim() || "";
      if (code && name) return `${code} — ${name}`;
      return name || code || "—";
    },
    getComorbidDiagnosesDisplay(visit: Visit) {
      if (
        !visit.comorbidDiagnosisIds ||
        visit.comorbidDiagnosisIds.length === 0
      ) {
        return "—";
      }
      const labels = visit.comorbidDiagnosisIds
        .map((id) => this.getDiagnosisDisplay(id))
        .filter((label) => label && label !== "—");
      return labels.length > 0 ? labels.join(";\n") : "—";
    },
    selectVisit(visitId: number) {
      this.selectedVisitId = this.selectedVisitId === visitId ? null : visitId;
    },
    editVisitRow(visitId: number) {
      this.selectedVisitId = visitId;
      this.openEditModal();
    },
    deleteVisitRow(visitId: number) {
      this.selectedVisitId = visitId;
      this.deleteVisit();
    },
    openCreateModal() {
      this.modalMode = "create";
      this.modalTitle = "Добавить визит";
      this.modalVisit = {
        patientId: null,
        researchId: null,
        visitNumber: null,
        collectionDate: this.getNowLocalDatetime(),
        ageAtCollection: null,
        diagnosisId: null,
      };
      this.modalOpen = true;
    },
    openEditModal() {
      const visit = this.visits.find(
        (item) => item.visitId === this.selectedVisitId
      );
      if (!visit) {
        return;
      }
      this.modalMode = "edit";
      this.modalTitle = "Обновить визит";
      this.modalVisit = {
        patientId: visit.patientId,
        researchId: visit.researchId,
        visitNumber: visit.visitNumber,
        collectionDate: this.toDatetimeLocal(visit.collectionDate),
        ageAtCollection: visit.ageAtCollection,
        diagnosisId: visit.diagnosisId,
      };
      this.modalOpen = true;
    },
    openDiagnosisModal(presetId: number | null = null) {
      this.refDiagnosisId = presetId;
      this.refDiagnosisCode = "";
      this.refDiagnosisName = "";
      this.refModalMode = presetId ? "edit" : "create";
      this.refModalTitle =
        this.refModalMode === "edit" ? "Обновить диагноз" : "Добавить диагноз";
      if (this.refDiagnosisId) {
        this.fillDiagnosisModal();
      }
      this.refModalOpen = true;
    },
    closeDiagnosisModal() {
      this.refModalOpen = false;
    },
    fillDiagnosisModal() {
      const diagnosis = this.diagnoses.find(
        (item) => item.diagnosisId === this.refDiagnosisId
      );
      this.refDiagnosisCode = diagnosis?.icd10Code || "";
      this.refDiagnosisName = diagnosis?.diagnosisName || "";
    },
    async submitDiagnosisModal() {
      if (!this.refDiagnosisName.trim()) {
        return;
      }
      if (this.refModalMode === "edit") {
        await this.updateDiagnosisModal();
        return;
      }
      try {
        await axios.post("/references/diagnoses", {
          icd10Code: this.refDiagnosisCode,
          diagnosisName: this.refDiagnosisName,
        });
        await this.fetchReferences();
        this.closeDiagnosisModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении диагноза";
      }
    },
    async updateDiagnosisModal() {
      if (!this.refDiagnosisId || !this.refDiagnosisName.trim()) {
        return;
      }
      try {
        await axios.put(`/references/diagnoses/${this.refDiagnosisId}`, {
          icd10Code: this.refDiagnosisCode,
          diagnosisName: this.refDiagnosisName,
        });
        await this.fetchReferences();
        this.closeDiagnosisModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении диагноза";
      }
    },
    async deleteDiagnosisModal() {
      if (!this.refDiagnosisId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранный диагноз?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/references/diagnoses/${this.refDiagnosisId}`);
        await this.fetchReferences();
        this.closeDiagnosisModal();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении диагноза";
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
        if (this.modalVisit.diagnosisId === id) {
          this.modalVisit.diagnosisId = null;
        }
        await this.fetchReferences();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении диагноза";
      }
    },
    handlePatientSelection() {
      this.updateAgeFromSelection();
      if (this.modalMode !== "create") {
        return;
      }
      const patient = this.patients.find(
        (item) => item.patientId === this.modalVisit.patientId
      );
      if (patient) {
        this.modalVisit.diagnosisId = patient.mainDiagnosisId ?? null;
        this.modalVisit.visitNumber = this.getNextVisitNumber(
          patient.patientId
        );
      } else {
        this.modalVisit.diagnosisId = null;
        this.modalVisit.visitNumber = null;
      }
    },
    getNextVisitNumber(patientId: number) {
      const patientVisits = this.visits.filter(
        (visit) => visit.patientId === patientId
      );
      if (patientVisits.length === 0) {
        return 1;
      }
      const maxNumber = Math.max(
        ...patientVisits.map((visit) => visit.visitNumber || 0)
      );
      return maxNumber + 1;
    },
    updateAgeFromSelection() {
      const patient = this.patients.find(
        (item) => item.patientId === this.modalVisit.patientId
      );
      if (!patient?.birthDate || !this.modalVisit.collectionDate) {
        return;
      }
      const birth = new Date(patient.birthDate);
      const collection = new Date(this.modalVisit.collectionDate);
      let age = collection.getFullYear() - birth.getFullYear();
      const m = collection.getMonth() - birth.getMonth();
      if (m < 0 || (m === 0 && collection.getDate() < birth.getDate())) {
        age -= 1;
      }
      this.modalVisit.ageAtCollection = age;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitModal() {
      if (this.modalMode === "create") {
        await this.createVisit();
      } else {
        await this.updateVisit();
      }
    },
    async createVisit() {
      try {
        await axios.post("/visits", this.serializeVisitForm());
        this.successMessage = "Визит добавлен";
        this.closeModal();
        await this.fetchVisits();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении визита"
        );
      }
    },
    async updateVisit() {
      if (!this.selectedVisitId) {
        return;
      }
      try {
        await axios.put(
          `/visits/${this.selectedVisitId}`,
          this.serializeVisitForm()
        );
        this.successMessage = "Визит обновлен";
        this.closeModal();
        await this.fetchVisits();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении визита"
        );
      }
    },
    async deleteVisit() {
      if (!this.selectedVisitId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранный визит?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/visits/${this.selectedVisitId}`);
        this.successMessage = "Визит удален";
        this.selectedVisitId = null;
        await this.fetchVisits();
      } catch (error) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении визита"
        );
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
    serializeVisitForm() {
      return {
        patientId: this.modalVisit.patientId,
        researchId: this.modalVisit.researchId,
        visitNumber: this.modalVisit.visitNumber,
        collectionDate: this.modalVisit.collectionDate,
        ageAtCollection: this.modalVisit.ageAtCollection,
        diagnosisId: this.modalVisit.diagnosisId,
      };
    },
    toDatetimeLocal(value: string) {
      if (!value) return "";
      return value.replace(" ", "T").slice(0, 16);
    },
    getNowLocalDatetime() {
      const now = new Date();
      const pad = (value: number) => String(value).padStart(2, "0");
      return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(
        now.getDate()
      )}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
    },
    formatDate(value: string) {
      if (!value) return "—";
      return new Date(value).toLocaleString("ru-RU");
    },
    formatDateOnly(value: string) {
      if (!value) return "—";
      return new Date(value).toLocaleDateString("ru-RU");
    },
    formatTimeOnly(value: string) {
      if (!value) return "";
      return new Date(value).toLocaleTimeString("ru-RU", {
        hour: "2-digit",
        minute: "2-digit",
      });
    },
  },
});
</script>

<style scoped>
.visit-page {
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

.visit-table {
  width: 100%;
  table-layout: fixed;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.visit-table th,
.visit-table td {
  border-bottom: 1px solid var(--border);
  padding: 10px;
  text-align: left;
  white-space: normal;
  word-break: break-word;
}

.visit-table td {
  white-space: pre-line;
}

.visit-table .id-col {
  width: 5;
  max-width: 5%;
}
.visit-table .patient-col {
  width: 12%;
  max-width: 12%;
}
.visit-table .research-col {
  width: 20%;
  max-width: 20%;
}
.visit-table .number-col {
  width: 5%;
  max-width: 5%;
}
.visit-table .date-col {
  width: 8%;
  max-width: 8%;
}
.visit-table .age-col {
  width: 8%;
  max-width: 8%;
}
.visit-table .diagnosis-col {
  width: 16%;
  max-width: 16%;
}
.visit-table .comorbid-col {
  width: 16%;
  max-width: 16%;
}

.visit-table th.action-col,
.visit-table td.action-col {
  width: 4%;
  min-width: 44px;
  max-width: 4%;
  text-align: center;
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

.visit-table td.wrap-cell {
  white-space: normal;
}

.visit-table td.comorbid-cell {
  white-space: pre-line;
}

.visit-table .date-cell {
  white-space: normal;
  line-height: 1.2;
}

.visit-table .time-line {
  font-size: 0.85em;
  color: var(--text-secondary);
}

.visit-table th {
  color: var(--text-primary);
  background-color: #f0e9df;
}

.visit-table tr.selected {
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

.multi-select {
  border: 1px solid var(--border);
  border-radius: 6px;
  background-color: #e9dfd2;
  padding: 8px;
  max-height: 160px;
  overflow: auto;
  display: grid;
  gap: 6px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
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
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}

.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}

.barcode-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.barcode-cell span {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.barcode-cell :deep(.barcode-svg) {
  max-width: 200px;
}
</style>
