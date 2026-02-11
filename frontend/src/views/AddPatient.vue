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
        informedConsent: false,
      } as Patient,
      nationalities: [] as Nationality[],
      diagnoses: [] as DiagnosisRef[],
      loading: false,
      successMessage: "",
      errorMessage: "",
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
    resetForm() {
      this.patient = {
        patientBarcode: "",
        gender: "UNKNOWN",
        birthDate: "",
        nationalityId: null,
        mainDiagnosisId: null,
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
