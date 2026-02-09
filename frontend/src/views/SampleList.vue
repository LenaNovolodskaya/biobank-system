<template>
  <div class="sample-page">
    <div class="page-header">
      <h2>Образцы</h2>
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

    <div class="card filters-card">
      <div class="filters">
        <div class="form-group">
          <label for="search">Поиск</label>
          <input
            id="search"
            v-model="searchQuery"
            type="text"
            class="form-control"
            placeholder="Штрихкод, ID, позиция"
          />
        </div>
        <div class="form-group">
          <label for="expiry">Срок хранения</label>
          <select id="expiry" v-model="expiryStatus" class="form-control">
            <option value="">Все</option>
            <option value="GREEN">Годен</option>
            <option value="YELLOW">Истекает</option>
            <option value="RED">Просрочен</option>
          </select>
        </div>
        <div class="form-group">
          <label for="container">Контейнер</label>
          <select id="container" v-model="containerFilter" class="form-control">
            <option value="">Все</option>
            <option
              v-for="container in containers"
              :key="container.containerId"
              :value="getContainerLabel(container.containerId)"
            >
              {{ getContainerLabel(container.containerId) }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="sampleType">Тип образца</label>
          <select
            id="sampleType"
            v-model="sampleTypeFilter"
            class="form-control"
          >
            <option value="">Все</option>
            <option
              v-for="type in sampleTypes"
              :key="type.sampleTypeId"
              :value="type.sampleTypeName"
            >
              {{ type.sampleTypeName }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="sampleStatus">Статус образца</label>
          <select
            id="sampleStatus"
            v-model="sampleStatusFilter"
            class="form-control"
          >
            <option value="">Все</option>
            <option
              v-for="status in sampleStatuses"
              :key="status.sampleStatusId"
              :value="status.sampleStatusName"
            >
              {{ status.sampleStatusName }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="visitId">Визит</label>
          <select id="visitId" v-model="visitFilter" class="form-control">
            <option value="">Все</option>
            <option
              v-for="visit in visits"
              :key="visit.visitId"
              :value="getVisitLabel(visit.visitId)"
            >
              {{ getVisitLabel(visit.visitId) }}
            </option>
          </select>
        </div>
        <div class="form-actions">
          <button class="btn btn-secondary" @click="resetFilters">Сброс</button>
          <button class="btn btn-secondary wide-button" @click="fetchSamples">
            Обновить список
          </button>
        </div>
      </div>
    </div>

    <div class="card columns-card">
      <div class="section-header">
        <h3>Столбцы</h3>
      </div>
      <div class="columns-grid">
        <label v-for="column in columns" :key="column.key" class="checkbox">
          <input type="checkbox" v-model="column.visible" />
          <span>{{ column.label }}</span>
        </label>
      </div>
    </div>

    <div class="card table-card">
      <p v-if="loading">Загрузка...</p>
      <div v-else-if="filteredSamples.length === 0" class="empty-state">
        Нет образцов по заданным условиям
      </div>
      <div v-else class="table-wrapper">
        <table class="samples-table">
          <thead>
            <tr>
              <th v-for="column in visibleColumns" :key="column.key">
                {{ column.label }}
              </th>
              <th class="action-col"></th>
              <th class="action-col"></th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="sample in filteredSamples"
              :key="sample.sampleId"
              :class="{ selected: sample.sampleId === selectedSampleId }"
              @click="selectSample(sample.sampleId)"
            >
              <td v-for="column in visibleColumns" :key="column.key">
                <template v-if="column.key === 'barcode'">
                  <div class="barcode-cell">
                    <BarcodeSvg :value="sample.barcode" />
                    <span>{{ sample.barcode }}</span>
                  </div>
                </template>
                <template v-else>
                  {{ formatValue(sample, column.key) }}
                </template>
              </td>
              <td class="action-col">
                <button
                  class="icon-button"
                  type="button"
                  aria-label="Обновить"
                  title="Обновить"
                  @click.stop="editSampleRow(sample.sampleId)"
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
                  @click.stop="deleteSampleRow(sample.sampleId)"
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
            <label for="modalVisitId">Визит (ID) *</label>
            <input
              id="modalVisitId"
              v-model.number="modalSample.visitId"
              type="number"
              min="1"
              required
              class="form-control"
              placeholder="Введите ID визита"
            />
          </div>
          <div class="form-group">
            <label for="modalBarcode">Штрихкод *</label>
            <input
              id="modalBarcode"
              v-model="modalSample.barcode"
              type="text"
              required
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalSampleType">Тип образца</label>
            <select
              id="modalSampleType"
              v-model="modalSample.sampleTypeId"
              class="form-control"
            >
              <option value="">Не указано</option>
              <option
                v-for="type in sampleTypes"
                :key="type.sampleTypeId"
                :value="type.sampleTypeId"
              >
                {{ type.sampleTypeName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="modalInitialQty">Начальное количество пробирок *</label>
            <input
              id="modalInitialQty"
              v-model.number="modalSample.initialQuantity"
              type="number"
              min="1"
              required
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalCurrentQty">Текущее количество пробирок *</label>
            <input
              id="modalCurrentQty"
              v-model.number="modalSample.currentQuantity"
              type="number"
              min="0"
              required
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalRecommended">
              Рекомендуемый срок хранения (в месяцах)
            </label>
            <input
              id="modalRecommended"
              v-model.number="modalSample.recommendedStorageMonths"
              type="number"
              min="0"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="modalCollectionDate">Дата забора образца</label>
            <input
              id="modalCollectionDate"
              v-model="modalSample.collectionDate"
              type="datetime-local"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label>Фактический срок хранения (в месяцах)</label>
            <div class="readonly-field">{{ actualMonthsPreview }}</div>
          </div>
          <div class="form-group">
            <label>Срок хранения (светофор)</label>
            <div class="readonly-field">{{ expiryPreview }}</div>
          </div>
          <div class="form-group">
            <label for="modalContainer">Контейнер</label>
            <select
              id="modalContainer"
              v-model="modalSample.containerId"
              class="form-control"
            >
              <option value="">Не указано</option>
              <option
                v-for="container in containers"
                :key="container.containerId"
                :value="container.containerId"
              >
                {{ container.containerType || "Контейнер" }}
                №{{ container.containerNumber ?? "—" }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>Пробирки</label>
            <div v-if="tubeCount > 0" class="tube-grid">
              <div v-for="index in tubeCount" :key="index" class="tube-row">
                <div class="tube-label">Пробирка {{ index }}</div>
                <select
                  v-model.number="tubeStatusIds[index - 1]"
                  class="form-control"
                >
                  <option :value="null">Не указано</option>
                  <option
                    v-for="status in sampleStatuses"
                    :key="status.sampleStatusId"
                    :value="status.sampleStatusId"
                  >
                    {{ status.sampleStatusName }}
                  </option>
                </select>
                <select
                  v-model="tubePositions[index - 1]"
                  class="form-control"
                  :disabled="!modalSample.containerId"
                >
                  <option value="">Не указано</option>
                  <option
                    v-for="position in positionsForContainer"
                    :key="position.value"
                    :value="position.value"
                    :disabled="isPositionDisabled(position, index - 1)"
                  >
                    {{ position.value }}
                  </option>
                </select>
              </div>
            </div>
            <div v-else class="readonly-field">
              Укажите количество пробирок.
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
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import BarcodeSvg from "@/components/BarcodeSvg.vue";

interface Sample {
  sampleId: number;
  visitId: number;
  barcode: string;
  sampleTypeId: number | null;
  initialQuantity: number;
  currentQuantity: number;
  recommendedStorageMonths: number | null;
  actualStorageMonths: number | null;
  expiryStatus: string;
  sampleStatusId: number | null;
  tubeStatusIds?: string | null;
  containerId: number | null;
  positionInContainer: string | null;
  createdAtSample: string;
}

interface ColumnConfig {
  key: string;
  label: string;
  visible: boolean;
}

interface SampleForm {
  visitId: number | null;
  barcode: string;
  sampleTypeId: number | null;
  initialQuantity: number | null;
  currentQuantity: number | null;
  recommendedStorageMonths: number | null;
  actualStorageMonths: number | null;
  expiryStatus: string;
  sampleStatusId: number | null;
  containerId: number | null;
  positionInContainer: string;
  collectionDate: string;
}

interface VisitRef {
  visitId: number;
  patientId: number;
  researchId: number;
  visitNumber: number;
  collectionDate: string;
  ageAtCollection: number;
  diagnosisId: number | null;
}

interface PatientRef {
  patientId: number;
  patientBarcode: string;
  gender: string;
  birthDate: string;
  nationalityName?: string;
  mainDiagnosisId?: number | null;
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

interface SampleTypeRef {
  sampleTypeId: number;
  sampleTypeName: string;
}

interface SampleStatusRef {
  sampleStatusId: number;
  sampleStatusName: string;
}

interface ContainerRef {
  containerId: number;
  shelfNumber: string | null;
  containerType: string | null;
  containerNumber: number | null;
  rowsCount: number | null;
  columnsCount: number | null;
  maxSamplesCount: number;
  currentSamplesCount: number;
}

const resolveContainerLayout = (container: ContainerRef) => {
  const total = container.maxSamplesCount || 0;
  const hasRows =
    typeof container.rowsCount === "number" && container.rowsCount > 0;
  const hasColumns =
    typeof container.columnsCount === "number" && container.columnsCount > 0;
  if (hasRows && hasColumns) {
    return {
      rows: container.rowsCount as number,
      columns: container.columnsCount as number,
      total:
        (container.rowsCount as number) * (container.columnsCount as number),
    };
  }
  if (!total) {
    return { rows: 0, columns: 0, total: 0 };
  }
  const columnsGuess = Math.ceil(Math.sqrt(total));
  const rowsGuess = Math.ceil(total / columnsGuess);
  return {
    rows: hasRows ? (container.rowsCount as number) : rowsGuess,
    columns: hasColumns ? (container.columnsCount as number) : columnsGuess,
    total,
  };
};

const getColumnLabel = (index: number): string => {
  let result = "";
  let current = index;
  while (current > 0) {
    const remainder = (current - 1) % 26;
    result = String.fromCharCode(65 + remainder) + result;
    current = Math.floor((current - 1) / 26);
  }
  return result;
};

const getColumnIndex = (label: string): number | null => {
  if (!label) {
    return null;
  }
  let index = 0;
  const normalized = label.toUpperCase();
  for (let i = 0; i < normalized.length; i += 1) {
    const code = normalized.charCodeAt(i);
    if (code < 65 || code > 90) {
      return null;
    }
    index = index * 26 + (code - 64);
  }
  return index || null;
};

const normalizePositionLabel = (
  container: ContainerRef,
  value: string | null
): string | null => {
  if (!value) {
    return null;
  }
  const { columns, total } = resolveContainerLayout(container);
  if (!columns || !total) {
    return null;
  }
  const trimmed = value.trim().toUpperCase();
  const letterMatch = trimmed.match(/^([A-Z]+)(\d+)$/);
  if (letterMatch) {
    const column = getColumnIndex(letterMatch[1]);
    const row = Number(letterMatch[2]);
    if (!column || row < 1) {
      return null;
    }
    const index = (row - 1) * columns + column;
    if (index < 1 || index > total) {
      return null;
    }
    return `${getColumnLabel(column)}${row}`;
  }
  const numeric = Number(trimmed);
  if (!Number.isFinite(numeric) || numeric < 1) {
    return null;
  }
  const index = Math.floor(numeric);
  if (index > total) {
    return null;
  }
  const row = Math.floor((index - 1) / columns) + 1;
  const column = ((index - 1) % columns) + 1;
  return `${getColumnLabel(column)}${row}`;
};

const splitPositionValues = (value: string | null): string[] => {
  if (!value) {
    return [];
  }
  return value
    .split(/[,\n;]+/)
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
};

const extractPositionLabels = (
  container: ContainerRef,
  value: string | null
): string[] => {
  const parts = splitPositionValues(value);
  const labels = parts
    .map((part) => normalizePositionLabel(container, part))
    .filter((label): label is string => !!label);
  return Array.from(new Set(labels));
};

export default defineComponent({
  name: "SampleList",
  components: {
    BarcodeSvg,
  },
  data() {
    return {
      samples: [] as Sample[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      selectedSampleId: null as number | null,
      modalOpen: false,
      modalMode: "create" as "create" | "edit",
      modalTitle: "",
      modalSample: {
        visitId: null,
        barcode: "",
        sampleTypeId: null,
        initialQuantity: null,
        currentQuantity: null,
        recommendedStorageMonths: null,
        actualStorageMonths: null,
        expiryStatus: "",
        sampleStatusId: null,
        containerId: null,
        positionInContainer: "",
        collectionDate: "",
      } as SampleForm,
      visits: [] as VisitRef[],
      patients: [] as PatientRef[],
      researches: [] as ResearchRef[],
      diagnoses: [] as DiagnosisRef[],
      sampleTypes: [] as SampleTypeRef[],
      sampleStatuses: [] as SampleStatusRef[],
      containers: [] as ContainerRef[],
      searchQuery: "",
      expiryStatus: "",
      containerFilter: "",
      sampleTypeFilter: "",
      sampleStatusFilter: "",
      visitFilter: "",
      tubeStatusIds: [] as Array<number | null>,
      tubePositions: [] as string[],
      columns: [
        { key: "barcode", label: "Штрихкод", visible: true },
        { key: "researchInfo", label: "Исследование", visible: true },
        { key: "patientAge", label: "Возраст", visible: true },
        { key: "mainDiagnosis", label: "Основной диагноз", visible: true },
        { key: "patientGender", label: "Пол", visible: true },
        { key: "patientBirthDate", label: "Дата рождения", visible: true },
        { key: "patientNationality", label: "Национальность", visible: true },
        { key: "sampleTypeId", label: "Тип", visible: true },
        { key: "initialQuantity", label: "Начальное кол-во", visible: true },
        { key: "currentQuantity", label: "Текущее кол-во", visible: true },
        {
          key: "recommendedStorageMonths",
          label: "Рек. хранение",
          visible: true,
        },
        {
          key: "actualStorageMonths",
          label: "Факт. хранение",
          visible: true,
        },
        { key: "expiryStatus", label: "Срок хранения", visible: true },
        { key: "containerId", label: "Контейнер", visible: true },
        { key: "positionInContainer", label: "Позиция", visible: true },
        { key: "sampleStatusId", label: "Статус", visible: true },
        { key: "createdAtSample", label: "Создан", visible: true },
      ] as ColumnConfig[],
    };
  },
  computed: {
    visibleColumns(): ColumnConfig[] {
      return this.columns.filter((column) => column.visible);
    },
    tubeCount(): number {
      const quantity = Number(this.modalSample.initialQuantity || 0);
      return Number.isFinite(quantity) && quantity > 0 ? quantity : 0;
    },
    positionsForContainer() {
      const self = this as unknown as {
        containers: ContainerRef[];
        modalSample: SampleForm;
        samples: Sample[];
        modalMode: "create" | "edit";
        selectedSampleId: number | null;
        tubePositions: string[];
      };
      const container = self.containers.find(
        (item) => item.containerId === self.modalSample.containerId
      );
      if (!container) {
        return [];
      }
      const layout = resolveContainerLayout(container);
      if (!layout.total || !layout.columns) {
        return [];
      }
      const occupied = new Set<string>();
      self.samples
        .filter(
          (sample: Sample) => sample.containerId === container.containerId
        )
        .forEach((sample: Sample) => {
          if (
            self.modalMode === "edit" &&
            sample.sampleId === self.selectedSampleId
          ) {
            return;
          }
          extractPositionLabels(container, sample.positionInContainer).forEach(
            (label) => occupied.add(label)
          );
        });
      return Array.from({ length: layout.total }, (_, i) => {
        const index = i + 1;
        const row = Math.floor((index - 1) / layout.columns) + 1;
        const column = ((index - 1) % layout.columns) + 1;
        const value = `${getColumnLabel(column)}${row}`;
        return {
          value,
          disabled: occupied.has(value) && !self.tubePositions.includes(value),
        };
      });
    },
    actualMonthsPreview(): string {
      return this.calculateActualMonths(this.modalSample.collectionDate);
    },
    expiryPreview(): string {
      if (!this.modalSample.recommendedStorageMonths) {
        return "Годен";
      }
      const actual = Number(this.actualMonthsPreview);
      if (Number.isNaN(actual)) {
        return "Годен";
      }
      const remaining = this.modalSample.recommendedStorageMonths - actual;
      if (remaining < 0) {
        return "Просрочен";
      }
      if (remaining <= 2) {
        return "Истекает";
      }
      return "Годен";
    },
    filteredSamples(): Sample[] {
      const query = this.searchQuery.trim().toLowerCase();
      return this.samples.filter((sample) => {
        const matchesQuery =
          !query ||
          sample.barcode?.toLowerCase().includes(query) ||
          sample.sampleId?.toString().includes(query) ||
          sample.positionInContainer?.toLowerCase().includes(query);
        const matchesExpiry =
          !this.expiryStatus || sample.expiryStatus === this.expiryStatus;
        const matchesContainer =
          !this.containerFilter ||
          this.getContainerLabel(sample.containerId) === this.containerFilter;
        const matchesType =
          !this.sampleTypeFilter ||
          this.getSampleTypeName(sample.sampleTypeId) === this.sampleTypeFilter;
        const matchesStatus =
          !this.sampleStatusFilter ||
          this.getSampleStatusName(sample.sampleStatusId) ===
            this.sampleStatusFilter;
        const matchesVisit =
          !this.visitFilter ||
          this.getVisitLabel(sample.visitId) === this.visitFilter;
        return (
          matchesQuery &&
          matchesExpiry &&
          matchesContainer &&
          matchesType &&
          matchesStatus &&
          matchesVisit
        );
      });
    },
  },
  created() {
    this.fetchSamples();
    this.fetchReferenceData();
  },
  watch: {
    "modalSample.visitId": "syncCollectionFromVisit",
    "modalSample.containerId"() {
      this.modalSample.positionInContainer = "";
      this.tubePositions = [];
      this.syncTubeArrays();
    },
    "modalSample.initialQuantity"() {
      if (this.modalMode === "create") {
        this.modalSample.currentQuantity = this.modalSample.initialQuantity;
      }
      this.syncTubeArrays();
    },
  },
  methods: {
    async fetchSamples() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const response = await axios.get("/samples");
        this.samples = [...response.data].sort(
          (a: Sample, b: Sample) => a.sampleId - b.sampleId
        );
      } catch (error) {
        console.error("Ошибка при загрузке образцов:", error);
        this.errorMessage = "Не удалось загрузить образцы";
      } finally {
        this.loading = false;
      }
    },
    async fetchReferenceData() {
      try {
        const [
          typesResponse,
          statusesResponse,
          containersResponse,
          visitsResponse,
          patientsResponse,
          researchesResponse,
          diagnosesResponse,
        ] = await Promise.all([
          axios.get("/references/sample-types"),
          axios.get("/references/sample-statuses"),
          axios.get("/storage/containers"),
          axios.get("/visits"),
          axios.get("/patients"),
          axios.get("/researches"),
          axios.get("/references/diagnoses"),
        ]);
        this.sampleTypes = this.sortByNameWithUnknown(
          typesResponse.data,
          (item) => item.sampleTypeName
        );
        this.sampleStatuses = this.sortByNameWithUnknown(
          statusesResponse.data,
          (item) => item.sampleStatusName
        );
        this.containers = [...containersResponse.data].sort(
          (a: ContainerRef, b: ContainerRef) =>
            this.getContainerLabel(a.containerId).localeCompare(
              this.getContainerLabel(b.containerId),
              "ru-RU"
            )
        );
        this.visits = [...visitsResponse.data].sort(
          (a: VisitRef, b: VisitRef) => a.visitId - b.visitId
        );
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
    syncCollectionFromVisit() {
      if (this.modalMode === "edit" && this.modalSample.collectionDate) {
        return;
      }
      const visit = this.visits.find(
        (item) => item.visitId === this.modalSample.visitId
      );
      if (!visit?.collectionDate) {
        return;
      }
      this.modalSample.collectionDate = this.toDatetimeLocal(
        visit.collectionDate
      );
    },
    calculateActualMonths(value: string | null | undefined): string {
      if (!value) {
        return "—";
      }
      const fromDate = new Date(value);
      if (Number.isNaN(fromDate.getTime())) {
        return "—";
      }
      const now = new Date();
      const months =
        (now.getFullYear() - fromDate.getFullYear()) * 12 +
        (now.getMonth() - fromDate.getMonth());
      return months >= 0 ? String(months) : "—";
    },
    syncTubeArrays() {
      const quantity = this.tubeCount;
      if (quantity <= 0) {
        this.tubePositions = [];
        this.tubeStatusIds = [];
        return;
      }
      this.tubePositions = this.normalizeArray(
        this.tubePositions,
        quantity,
        this.modalSample.positionInContainer || ""
      );
      this.tubeStatusIds = this.normalizeArray(
        this.tubeStatusIds,
        quantity,
        this.modalSample.sampleStatusId ?? null
      );
    },
    normalizeArray<T>(values: T[], length: number, fallback: T): T[] {
      const result = values.slice(0, length);
      while (result.length < length) {
        result.push(fallback);
      }
      return result;
    },
    parseIdList(value?: string | null): number[] {
      if (!value) return [];
      return value
        .split(",")
        .map((item) => Number(item.trim()))
        .filter((item) => Number.isFinite(item));
    },
    getTubeStatusLabels(sample: Sample) {
      const tubeStatusIds = this.parseIdList(sample.tubeStatusIds);
      if (tubeStatusIds.length === 0) {
        return null;
      }
      const labels = tubeStatusIds.map((id) =>
        this.getSampleStatusName(id ?? null)
      );
      return labels.filter((label) => label && label !== "—").join("\n");
    },
    isPositionDisabled(
      position: { value: string; disabled: boolean },
      index: number
    ) {
      if (position.disabled) {
        return true;
      }
      return this.tubePositions.some(
        (value, idx) => idx !== index && value === position.value
      );
    },
    selectSample(sampleId: number) {
      this.selectedSampleId =
        this.selectedSampleId === sampleId ? null : sampleId;
    },
    editSampleRow(sampleId: number) {
      this.selectedSampleId = sampleId;
      this.openEditModal();
    },
    deleteSampleRow(sampleId: number) {
      this.selectedSampleId = sampleId;
      this.deleteSample();
    },
    openCreateModal() {
      this.errorMessage = "";
      this.successMessage = "";
      this.modalMode = "create";
      this.modalTitle = "Добавить образец";
      const storageStatusId = this.getStorageStatusId();
      this.tubePositions = [];
      this.tubeStatusIds = storageStatusId != null ? [storageStatusId] : [];
      this.modalSample = {
        visitId: null,
        barcode: "",
        sampleTypeId: null,
        initialQuantity: null,
        currentQuantity: null,
        recommendedStorageMonths: null,
        actualStorageMonths: null,
        expiryStatus: "",
        sampleStatusId: storageStatusId,
        containerId: null,
        positionInContainer: "",
        collectionDate: "",
      };
      this.syncTubeArrays();
      this.modalOpen = true;
    },
    openEditModal() {
      this.errorMessage = "";
      this.successMessage = "";
      const sample = this.samples.find(
        (item) => item.sampleId === this.selectedSampleId
      );
      if (!sample) {
        return;
      }
      this.modalMode = "edit";
      this.modalTitle = "Обновить образец";
      const container = this.containers.find(
        (item) => item.containerId === sample.containerId
      );
      const positionLabel =
        (container &&
          normalizePositionLabel(container, sample.positionInContainer)) ||
        sample.positionInContainer ||
        "";
      const positionLabels =
        container && sample.positionInContainer
          ? extractPositionLabels(container, sample.positionInContainer)
          : [];
      const tubeStatusIds = this.parseIdList(sample.tubeStatusIds);
      this.modalSample = {
        visitId: sample.visitId,
        barcode: sample.barcode,
        sampleTypeId: sample.sampleTypeId,
        initialQuantity: sample.initialQuantity,
        currentQuantity: sample.currentQuantity,
        recommendedStorageMonths: sample.recommendedStorageMonths,
        actualStorageMonths: sample.actualStorageMonths,
        expiryStatus: sample.expiryStatus || "",
        sampleStatusId: sample.sampleStatusId,
        containerId: sample.containerId,
        positionInContainer: positionLabel,
        collectionDate: this.toDatetimeLocal(sample.createdAtSample),
      };
      const quantity = Number(sample.initialQuantity || 0);
      if (quantity > 1) {
        this.tubePositions = this.normalizeArray(positionLabels, quantity, "");
        if (tubeStatusIds.length > 0) {
          this.tubeStatusIds = this.normalizeArray(
            tubeStatusIds,
            quantity,
            tubeStatusIds[0] ?? null
          );
        } else if (sample.sampleStatusId != null) {
          this.tubeStatusIds = Array.from(
            { length: quantity },
            () => sample.sampleStatusId
          );
        } else {
          this.tubeStatusIds = Array.from({ length: quantity }, () => null);
        }
      } else {
        this.tubePositions = [];
        this.tubeStatusIds = [];
      }
      this.syncTubeArrays();
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitModal() {
      this.errorMessage = "";
      this.successMessage = "";
      const quantity = Number(this.modalSample.initialQuantity || 0);
      if (quantity > 0) {
        if (this.tubeStatusIds.length !== quantity) {
          this.errorMessage =
            "Заполните статусы пробирок по количеству пробирок.";
          return;
        }
        if (this.tubeStatusIds.some((id) => id == null)) {
          this.errorMessage = "Укажите статус для каждой пробирки.";
          return;
        }
      }
      if (this.modalSample.containerId && quantity > 0) {
        if (this.tubePositions.length !== quantity) {
          this.errorMessage =
            "Количество выбранных позиций должно совпадать с количеством пробирок.";
          return;
        }
        if (this.tubePositions.some((value) => !value)) {
          this.errorMessage = "Укажите позицию для каждой пробирки.";
          return;
        }
        const unique = new Set(this.tubePositions);
        if (unique.size !== this.tubePositions.length) {
          this.errorMessage = "Позиции пробирок не должны повторяться.";
          return;
        }
      }
      if (this.modalMode === "create") {
        await this.createSample();
      } else {
        await this.updateSample();
      }
    },
    async createSample() {
      try {
        await axios.post("/samples", this.serializeSampleForm());
        this.successMessage = "Образец добавлен";
        this.closeModal();
        await this.fetchSamples();
      } catch (error) {
        console.error("Ошибка при добавлении образца:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении образца. Проверьте поля."
        );
      }
    },
    async updateSample() {
      if (!this.selectedSampleId) {
        return;
      }
      try {
        await axios.put(
          `/samples/${this.selectedSampleId}`,
          this.serializeSampleForm()
        );
        this.successMessage = "Образец обновлен";
        this.closeModal();
        await this.fetchSamples();
      } catch (error) {
        console.error("Ошибка при обновлении образца:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении образца. Проверьте поля."
        );
      }
    },
    async deleteSample() {
      if (!this.selectedSampleId) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранный образец?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/samples/${this.selectedSampleId}`);
        this.successMessage = "Образец удален";
        this.selectedSampleId = null;
        await this.fetchSamples();
      } catch (error) {
        console.error("Ошибка при удалении образца:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении образца."
        );
      }
    },
    serializeSampleForm() {
      const quantity = Number(this.modalSample.initialQuantity || 0);
      const tubePositions =
        quantity > 0 ? this.tubePositions.filter((value) => value) : [];
      const tubeStatusIds =
        quantity > 0 ? this.tubeStatusIds.filter((id) => id != null) : [];
      const hasUniformStatus =
        tubeStatusIds.length > 0 &&
        tubeStatusIds.every((id) => id === tubeStatusIds[0]);
      const resolvedSampleStatusId =
        quantity > 0
          ? hasUniformStatus
            ? tubeStatusIds[0] ?? null
            : tubeStatusIds[0] ?? null
          : this.modalSample.sampleStatusId;
      const positionValue =
        this.modalSample.containerId && quantity > 1
          ? tubePositions.join(", ")
          : this.modalSample.containerId && quantity === 1
          ? tubePositions[0] || null
          : this.modalSample.positionInContainer || null;
      return {
        visitId: this.toNullableNumber(this.modalSample.visitId),
        barcode: this.modalSample.barcode,
        sampleTypeId: this.toNullableNumber(this.modalSample.sampleTypeId),
        initialQuantity: this.toNullableNumber(
          this.modalSample.initialQuantity
        ),
        currentQuantity: this.toNullableNumber(
          this.modalSample.currentQuantity
        ),
        recommendedStorageMonths: this.toNullableNumber(
          this.modalSample.recommendedStorageMonths
        ),
        actualStorageMonths: this.toNullableNumber(
          this.modalSample.actualStorageMonths
        ),
        expiryStatus: null,
        sampleStatusId: this.toNullableNumber(resolvedSampleStatusId ?? null),
        tubeStatusIds:
          quantity > 0 && tubeStatusIds.length > 0
            ? tubeStatusIds.join(", ")
            : null,
        containerId: this.toNullableNumber(this.modalSample.containerId),
        positionInContainer: positionValue,
        collectionDate: this.modalSample.collectionDate || null,
      };
    },
    toNullableNumber(value: number | null) {
      return value === null ? null : value;
    },
    resolveErrorMessage(error: unknown, fallbackMessage: string) {
      if (axios.isAxiosError(error)) {
        const responseData = error.response?.data as
          | { message?: string }
          | undefined;
        return responseData?.message || fallbackMessage;
      }
      return fallbackMessage;
    },
    toDatetimeLocal(value: string) {
      if (!value) return "";
      return value.replace(" ", "T").slice(0, 16);
    },
    resetFilters() {
      this.searchQuery = "";
      this.expiryStatus = "";
      this.containerFilter = "";
      this.sampleTypeFilter = "";
      this.sampleStatusFilter = "";
      this.visitFilter = "";
    },
    getVisitLabel(visitId: number | null | undefined) {
      if (!visitId) return "—";
      const visit = this.visits.find((item) => item.visitId === visitId);
      if (!visit) return "—";
      return `Визит ${visit.visitNumber} — ${this.formatDate(
        visit.collectionDate
      )}`;
    },
    getVisitById(visitId: number | null | undefined) {
      if (!visitId) return undefined;
      return this.visits.find((item) => item.visitId === visitId);
    },
    getPatientByVisit(visitId: number | null | undefined) {
      const visit = this.getVisitById(visitId);
      if (!visit) return undefined;
      return this.patients.find((item) => item.patientId === visit.patientId);
    },
    getResearchLabel(researchId: number | null | undefined) {
      if (!researchId) return "—";
      const research = this.researches.find(
        (item) => item.researchId === researchId
      );
      if (!research) return "—";
      return `${research.researchNumber} — ${research.researchName}`;
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
    getGenderText(gender?: string) {
      switch (gender) {
        case "MALE":
          return "Мужской";
        case "FEMALE":
          return "Женский";
        default:
          return "Не указано";
      }
    },
    formatDateOnly(value: string) {
      if (!value) return "—";
      return new Date(value).toLocaleDateString("ru-RU");
    },
    getSampleTypeName(sampleTypeId: number | null | undefined) {
      if (!sampleTypeId) return "—";
      const type = this.sampleTypes.find(
        (item) => item.sampleTypeId === sampleTypeId
      );
      return type?.sampleTypeName || "—";
    },
    getSampleStatusName(sampleStatusId: number | null | undefined) {
      if (!sampleStatusId) return "—";
      const status = this.sampleStatuses.find(
        (item) => item.sampleStatusId === sampleStatusId
      );
      return status?.sampleStatusName || "—";
    },
    getStorageStatusId() {
      const target = this.sampleStatuses.find(
        (status) =>
          status.sampleStatusName?.trim().toLowerCase() === "в хранилище"
      );
      return target?.sampleStatusId ?? null;
    },
    getContainerLabel(containerId: number | null | undefined) {
      if (!containerId) return "—";
      const container = this.containers.find(
        (item) => item.containerId === containerId
      );
      if (!container) return "—";
      const type = container.containerType || "Контейнер";
      const number = container.containerNumber ?? container.containerId;
      return `${type} ${number}`;
    },
    formatDate(value: string) {
      if (!value) return "—";
      const date = new Date(value);
      if (Number.isNaN(date.getTime())) {
        return value;
      }
      return date.toLocaleString("ru-RU");
    },
    formatValue(sample: Sample, key: string) {
      if (key === "expiryStatus") {
        const value = sample.expiryStatus;
        if (value === "GREEN") return "Годен";
        if (value === "YELLOW") return "Истекает";
        if (value === "RED") return "Просрочен";
      }
      if (key === "researchInfo") {
        const visit = this.getVisitById(sample.visitId);
        return this.getResearchLabel(visit?.researchId);
      }
      if (key === "patientAge") {
        const visit = this.getVisitById(sample.visitId);
        return visit?.ageAtCollection ?? "—";
      }
      if (key === "mainDiagnosis") {
        const patient = this.getPatientByVisit(sample.visitId);
        return this.getDiagnosisDisplay(patient?.mainDiagnosisId ?? null);
      }
      if (key === "patientGender") {
        const patient = this.getPatientByVisit(sample.visitId);
        return this.getGenderText(patient?.gender);
      }
      if (key === "patientBirthDate") {
        const patient = this.getPatientByVisit(sample.visitId);
        return patient?.birthDate
          ? this.formatDateOnly(patient.birthDate)
          : "—";
      }
      if (key === "patientNationality") {
        const patient = this.getPatientByVisit(sample.visitId);
        return patient?.nationalityName || "—";
      }
      const value = (sample as unknown as Record<string, unknown>)[key];
      if (value === null || value === undefined || value === "") {
        return "—";
      }
      if (key === "sampleTypeId") {
        return this.getSampleTypeName(sample.sampleTypeId);
      }
      if (key === "sampleStatusId") {
        const tubeLabels = this.getTubeStatusLabels(sample);
        return tubeLabels ?? this.getSampleStatusName(sample.sampleStatusId);
      }
      if (key === "actualStorageMonths") {
        return this.calculateActualMonths(sample.createdAtSample);
      }
      if (key === "containerId") {
        return this.getContainerLabel(sample.containerId);
      }
      if (key === "positionInContainer") {
        const container = this.containers.find(
          (item) => item.containerId === sample.containerId
        );
        if (container) {
          const labels = extractPositionLabels(
            container,
            sample.positionInContainer
          );
          return labels.length > 0 ? labels.join("\n") : value;
        }
        return value;
      }
      return value;
    },
  },
});
</script>

<style scoped>
.sample-page {
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

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.columns-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-weight: 600;
}

.tube-grid {
  display: grid;
  gap: 10px;
}

.tube-row {
  display: grid;
  grid-template-columns: 120px 1fr 1fr;
  gap: 8px;
  align-items: center;
}

.tube-label {
  color: var(--text-secondary);
  font-weight: 600;
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

.readonly-field {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background-color: #e2d6c8;
  color: var(--text-secondary);
}

.form-actions {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.wide-button {
  min-width: 180px;
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

.header-button {
  width: 36px;
  height: 36px;
}

.table-wrapper {
  width: 100%;
  overflow: auto;
}

.samples-table {
  width: max-content;
  min-width: 1400px;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.samples-table th,
.samples-table td {
  border-bottom: 1px solid var(--border);
  padding: 10px;
  text-align: left;
  white-space: normal;
  max-width: 400px;
  word-break: break-word;
}

.samples-table td {
  white-space: pre-line;
}

.samples-table th.action-col,
.samples-table td.action-col {
  width: 44px;
  max-width: 44px;
  text-align: center;
}

.samples-table th {
  color: var(--text-primary);
  background-color: #f0e9df;
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

.samples-table tr.selected {
  background-color: #cfc1ad;
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
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

.empty-state {
  padding: 16px;
  background-color: #ddd1c4;
  border-radius: 8px;
  color: var(--text-secondary);
}
.alert-success,
.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}
</style>
