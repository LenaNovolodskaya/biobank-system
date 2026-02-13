<template>
  <div class="add-patient-container">
    <h2>Добавление нового пациента</h2>

    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>

    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="submitForm" class="patient-form">
      <div class="form-group">
        <label for="patientBarcode">Штрихкод пациента *</label>
        <input
          type="text"
          id="patientBarcode"
          v-model="patient.patientBarcode"
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
              type="radio"
              v-model="patient.gender"
              value="MALE"
              required
            />
            Мужской
          </label>
          <label class="radio-label">
            <input
              type="radio"
              v-model="patient.gender"
              value="FEMALE"
              required
            />
            Женский
          </label>
          <label class="radio-label">
            <input
              type="radio"
              v-model="patient.gender"
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
          type="date"
          id="birthDate"
          v-model="patient.birthDate"
          required
          class="form-control"
          :max="maxDate"
        />
      </div>

      <div class="form-group">
        <label for="nationality">Национальность</label>
        <select
          id="nationality"
          v-model="patient.nationalityId"
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
      </div>

      <div class="form-group">
        <label for="mainDiagnosis">Основной диагноз</label>
        <select
          id="mainDiagnosis"
          v-model="patient.mainDiagnosisId"
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
      </div>

      <div class="form-group with-action">
        <label for="comorbidDiagnoses">Сопутствующие диагнозы</label>
        <div class="input-action">
          <div id="comorbidDiagnoses" class="multi-select">
            <label
              v-for="diagnosis in diagnoses"
              :key="diagnosis.diagnosisId"
              class="checkbox-label"
            >
              <input
                type="checkbox"
                :value="diagnosis.diagnosisId"
                v-model="patient.comorbidDiagnosisIds"
              />
              {{ diagnosis.diagnosisName }}
            </label>
          </div>
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
              :disabled="!getSingleComorbidDiagnosisId()"
              @click="openDiagnosisModal(getSingleComorbidDiagnosisId())"
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
          <input type="checkbox" v-model="patient.informedConsent" />
          Информированное согласие на использование биоматериала
        </label>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="loading">
          <span v-if="loading">Добавление...</span>
          <span v-else>Добавить пациента</span>
        </button>
        <button type="button" class="btn btn-secondary" @click="resetForm">
          Очистить форму
        </button>
      </div>
    </form>

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
import { defineComponent } from "vue";
import axios from "axios";

