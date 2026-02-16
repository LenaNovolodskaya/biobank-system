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
      <div class="filters-header">
        <h3>Фильтры</h3>
        <p class="filters-hint">
          Выберите поле, укажите значение и нажмите «Добавить» для включения
          дополнительного фильтра
        </p>
      </div>
      <div class="filters-list">
        <div v-for="(row, index) in filterRows" :key="index" class="filter-row">
          <div class="form-group filter-field-group">
            <label>Поле</label>
            <select v-model="row.fieldKey" class="form-control">
              <option value="">— Выберите поле —</option>
              <option
                v-for="opt in availableFilterFieldsForRow(index)"
                :key="opt.key"
                :value="opt.key"
              >
                {{ opt.label }}
              </option>
            </select>
          </div>
          <div class="form-group filter-value-group">
            <label>Значение</label>
            <template v-if="row.fieldKey === 'barcode'">
              <input
                v-model="row.value"
                type="text"
                class="form-control"
                placeholder="Часть штрихкода"
              />
            </template>
            <template v-else-if="row.fieldKey === 'expiryStatus'">
              <select v-model="row.value" class="form-control">
                <option value="">Все</option>
                <option value="GREEN">Годен</option>
                <option value="YELLOW">Истекает</option>
                <option value="RED">Просрочен</option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'researchId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="r in researches"
                  :key="r.researchId"
                  :value="r.researchId"
                >
                  {{ getResearchLabel(r.researchId) }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'mainDiagnosisId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="d in diagnoses"
                  :key="d.diagnosisId"
                  :value="d.diagnosisId"
                >
                  {{ getDiagnosisDisplay(d.diagnosisId) }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'sampleTypeId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="type in sampleTypes"
                  :key="type.sampleTypeId"
                  :value="type.sampleTypeId"
                >
                  {{ type.sampleTypeName }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'containerId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="c in containers"
                  :key="c.containerId"
                  :value="c.containerId"
                >
                  {{ getContainerLabel(c.containerId) }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'initialQuantity'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Кол-во"
              />
            </template>
            <template v-else-if="row.fieldKey === 'currentQuantity'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Кол-во"
              />
            </template>
            <template v-else-if="row.fieldKey === 'recommendedStorageMonths'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Месяцев"
              />
            </template>
            <template v-else-if="row.fieldKey === 'ageMin'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Возраст от"
              />
            </template>
            <template v-else-if="row.fieldKey === 'ageMax'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Возраст до"
              />
            </template>
            <template v-else-if="row.fieldKey === 'patientGender'">
              <select v-model="row.value" class="form-control">
                <option value="">Все</option>
                <option value="MALE">Мужской</option>
                <option value="FEMALE">Женский</option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'patientNationalityId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="n in nationalities"
                  :key="n.nationalityId"
                  :value="n.nationalityId"
                >
                  {{ n.nationalityName }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'comorbidDiagnosisId'">
              <select v-model="row.value" class="form-control">
                <option :value="null">Все</option>
                <option
                  v-for="d in diagnoses"
                  :key="d.diagnosisId"
                  :value="d.diagnosisId"
                >
                  {{ getDiagnosisDisplay(d.diagnosisId) }}
                </option>
              </select>
            </template>
            <template v-else-if="row.fieldKey === 'createdAtSampleFrom'">
              <input
                v-model="row.value"
                type="date"
                class="form-control"
                placeholder="Дата от"
              />
            </template>
            <template v-else-if="row.fieldKey === 'actualStorageMonthsMin'">
              <input
                v-model.number="row.value"
                type="number"
                min="0"
                class="form-control"
                placeholder="Месяцев"
              />
            </template>
            <template v-else-if="row.fieldKey === 'patientBirthDateFrom'">
              <input
                v-model="row.value"
                type="date"
                class="form-control"
                placeholder="Дата от"
              />
            </template>
            <template v-else>
              <span class="filter-placeholder">—</span>
            </template>
          </div>
          <div class="filter-row-actions">
            <button
              class="icon-button"
              type="button"
              aria-label="Удалить фильтр"
              title="Удалить фильтр"
              @click="removeFilterRow(index)"
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
        <button class="btn btn-secondary" type="button" @click="addFilterRow">
          Добавить
        </button>
        <button class="btn btn-secondary" type="button" @click="resetFilters">
          Сброс
        </button>
      </div>
    </div>

    <div class="card columns-card">
      <div class="section-header">
        <h3>Столбцы</h3>
      </div>
      <div class="columns-grid">
        <label
          v-for="column in columnsForSelection"
          :key="column.key"
          class="checkbox"
        >
          <input type="checkbox" v-model="column.visible" />
          <span>{{ column.labelFull }}</span>
        </label>
      </div>
    </div>

    <div class="card table-card">
      <p v-if="loading">Загрузка...</p>
      <div v-else class="table-wrapper" @click="activeHeaderHelp = null">
        <table class="samples-table">
          <thead>
            <tr>
              <th v-for="column in visibleColumns" :key="column.key">
                <div class="header-help" @click.stop>
                  <span>{{ column.label }}</span>
                  <button
                    v-if="headerHelp[column.key]"
                    type="button"
                    class="help-button"
                    :aria-label="`Описание столбца ${column.label}`"
                    @click.stop="toggleHeaderHelp(column.key)"
                  >
                    ?
                  </button>
                  <div
                    v-if="activeHeaderHelp === column.key"
                    class="help-popover"
                  >
                    {{ headerHelp[column.key] }}
                  </div>
                </div>
              </th>
              <th class="action-col"></th>
              <th class="action-col"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredSamples.length === 0" class="empty-row">
              <td :colspan="visibleColumns.length + 2" class="empty-state">
                Нет образцов по заданным условиям
              </td>
            </tr>
            <template v-for="sample in filteredSamples" :key="sample.sampleId">
              <tr
                :class="{ selected: sample.sampleId === selectedSampleId }"
                @click="selectSample(sample.sampleId)"
              >
                <td v-for="column in visibleColumns" :key="column.key">
                  <template v-if="column.key === 'sampleTypeIcon'">
                    <img
                      v-if="getSampleTypeIconUrl(sample.sampleTypeId)"
                      :src="getSampleTypeIconUrl(sample.sampleTypeId)!"
                      :alt="getSampleTypeName(sample.sampleTypeId)"
                      class="sample-type-icon"
                    />
                    <span v-else>—</span>
                  </template>
                  <template v-else-if="column.key === 'barcode'">
                    <div class="barcode-cell">
                      <BarcodeSvg :value="sample.barcode" />
                      <span>{{ sample.barcode }}</span>
                    </div>
                  </template>
                  <template v-else-if="column.key === 'createdAtSample'">
                    <div class="date-cell">
                      <div>{{ formatDateOnly(sample.createdAtSample) }}</div>
                      <div
                        v-if="formatTimeOnly(sample.createdAtSample)"
                        class="time-line"
                      >
                        {{ formatTimeOnly(sample.createdAtSample) }}
                      </div>
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
              <tr
                v-if="sample.sampleId === selectedSampleId"
                class="specimens-details-row"
              >
                <td :colspan="visibleColumns.length + 2" class="specimens-cell">
                  <div class="specimens-header">Образцы:</div>
                  <div
                    v-if="getSampleSpecimens(sample).length === 0"
                    class="specimens-empty"
                  >
                    Нет аликвот
                  </div>
                  <table v-else class="specimens-table">
                    <thead>
                      <tr>
                        <th>Штрихкод</th>
                        <th>Статус</th>
                        <th>Позиция</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="specimen in getSampleSpecimens(sample)"
                        :key="specimen.specimenId"
                        class="specimen-row"
                      >
                        <td class="specimen-barcode-cell">
                          <BarcodeSvg :value="specimen.barcode" />
                          <span class="specimen-barcode">{{
                            specimen.barcode
                          }}</span>
                        </td>
                        <td class="specimen-status">
                          {{ getSampleStatusName(specimen.sampleStatusId) }}
                        </td>
                        <td class="specimen-position">
                          {{ specimen.positionInContainer || "—" }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </template>
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
            <label for="modalInitialQty">Начальное количество аликвот *</label>
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
            <label for="modalCurrentQty">Текущее количество аликвот *</label>
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
          <div class="form-group specimens-form-group">
            <label>Образцы</label>
            <div v-if="tubeCount > 0" class="specimens-form-grid">
              <div
                v-for="(specimen, index) in modalSample.specimens"
                :key="index"
                class="specimen-form-row"
              >
                <div class="specimen-form-label">Образец {{ index + 1 }}</div>
                <input
                  v-model="specimen.barcode"
                  type="text"
                  class="form-control"
                  placeholder="Штрихкод образца"
                />
                <select
                  v-model.number="specimen.sampleStatusId"
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
                  v-model="specimen.positionInContainer"
                  class="form-control"
                  :disabled="!modalSample.containerId"
                >
                  <option value="">Не указано</option>
                  <option
                    v-for="pos in positionsForSpecimenContainer(
                      modalSample.containerId,
                      index
                    )"
                    :key="pos.value"
                    :value="pos.value"
                    :disabled="pos.disabled"
                  >
                    {{ pos.value }}
                  </option>
                </select>
              </div>
            </div>
            <div v-else class="readonly-field">
              Укажите начальное количество аликвот.
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

interface Specimen {
  specimenId: number;
  barcode: string;
  sampleId: number;
  sampleStatusId: number | null;
  containerId: number | null;
  positionInContainer: string | null;
}

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
  containerId: number | null;
  createdAtSample: string;
  specimens?: Specimen[];
}

interface ColumnConfig {
  key: string;
  label: string;
  labelFull: string;
  visible: boolean;
}

interface SpecimenFormItem {
  barcode: string;
  sampleStatusId: number | null;
  containerId: number | null;
  positionInContainer: string;
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
  containerId: number | null;
  collectionDate: string;
  specimens: SpecimenFormItem[];
}

interface VisitRef {
  visitId: number;
  patientId: number;
  researchId: number;
  visitNumber: number;
  collectionDate: string;
  ageAtCollection: number;
  diagnosisId: number | null;
  comorbidDiagnosisIds?: number[];
}

interface PatientRef {
  patientId: number;
  patientBarcode: string;
  gender: string;
  birthDate: string;
  nationalityId?: number | null;
  nationalityName?: string;
  mainDiagnosisId?: number | null;
}

interface NationalityRef {
  nationalityId: number;
  nationalityName: string;
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
  iconPath?: string | null;
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
  numberingType: string | null;
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
      modalSampleInitializing: false,
      modalSample: {
        visitId: null,
        barcode: "",
        sampleTypeId: null,
        initialQuantity: null,
        currentQuantity: null,
        recommendedStorageMonths: null,
        actualStorageMonths: null,
        expiryStatus: "",
        containerId: null,
        collectionDate: "",
        specimens: [],
      } as SampleForm,
      visits: [] as VisitRef[],
      patients: [] as PatientRef[],
      researches: [] as ResearchRef[],
      diagnoses: [] as DiagnosisRef[],
      sampleTypes: [] as SampleTypeRef[],
      sampleStatuses: [] as SampleStatusRef[],
      containers: [] as ContainerRef[],
      nationalities: [] as NationalityRef[],
      filterRows: [
        { fieldKey: "", value: null as string | number | null },
      ] as Array<{ fieldKey: string; value: string | number | null }>,
      filterFieldDefinitions: [
        { key: "barcode", label: "Штрихкод" },
        { key: "sampleTypeId", label: "Тип" },
        { key: "containerId", label: "Контейнер" },
        { key: "initialQuantity", label: "Начальное количество" },
        { key: "currentQuantity", label: "Текущее количество" },
        { key: "createdAtSampleFrom", label: "Дата забора от" },
        {
          key: "actualStorageMonthsMin",
          label: "Фактический срок хранения от",
        },
        {
          key: "recommendedStorageMonths",
          label: "Рекомендованный срок хранения",
        },
        { key: "expiryStatus", label: "Статус срока хранения" },
        { key: "ageMin", label: "Возраст от" },
        { key: "ageMax", label: "Возраст до" },
        { key: "mainDiagnosisId", label: "Основной диагноз" },
        { key: "comorbidDiagnosisId", label: "Сопутствующие диагнозы" },
        { key: "patientGender", label: "Пол" },
        { key: "patientBirthDateFrom", label: "Дата рождения от" },
        { key: "patientNationalityId", label: "Национальность" },
        { key: "researchId", label: "Исследование" },
      ] as Array<{ key: string; label: string }>,
      columns: [
        { key: "sampleTypeIcon", label: "", labelFull: "", visible: true },
        {
          key: "barcode",
          label: "Штрихкод",
          labelFull: "Штрихкод",
          visible: true,
        },
        {
          key: "sampleTypeId",
          label: "Тип",
          labelFull: "Тип образца",
          visible: true,
        },
        {
          key: "containerId",
          label: "Контейнер",
          labelFull: "Контейнер",
          visible: true,
        },
        {
          key: "initialQuantity",
          label: "Нач. кол-во",
          labelFull: "Начальное количество",
          visible: true,
        },
        {
          key: "currentQuantity",
          label: "Тек. кол-во",
          labelFull: "Текущее количество",
          visible: true,
        },
        {
          key: "createdAtSample",
          label: "Дата забора",
          labelFull: "Дата забора",
          visible: true,
        },
        {
          key: "actualStorageMonths",
          label: "Факт. хранение",
          labelFull: "Фактический срок хранения",
          visible: true,
        },
        {
          key: "recommendedStorageMonths",
          label: "Рек. хранение",
          labelFull: "Рекомендованный срок хранения",
          visible: true,
        },
        {
          key: "expiryStatus",
          label: "Срок хранения",
          labelFull: "Статус срока хранения",
          visible: true,
        },
        {
          key: "patientAge",
          label: "Возраст",
          labelFull: "Возраст",
          visible: false,
        },
        {
          key: "mainDiagnosis",
          label: "Основной диагноз",
          labelFull: "Основной диагноз",
          visible: false,
        },
        {
          key: "comorbidDiagnoses",
          label: "Сопутствующие диагнозы",
          labelFull: "Сопутствующие диагнозы",
          visible: false,
        },
        {
          key: "patientGender",
          label: "Пол",
          labelFull: "Пол",
          visible: false,
        },
        {
          key: "patientBirthDate",
          label: "Дата рождения",
          labelFull: "Дата рождения",
          visible: false,
        },
        {
          key: "patientNationality",
          label: "Национальность",
          labelFull: "Национальность",
          visible: false,
        },
        {
          key: "researchInfo",
          label: "Исследование",
          labelFull: "Исследование",
          visible: false,
        },
      ] as ColumnConfig[],
      headerHelp: {
        sampleTypeIcon: "",
        barcode: "Штрихкод образца",
        researchInfo: "Номер и название темы исследования",
        patientAge: "Возраст пациента на момент забора биоматериала",
        mainDiagnosis:
          "Основной диагноз, поставленный пациенту, у которого взят биоматериал",
        comorbidDiagnoses:
          "Сопутствующие диагнозы, зафиксированные при визите забора биоматериала",
        patientGender: "Пол пациента, у которого взят биоматериал",
        patientBirthDate: "Дата рождения пациента, у которого взят биоматериал",
        patientNationality:
          "Национальность пациента, у которого взят биоматериал",
        sampleTypeId: "Вид забранного биологического материала",
        initialQuantity:
          "Общее количество идентичных аликвот, полученных от пациента и первоначально зарегистрированных в хранилище",
        currentQuantity:
          "Текущее количество аликвот данного образца, оставшихся в хранилище на данный момент",
        recommendedStorageMonths:
          "Срок, в течение которого образец гарантированно пригоден для анализа согласно протоколу исследования",
        actualStorageMonths:
          "Период времени, прошедший с момента забора биоматериала до текущей даты",
        expiryStatus:
          "Индикатор пригодности образца на основе сравнения фактического и рекомендованного срока (годен/истекает/просрочен)",
        containerId: "Название и номер места долгосрочного хранения",
        createdAtSample:
          "Дата и точное время процедуры взятия биоматериала у пациента",
      } as Record<string, string>,
      activeHeaderHelp: null as string | null,
    };
  },
  computed: {
    columnsForSelection(): ColumnConfig[] {
      return this.columns.filter((c) => c.key !== "sampleTypeIcon");
    },
    visibleColumns(): ColumnConfig[] {
      return this.columns.filter((column) => column.visible);
    },
    tubeCount(): number {
      const quantity = Number(this.modalSample.initialQuantity || 0);
      return Number.isFinite(quantity) && quantity > 0 ? quantity : 0;
    },
    actualMonthsPreview(): string {
      return this.calculateActualMonths(this.modalSample.collectionDate);
    },
    expiryPreview(): string {
      return this.getExpiryStatusLabel(
        this.getExpiryStatusCode(
          this.modalSample.collectionDate,
          this.modalSample.recommendedStorageMonths
        )
      );
    },
    filteredSamples(): Sample[] {
      const activeFilters = this.filterRows.filter(
        (r) => r.fieldKey && this.isFilterValueActive(r.fieldKey, r.value)
      );
      if (activeFilters.length === 0) {
        return this.samples;
      }
      return this.samples.filter((sample) => {
        return activeFilters.every((f) =>
          this.sampleMatchesFilter(sample, f.fieldKey, f.value)
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
    "modalSample.initialQuantity"() {
      if (this.modalMode === "create") {
        this.modalSample.currentQuantity = this.modalSample.initialQuantity;
      }
      this.syncSpecimens();
    },
    "modalSample.barcode"() {
      if (this.modalMode === "create" && this.tubeCount > 0) {
        this.syncSpecimens();
      }
    },
    "modalSample.containerId"() {
      this.syncSpecimens();
    },
  },
  methods: {
    toggleHeaderHelp(key: string) {
      this.activeHeaderHelp = this.activeHeaderHelp === key ? null : key;
    },
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
          nationalitiesResponse,
        ] = await Promise.all([
          axios.get("/references/sample-types"),
          axios.get("/references/sample-statuses"),
          axios.get("/storage/containers"),
          axios.get("/visits"),
          axios.get("/patients"),
          axios.get("/researches"),
          axios.get("/references/diagnoses"),
          axios.get("/references/nationalities"),
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
        this.nationalities = [...nationalitiesResponse.data].sort(
          (a: NationalityRef, b: NationalityRef) =>
            a.nationalityName.localeCompare(b.nationalityName, "ru-RU")
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
      const startDate = this.getDateOnly(value);
      const today = this.getTodayDateOnly();
      if (!startDate || !today) {
        return "—";
      }
      if (today < startDate) {
        return "0 мес 0 дн";
      }
      const diff = this.getMonthsDaysDiff(startDate, today);
      if (!diff) {
        return "—";
      }
      return `${diff.months} мес ${diff.days} дн`;
    },
    getActualStorageMonthsNumber(sample: Sample): number | null {
      const startDate = this.getDateOnly(sample.createdAtSample);
      const today = this.getTodayDateOnly();
      if (!startDate || !today) return null;
      const diff = this.getMonthsDaysDiff(startDate, today);
      if (!diff) return null;
      return diff.months + diff.days / 30;
    },
    getMonthsDaysDiff(startDate: Date, endDate: Date) {
      if (endDate < startDate) {
        return null;
      }
      let months =
        (endDate.getFullYear() - startDate.getFullYear()) * 12 +
        (endDate.getMonth() - startDate.getMonth());
      if (endDate.getDate() < startDate.getDate()) {
        months -= 1;
      }
      const anchor = this.addMonthsClamped(startDate, months);
      const days = Math.round(
        (endDate.getTime() - anchor.getTime()) / (24 * 60 * 60 * 1000)
      );
      return { months, days: Math.max(days, 0) };
    },
    addMonthsClamped(date: Date, monthsToAdd: number) {
      const year = date.getFullYear();
      const month = date.getMonth() + monthsToAdd;
      const day = date.getDate();
      const targetYear = year + Math.floor(month / 12);
      const targetMonth = ((month % 12) + 12) % 12;
      const lastDay = new Date(targetYear, targetMonth + 1, 0).getDate();
      return new Date(targetYear, targetMonth, Math.min(day, lastDay));
    },
    syncSpecimens() {
      const quantity = this.tubeCount;
      const current = this.modalSample.specimens;
      if (quantity <= 0) {
        this.modalSample.specimens = [];
        return;
      }
      const baseBarcode = this.modalSample.barcode || "";
      const existing = [...current];
      const result: SpecimenFormItem[] = [];
      for (let i = 0; i < quantity; i += 1) {
        if (i < existing.length) {
          result.push(existing[i]);
        } else {
          result.push({
            barcode: baseBarcode ? `${baseBarcode}-${i + 1}` : "",
            sampleStatusId: this.getStorageStatusId(),
            containerId: this.modalSample.containerId,
            positionInContainer: "",
          });
        }
      }
      this.modalSample.specimens = result;
    },
    getSampleSpecimens(sample: Sample): Specimen[] {
      return sample.specimens || [];
    },
    positionsForSpecimenContainer(
      containerId: number | null,
      currentIndex: number
    ): Array<{ value: string; disabled: boolean }> {
      if (!containerId) return [];
      const container = this.containers.find(
        (c: ContainerRef) => c.containerId === containerId
      );
      if (!container) return [];
      const layout = resolveContainerLayout(container);
      if (!layout.total || !layout.columns) return [];
      const occupied = new Set<string>();
      this.samples.forEach((s: Sample) => {
        if (this.modalMode === "edit" && s.sampleId === this.selectedSampleId)
          return;
        (s.specimens || []).forEach((a: Specimen) => {
          if (a.containerId === containerId && a.positionInContainer)
            occupied.add(a.positionInContainer);
        });
      });
      this.modalSample.specimens.forEach((a, idx) => {
        if (
          idx !== currentIndex &&
          a.containerId === containerId &&
          a.positionInContainer
        ) {
          occupied.add(a.positionInContainer);
        }
      });
      const getPositionLabel = (
        container: ContainerRef,
        row: number,
        column: number,
        columns: number
      ): string => {
        const t = container.numberingType || "LETTER_DIGIT";
        const index0 = (row - 1) * columns + (column - 1);
        if (t === "SEQUENTIAL") return String(index0 + 1);
        if (t === "DIGIT_LETTER") return `${row}${getColumnLabel(column)}`;
        if (t === "DIGIT_DIGIT") return `${row}/${column}`;
        return `${getColumnLabel(column)}${row}`;
      };
      return Array.from({ length: layout.total }, (_, i) => {
        const index = i + 1;
        const row = Math.floor((index - 1) / layout.columns) + 1;
        const column = ((index - 1) % layout.columns) + 1;
        const value = getPositionLabel(container, row, column, layout.columns);
        return {
          value,
          disabled: occupied.has(value),
        };
      });
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
      this.modalSample = {
        visitId: null,
        barcode: "",
        sampleTypeId: null,
        initialQuantity: null,
        currentQuantity: null,
        recommendedStorageMonths: null,
        actualStorageMonths: null,
        expiryStatus: "",
        containerId: null,
        collectionDate: "",
        specimens: [],
      };
      this.syncSpecimens();
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
      this.modalSampleInitializing = true;
      this.modalMode = "edit";
      this.modalTitle = "Обновить образец";
      const specimens = sample.specimens || [];
      this.modalSample = {
        visitId: sample.visitId,
        barcode: sample.barcode,
        sampleTypeId: sample.sampleTypeId,
        initialQuantity: sample.initialQuantity,
        currentQuantity: sample.currentQuantity,
        recommendedStorageMonths: sample.recommendedStorageMonths,
        actualStorageMonths: sample.actualStorageMonths,
        expiryStatus: sample.expiryStatus || "",
        containerId: sample.containerId,
        collectionDate: this.toDatetimeLocal(sample.createdAtSample),
        specimens:
          specimens.length > 0
            ? specimens.map((a) => ({
                barcode: a.barcode,
                sampleStatusId: a.sampleStatusId,
                containerId: a.containerId,
                positionInContainer: a.positionInContainer || "",
              }))
            : [],
      };
      this.syncSpecimens();
      this.$nextTick(() => {
        this.modalSampleInitializing = false;
      });
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
    },
    async submitModal() {
      this.errorMessage = "";
      this.successMessage = "";
      const quantity = Number(this.modalSample.initialQuantity || 0);
      const specimens = this.modalSample.specimens || [];
      if (quantity > 0 && specimens.length !== quantity) {
        this.errorMessage =
          "Количество аликвот должно совпадать с начальным количеством.";
        return;
      }
      if (quantity > 0) {
        const emptyBarcodes = specimens.filter(
          (a) => !a.barcode || !a.barcode.trim()
        );
        if (emptyBarcodes.length > 0) {
          this.errorMessage = "Укажите штрихкод для каждой аликвоты.";
          return;
        }
        const barcodeSet = new Set(specimens.map((a) => a.barcode.trim()));
        if (barcodeSet.size !== specimens.length) {
          this.errorMessage = "Штрихкоды аликвот не должны повторяться.";
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
      const sampleContainerId = this.modalSample.containerId ?? null;
      const specimens = (this.modalSample.specimens || []).map((a) => ({
        barcode: a.barcode.trim(),
        sampleStatusId: a.sampleStatusId,
        containerId: sampleContainerId,
        positionInContainer: a.positionInContainer || null,
      }));
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
        containerId: this.toNullableNumber(this.modalSample.containerId),
        collectionDate: this.modalSample.collectionDate || null,
        specimens,
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
    addFilterRow() {
      this.filterRows.push({
        fieldKey: "",
        value: null as string | number | null,
      });
    },
    removeFilterRow(index: number) {
      this.filterRows.splice(index, 1);
      if (this.filterRows.length === 0) {
        this.filterRows.push({
          fieldKey: "",
          value: null as string | number | null,
        });
      }
    },
    availableFilterFieldsForRow(rowIndex: number) {
      const usedKeys = new Set(
        this.filterRows
          .map((r, i) => (i !== rowIndex && r.fieldKey ? r.fieldKey : null))
          .filter(Boolean)
      );
      return this.filterFieldDefinitions.filter(
        (def) => !usedKeys.has(def.key)
      );
    },
    isFilterValueActive(fieldKey: string, value: string | number | null) {
      if (value === null || value === undefined) {
        return false;
      }
      if (value === "" && fieldKey !== "barcode") {
        return false;
      }
      if (fieldKey === "barcode") {
        return typeof value === "string" && value.trim().length > 0;
      }
      if (
        fieldKey === "ageMin" ||
        fieldKey === "ageMax" ||
        fieldKey === "initialQuantity" ||
        fieldKey === "currentQuantity" ||
        fieldKey === "recommendedStorageMonths" ||
        fieldKey === "actualStorageMonthsMin"
      ) {
        const num = Number(value);
        return Number.isFinite(num) && num >= 0;
      }
      if (
        fieldKey === "createdAtSampleFrom" ||
        fieldKey === "patientBirthDateFrom"
      ) {
        return typeof value === "string" && value.trim().length > 0;
      }
      return true;
    },
    sampleMatchesFilter(
      sample: Sample,
      fieldKey: string,
      value: string | number | null
    ): boolean {
      if (fieldKey === "barcode") {
        const search = String(value || "")
          .trim()
          .toLowerCase();
        return !search || (sample.barcode || "").toLowerCase().includes(search);
      }
      if (fieldKey === "expiryStatus") {
        return (
          !value || this.getSampleExpiryStatus(sample) === (value as string)
        );
      }
      if (fieldKey === "researchId") {
        const visit = this.getVisitById(sample.visitId);
        return value == null || visit?.researchId === value;
      }
      if (fieldKey === "mainDiagnosisId") {
        const patient = this.getPatientByVisit(sample.visitId);
        return value == null || patient?.mainDiagnosisId === value;
      }
      if (fieldKey === "comorbidDiagnosisId") {
        const visit = this.getVisitById(sample.visitId);
        const ids = visit?.comorbidDiagnosisIds ?? [];
        return value == null || ids.includes(value as number);
      }
      if (fieldKey === "createdAtSampleFrom") {
        const sampleDate = this.getDateOnly(sample.createdAtSample);
        const filterDate = this.getDateOnly(value as string);
        return !filterDate || !sampleDate || sampleDate >= filterDate;
      }
      if (fieldKey === "actualStorageMonthsMin") {
        const months = this.getActualStorageMonthsNumber(sample);
        const minVal = Number(value);
        return !Number.isFinite(minVal) || (months != null && months >= minVal);
      }
      if (fieldKey === "patientBirthDateFrom") {
        const patient = this.getPatientByVisit(sample.visitId);
        const birthDate = patient?.birthDate
          ? this.getDateOnly(patient.birthDate)
          : null;
        const filterDate = this.getDateOnly(value as string);
        return !filterDate || !birthDate || birthDate >= filterDate;
      }
      if (fieldKey === "sampleTypeId") {
        return value == null || sample.sampleTypeId === value;
      }
      if (fieldKey === "containerId") {
        return value == null || sample.containerId === value;
      }
      if (fieldKey === "initialQuantity") {
        return value == null || sample.initialQuantity === value;
      }
      if (fieldKey === "currentQuantity") {
        return value == null || sample.currentQuantity === value;
      }
      if (fieldKey === "recommendedStorageMonths") {
        return value == null || sample.recommendedStorageMonths === value;
      }
      if (fieldKey === "ageMin") {
        const visit = this.getVisitById(sample.visitId);
        const age = visit?.ageAtCollection ?? null;
        const minNum = Number(value);
        return age == null || !Number.isFinite(minNum) || age >= minNum;
      }
      if (fieldKey === "ageMax") {
        const visit = this.getVisitById(sample.visitId);
        const age = visit?.ageAtCollection ?? null;
        const maxNum = Number(value);
        return age == null || !Number.isFinite(maxNum) || age <= maxNum;
      }
      if (fieldKey === "patientGender") {
        const patient = this.getPatientByVisit(sample.visitId);
        return value == null || patient?.gender === value;
      }
      if (fieldKey === "patientNationalityId") {
        const patient = this.getPatientByVisit(sample.visitId);
        return value == null || patient?.nationalityId === value;
      }
      return true;
    },
    resetFilters() {
      this.filterRows = [
        { fieldKey: "", value: null as string | number | null },
      ];
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
    formatTimeOnly(value: string) {
      if (!value) return "";
      return new Date(value).toLocaleTimeString("ru-RU", {
        hour: "2-digit",
        minute: "2-digit",
      });
    },
    getSampleTypeName(sampleTypeId: number | null | undefined) {
      if (!sampleTypeId) return "—";
      const type = this.sampleTypes.find(
        (item) => item.sampleTypeId === sampleTypeId
      );
      return type?.sampleTypeName || "—";
    },
    getSampleTypeIconUrl(
      sampleTypeId: number | null | undefined
    ): string | null {
      if (!sampleTypeId) return null;
      const type = this.sampleTypes.find(
        (item) => item.sampleTypeId === sampleTypeId
      );
      if (!type?.iconPath) return null;
      return `/img/sample-types/${type.iconPath}`;
    },
    getSampleStatusName(sampleStatusId: number | null | undefined) {
      if (!sampleStatusId) return "—";
      const status = this.sampleStatuses.find(
        (item) => item.sampleStatusId === sampleStatusId
      );
      return status?.sampleStatusName || "—";
    },
    getExpiryStatusCode(
      collectionDate: string | null | undefined,
      recommendedMonths: number | null | undefined
    ) {
      if (!recommendedMonths) {
        return "GREEN";
      }
      const startDate = this.getDateOnly(collectionDate);
      if (!startDate) {
        return "GREEN";
      }
      const expiryDate = this.addMonthsClamped(startDate, recommendedMonths);
      const today = this.getTodayDateOnly();
      if (!today) {
        return "GREEN";
      }
      if (today > expiryDate) {
        return "RED";
      }
      let remainingMonths =
        (expiryDate.getFullYear() - today.getFullYear()) * 12 +
        (expiryDate.getMonth() - today.getMonth());
      if (today.getDate() > expiryDate.getDate()) {
        remainingMonths -= 1;
      }
      return remainingMonths < 2 ? "YELLOW" : "GREEN";
    },
    getExpiryStatusLabel(status: string) {
      if (status === "RED") return "Просрочен";
      if (status === "YELLOW") return "Истекает";
      return "Годен";
    },
    getSampleExpiryStatus(sample: Sample) {
      return this.getExpiryStatusCode(
        sample.createdAtSample,
        sample.recommendedStorageMonths
      );
    },
    getDateOnly(value: string | null | undefined) {
      if (!value) return null;
      const normalized = value.slice(0, 10);
      const [year, month, day] = normalized.split("-").map(Number);
      if (!year || !month || !day) {
        return null;
      }
      return new Date(year, month - 1, day);
    },
    getTodayDateOnly() {
      const now = new Date();
      return new Date(now.getFullYear(), now.getMonth(), now.getDate());
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
        return this.getExpiryStatusLabel(this.getSampleExpiryStatus(sample));
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
      if (key === "comorbidDiagnoses") {
        const visit = this.getVisitById(sample.visitId);
        const ids = visit?.comorbidDiagnosisIds ?? [];
        if (ids.length === 0) return "—";
        const labels = ids
          .map((id) => this.getDiagnosisDisplay(id))
          .filter((l) => l && l !== "—");
        return labels.length > 0 ? labels.join(";\n") : "—";
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
      if (key === "actualStorageMonths") {
        return this.calculateActualMonths(sample.createdAtSample);
      }
      if (key === "containerId") {
        return this.getContainerLabel(sample.containerId);
      }
      return value;
    },
  },
});
</script>

<style scoped>
.sample-page {
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

.filters-header {
  margin-bottom: 16px;
}

.filters-header h3 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
  font-size: 1rem;
}

.filters-hint {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.filters-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-field-group {
  flex: 0 0 180px;
  min-width: 140px;
}

.filter-value-group {
  flex: 0 0 220px;
  max-width: 220px;
}

.filter-value-group .filter-placeholder {
  display: inline-block;
  padding: 10px;
  color: var(--text-secondary);
}

.filter-row-actions {
  flex-shrink: 0;
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

.specimens-details-row {
  background-color: #f0e9df;
}

.specimens-cell {
  padding: 12px 16px !important;
  vertical-align: top;
}

.specimens-header {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.specimens-table {
  border-collapse: collapse;
  width: max-content;
  margin-top: 8px;
}

.specimens-table thead th,
.specimens-table tbody td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border);
  white-space: normal;
  word-break: break-word;
}

.specimens-table thead th {
  background-color: #f0e9df;
  color: var(--text-primary);
  border-bottom: 2px solid var(--border);
  font-weight: 600;
}

.specimens-table thead th:nth-child(1),
.specimens-table tbody td:nth-child(1) {
  min-width: 160px;
}

.specimens-table thead th:nth-child(2),
.specimens-table tbody td:nth-child(2) {
  min-width: 110px;
}

.specimens-table thead th:nth-child(3),
.specimens-table tbody td:nth-child(3) {
  min-width: 90px;
}

.specimen-barcode-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.specimen-barcode-cell :deep(.barcode-svg) {
  max-width: 140px;
}

.specimen-barcode {
  font-weight: 500;
  color: var(--text-primary);
}

.specimen-status,
.specimen-position {
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.specimens-empty {
  color: var(--text-secondary);
  font-style: italic;
  padding: 8px 0;
}

.specimens-form-grid {
  display: grid;
  gap: 12px;
}

.specimen-form-row {
  display: grid;
  grid-template-columns: 100px 1fr 140px 120px;
  gap: 8px;
  align-items: center;
}

.specimen-form-label {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.9rem;
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
  max-width: 300px;
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

.samples-table .date-cell {
  white-space: normal;
  line-height: 1.2;
}

.samples-table .time-line {
  font-size: 0.85em;
  color: var(--text-secondary);
}

.sample-type-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  vertical-align: middle;
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