interface Patient {
  patientBarcode: string;
  gender: string;
  birthDate: string;
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
  name: "AddPatient",
  data() {
    return {
      patient: {
        patientBarcode: "",
        gender: "UNKNOWN",
        birthDate: "",
        nationalityId: null as number | null,
        mainDiagnosisId: null as number | null,
        comorbidDiagnosisIds: [],
        informedConsent: false,
      } as Patient,
      nationalities: [] as Nationality[],
      diagnoses: [] as DiagnosisRef[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      diagModalOpen: false,
      diagModalTitle: "",
      diagModalMode: "create" as "create" | "edit",
      diagDiagnosisId: null as number | null,
      diagDiagnosisCode: "",
      diagDiagnosisName: "",
    };
  },
  computed: {
    maxDate(): string {
      const today = new Date();
      return today.toISOString().split("T")[0];
    },
  },
  created() {
    this.fetchNationalities();
    this.fetchDiagnoses();
  },
  methods: {
    async fetchNationalities() {
      try {
        const response = await axios.get("/references/nationalities");
        this.nationalities = response.data;
      } catch (error) {
        console.error("Ошибка при загрузке национальностей:", error);
        this.errorMessage = "Не удалось загрузить список национальностей";
      }
    },
    async fetchDiagnoses() {
      try {
        const response = await axios.get("/references/diagnoses");
        this.diagnoses = response.data;
      } catch (error) {
        console.error("Ошибка при загрузке диагнозов:", error);
        this.errorMessage = "Не удалось загрузить список диагнозов";
      }
    },
    async submitForm() {
      this.loading = true;
      this.successMessage = "";
      this.errorMessage = "";

      try {
        const response = await axios.post("/patients", this.patient);

        this.successMessage = `Пациент успешно добавлен! ID: ${response.data.patientId}`;
        this.resetForm();
      } catch (error: unknown) {
        console.error("Ошибка при добавлении пациента:", error);

        if (axios.isAxiosError(error)) {
          const responseData = error.response?.data as
            | { message?: string }
            | undefined;
          const message = responseData?.message;
          const status = error.response?.status;

          if (message) {
            this.errorMessage = message;
          } else if (status === 400) {
            this.errorMessage = "Неверные данные. Проверьте заполнение полей.";
          } else {
            this.errorMessage =
              "Ошибка при добавлении пациента. Попробуйте еще раз.";
          }
        } else {
          this.errorMessage =
            "Ошибка при добавлении пациента. Попробуйте еще раз.";
        }
      } finally {
        this.loading = false;
      }
    },
    getSingleComorbidDiagnosisId() {
      if (this.patient.comorbidDiagnosisIds.length !== 1) {
        return null;
      }
      return this.patient.comorbidDiagnosisIds[0] ?? null;
    },
    openDiagnosisModal(presetId: number | null = null) {
      this.diagDiagnosisId = presetId;
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
        await axios.post("/references/diagnoses", {
          icd10Code: this.diagDiagnosisCode,
          diagnosisName: this.diagDiagnosisName,
        });
        await this.fetchDiagnoses();
        this.closeDiagnosisModal();
      } catch (error) {
        console.error("Ошибка при добавлении диагноза:", error);
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
        console.error("Ошибка при обновлении диагноза:", error);
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
        if (this.patient.mainDiagnosisId === id) {
          this.patient.mainDiagnosisId = null;
        }
        this.patient.comorbidDiagnosisIds =
          this.patient.comorbidDiagnosisIds.filter((x) => x !== id);
        await this.fetchDiagnoses();
      } catch (error) {
        console.error("Ошибка при удалении диагноза:", error);
        this.errorMessage = "Ошибка при удалении диагноза";
      }
    },
    resetForm() {
      this.patient = {
        patientBarcode: "",
        gender: "UNKNOWN",
        birthDate: "",
        nationalityId: null,
        mainDiagnosisId: null,
        comorbidDiagnosisIds: [],
        informedConsent: false,
      };
    },
  },
});
</script>

<style scoped>
.add-patient-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: var(--text-primary);
}

.alert {
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
}

.alert-success {
  background-color: #d4e2c8;
  color: #3f5a34;
  border: 1px solid #c1d3b3;
}

.alert-danger {
  background-color: #e2c9c0;
  color: #6f3324;
  border: 1px solid #d4b6ab;
}

.patient-form {
  background-color: var(--surface);
  padding: 30px;
  border-radius: 8px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  min-height: 640px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-control {
  width: 100%;
  padding: 10px;
  border: 1px solid var(--border);
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
  background-color: #e9dfd2;
}

.form-control:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: var(--focus-ring);
}

.gender-options {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-secondary);
}

.radio-label input {
  cursor: pointer;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-secondary);
}

.checkbox-label input {
  cursor: pointer;
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
  height: 120px;
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
  border-radius: 8px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  max-width: 480px;
  width: 100%;
  max-height: 90vh;
  overflow: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: var(--text-primary);
}

.form-grid {
  display: grid;
  gap: 16px;
  padding: 20px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.25s ease, color 0.25s ease,
    box-shadow 0.25s ease, transform 0.25s ease;
}

.btn-primary {
  background-color: var(--accent);
  color: #f2ede6;
  box-shadow: var(--shadow);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--accent-dark);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background-color: #a8b6bd;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #c7d5dc;
  color: var(--text-primary);
  border: 1px solid var(--border);
}

.btn-secondary:hover {
  background-color: #bcc9d1;
}
</style>
