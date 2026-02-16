<template>
  <div class="storage-page">
    <h2>Хранилища</h2>

    <div v-if="successMessage" class="alert alert-success">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger">
      {{ errorMessage }}
    </div>

    <div class="storage-layout">
      <section class="card left-panel">
        <div class="section-header">
          <h3>Локации</h3>
          <div class="action-buttons">
            <button
              class="icon-button"
              title="Добавить"
              aria-label="Добавить"
              @click="openLocationModal"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M11 5h2v14h-2zM5 11h14v2H5z" fill="currentColor" />
              </svg>
            </button>
            <button
              class="icon-button"
              title="Обновить"
              aria-label="Обновить"
              :disabled="!selectedLocationId"
              @click="openEditLocationModal"
            >
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path
                  d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                  fill="currentColor"
                />
              </svg>
            </button>
            <button
              class="icon-button danger"
              title="Удалить"
              aria-label="Удалить"
              :disabled="!selectedLocationId"
              @click="deleteLocation"
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

        <p v-if="loading">Загрузка...</p>
        <div v-else-if="locations.length === 0" class="empty-state">
          Локации пока не добавлены
        </div>

        <div v-else class="tree">
          <div
            v-for="location in locations"
            :key="location.locationId"
            class="tree-item"
          >
            <button
              class="tree-button"
              :class="{ active: selectedLocationId === location.locationId }"
              :title="`${location.address}, ${location.roomNumber}`"
              @click="selectLocation(location.locationId)"
            >
              {{ location.locationName }}
            </button>

            <div
              v-if="selectedLocationId === location.locationId"
              class="tree-children"
            >
              <div class="location-meta">
                {{ location.address }}, {{ location.roomNumber }}
              </div>
              <div class="section-header">
                <h4>Хранилища</h4>
                <div class="action-buttons">
                  <button
                    class="icon-button"
                    title="Добавить"
                    aria-label="Добавить"
                    @click="openUnitModal"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path
                        d="M11 5h2v14h-2zM5 11h14v2H5z"
                        fill="currentColor"
                      />
                    </svg>
                  </button>
                  <button
                    class="icon-button"
                    title="Обновить"
                    aria-label="Обновить"
                    :disabled="!selectedUnitId"
                    @click="openEditUnitModal"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path
                        d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                        fill="currentColor"
                      />
                    </svg>
                  </button>
                  <button
                    class="icon-button danger"
                    title="Удалить"
                    aria-label="Удалить"
                    :disabled="!selectedUnitId"
                    @click="deleteUnit"
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

              <div v-if="location.units.length === 0" class="empty-state">
                Хранилища отсутствуют
              </div>

              <div v-else class="tree">
                <div
                  v-for="unit in location.units"
                  :key="unit.unitId"
                  class="tree-item"
                >
                  <button
                    class="tree-button"
                    :class="{ active: selectedUnitId === unit.unitId }"
                    @click="selectUnit(unit.unitId)"
                  >
                    {{ unit.unitName }} ({{ unit.unitTypeName }})
                  </button>

                  <div
                    v-if="selectedUnitId === unit.unitId"
                    class="tree-children"
                  >
                    <div class="section-header">
                      <h5>Полки</h5>
                      <div class="action-buttons">
                        <button
                          class="icon-button"
                          title="Добавить"
                          aria-label="Добавить"
                          @click="openContainerModal"
                        >
                          <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path
                              d="M11 5h2v14h-2zM5 11h14v2H5z"
                              fill="currentColor"
                            />
                          </svg>
                        </button>
                        <button
                          class="icon-button"
                          title="Обновить"
                          aria-label="Обновить"
                          :disabled="!selectedContainerId"
                          @click="openEditContainerModal"
                        >
                          <svg viewBox="0 0 24 24" aria-hidden="true">
                            <path
                              d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                              fill="currentColor"
                            />
                          </svg>
                        </button>
                        <button
                          class="icon-button danger"
                          title="Удалить"
                          aria-label="Удалить"
                          :disabled="!selectedContainerId"
                          @click="deleteContainer"
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

                    <div v-if="unitShelves.length === 0" class="empty-state">
                      Полки не указаны
                    </div>

                    <div v-else class="tree">
                      <div
                        v-for="shelf in unitShelves"
                        :key="shelf.index"
                        class="tree-item"
                      >
                        <button
                          class="tree-button"
                          :class="{
                            active: selectedShelfIndex === shelf.index,
                          }"
                          @click="selectShelf(shelf.index)"
                        >
                          {{ shelf.label }}
                        </button>

                        <div
                          v-if="selectedShelfIndex === shelf.index"
                          class="tree-children"
                        >
                          <div
                            v-if="filteredContainers.length === 0"
                            class="empty-state"
                          >
                            Контейнеры отсутствуют
                          </div>
                          <div v-else class="tree">
                            <button
                              v-for="container in filteredContainers"
                              :key="container.containerId"
                              class="tree-button"
                              :class="{
                                active:
                                  selectedContainerId === container.containerId,
                              }"
                              draggable="true"
                              @dragstart="
                                onDragStart(
                                  container.containerId,
                                  selectedShelfIndex
                                )
                              "
                              @dragover="onDragOver"
                              @drop="
                                onDropOnContainer(
                                  selectedShelfIndex,
                                  container.containerId
                                )
                              "
                              @click="selectContainer(container.containerId)"
                            >
                              {{
                                container.templateName ||
                                container.containerNumber ||
                                "Контейнер"
                              }}
                              {{
                                container.containerNumber
                                  ? `№${container.containerNumber}`
                                  : ""
                              }}
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="card right-panel">
        <div class="section-header">
          <h3>Схема хранилища</h3>
        </div>

        <div v-if="!selectedUnit" class="empty-state">
          Выберите хранилище слева, чтобы увидеть схему.
        </div>

        <div v-else class="scheme">
          <div
            v-for="shelf in schemeShelves"
            :key="shelf.key"
            class="shelf-block"
            @dragover="onDragOver"
            @drop="shelf.index ? onDropOnShelf(shelf.index) : undefined"
          >
            <div class="shelf-title">{{ shelf.label }}</div>
            <div v-if="shelf.containers.length === 0" class="empty-state">
              Контейнеры отсутствуют
            </div>
            <div v-else class="shelf-containers">
              <div
                v-for="container in shelf.containers"
                :key="container.containerId"
                class="container-scheme"
                :class="{
                  selected: selectedContainerId === container.containerId,
                }"
                :style="{ '--grid-width': containerGridWidth(container) }"
                draggable="true"
                @dragstart="onDragStart(container.containerId, shelf.index)"
                @dragover="onDragOver"
                @drop="onDropOnContainer(shelf.index, container.containerId)"
                @click="selectContainer(container.containerId)"
              >
                <div class="container-path">
                  {{ containerPath(container, shelf.label) }}
                </div>
                <div
                  v-if="container.numberingType === 'SEQUENTIAL'"
                  class="scheme-grid scheme-grid-sequential"
                  :style="{
                    gridTemplateColumns: `repeat(${containerGridColumns(
                      container
                    )}, var(--cell-size))`,
                    gridTemplateRows: `repeat(${containerGridRows(
                      container
                    )}, calc(var(--cell-label-height) + var(--cell-size))`,
                  }"
                >
                  <template
                    v-for="row in containerGridRows(container)"
                    :key="`row-${row}`"
                  >
                    <div
                      v-for="cell in containerRowCells(container, row)"
                      :key="cell.label"
                      class="scheme-cell-sequential-wrapper"
                    >
                      <div class="scheme-cell-label-above">
                        {{ cell.label }}
                      </div>
                      <div
                        class="scheme-cell"
                        :class="{
                          filled: cell.filled,
                          'filled-with-icon':
                            cell.filled && !!cell.sampleTypeIconUrl,
                          'expiry-green': cell.expiryStatus === 'GREEN',
                          'expiry-yellow': cell.expiryStatus === 'YELLOW',
                          'expiry-red': cell.expiryStatus === 'RED',
                        }"
                        :title="cell.sampleBarcode || ''"
                        @click="onCellClick(container, cell)"
                      >
                        <div
                          v-if="cell.filled && cell.sampleTypeIconUrl"
                          class="scheme-cell-icon-wrap"
                        >
                          <img
                            :src="cell.sampleTypeIconUrl"
                            class="scheme-cell-icon"
                            :alt="''"
                          />
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
                <div
                  v-else
                  class="scheme-grid"
                  :style="{
                    gridTemplateColumns: `auto repeat(${containerGridColumns(
                      container
                    )}, var(--cell-size))`,
                    gridTemplateRows: `auto repeat(${containerGridRows(
                      container
                    )}, var(--cell-size))`,
                  }"
                >
                  <div class="scheme-grid-corner"></div>
                  <div
                    v-for="col in containerGridColumns(container)"
                    :key="`col-${col}`"
                    class="scheme-grid-header"
                  >
                    {{ getColumnHeaderLabel(container, col) }}
                  </div>
                  <template
                    v-for="row in containerGridRows(container)"
                    :key="`row-${row}`"
                  >
                    <div class="scheme-grid-header row-header">
                      {{ getRowHeaderLabel(container, row) }}
                    </div>
                    <div
                      v-for="cell in containerRowCells(container, row)"
                      :key="cell.label"
                      class="scheme-cell"
                      :class="{
                        filled: cell.filled,
                        'filled-with-icon':
                          cell.filled && !!cell.sampleTypeIconUrl,
                        'expiry-green': cell.expiryStatus === 'GREEN',
                        'expiry-yellow': cell.expiryStatus === 'YELLOW',
                        'expiry-red': cell.expiryStatus === 'RED',
                      }"
                      :title="cell.sampleBarcode || ''"
                      @click="onCellClick(container, cell)"
                    >
                      <div
                        v-if="cell.filled && cell.sampleTypeIconUrl"
                        class="scheme-cell-icon-wrap"
                      >
                        <img
                          :src="cell.sampleTypeIconUrl"
                          class="scheme-cell-icon"
                          :alt="''"
                        />
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </div>
            <div class="shelf-divider"></div>
          </div>
        </div>

        <aside class="sample-drawer" :class="{ open: sampleDrawerOpen }">
          <div class="drawer-header">
            <div>
              <h3 class="drawer-title">
                {{
                  sampleDrawerMode === "details"
                    ? "Образец"
                    : sampleDrawerMode === "edit"
                    ? "Обновить образец"
                    : "Добавить образец"
                }}
              </h3>
              <p v-if="drawerContainer" class="drawer-subtitle">
                {{ containerPath(drawerContainer, drawerShelfLabel) }}
              </p>
              <p v-if="selectedCell" class="drawer-meta">
                Позиция: {{ selectedCell.label }}
              </p>
            </div>
            <div class="drawer-actions">
              <button
                v-if="sampleDrawerMode === 'details' && selectedSample"
                class="icon-button"
                type="button"
                aria-label="Обновить"
                title="Обновить"
                @click="openSampleEdit"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path
                    d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75L3 17.25z"
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button
                v-if="sampleDrawerMode === 'details' && selectedSample"
                class="icon-button danger"
                type="button"
                aria-label="Удалить"
                title="Удалить"
                @click="deleteSampleInDrawer"
              >
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path
                    d="M6 7h12l-1 14H7L6 7zm3-3h6l1 2H8l1-2z"
                    fill="currentColor"
                  />
                </svg>
              </button>
              <button class="btn btn-secondary" @click="closeSampleDrawer">
                Закрыть
              </button>
            </div>
          </div>

          <div v-if="sampleDrawerMode === 'details'" class="drawer-body">
            <div v-if="selectedSample" class="details-grid">
              <div
                v-for="field in sampleDetailFields"
                :key="field.key"
                class="detail-item"
              >
                <span>{{ field.label }}</span>
                <strong v-if="field.key !== 'barcode'">{{
                  formatSampleValue(selectedSample, field.key)
                }}</strong>
                <div v-else class="drawer-barcode-block">
                  <BarcodeSvg
                    v-if="getDisplayBarcode()"
                    :value="getDisplayBarcode()"
                  />
                  <div class="drawer-barcode-text">
                    {{ getDisplayBarcode() || "—" }}
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">Данные об образце не найдены.</div>
          </div>

          <form
            v-else
            class="drawer-body form-grid"
            @submit.prevent="submitSampleDrawer"
          >
            <div class="form-group">
              <label for="drawerVisitId">Визит (ID) *</label>
              <input
                id="drawerVisitId"
                v-model.number="drawerSampleForm.visitId"
                type="number"
                min="1"
                required
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label for="drawerBarcode">Штрихкод *</label>
              <input
                id="drawerBarcode"
                v-model="drawerSampleForm.barcode"
                type="text"
                required
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label for="drawerSampleType">Тип образца</label>
              <select
                id="drawerSampleType"
                v-model="drawerSampleForm.sampleTypeId"
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
              <label for="drawerInitialQty">Начальное кол-во *</label>
              <input
                id="drawerInitialQty"
                v-model.number="drawerSampleForm.initialQuantity"
                type="number"
                min="1"
                required
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label for="drawerCurrentQty">Текущее кол-во *</label>
              <input
                id="drawerCurrentQty"
                v-model.number="drawerSampleForm.currentQuantity"
                type="number"
                min="0"
                required
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label for="drawerRecommended">
                Рекомендуемый срок хранения (в месяцах)
              </label>
              <input
                id="drawerRecommended"
                v-model.number="drawerSampleForm.recommendedStorageMonths"
                type="number"
                min="0"
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label for="drawerCollectionDate">Дата забора образца</label>
              <input
                id="drawerCollectionDate"
                v-model="drawerSampleForm.collectionDate"
                type="datetime-local"
                class="form-control"
              />
            </div>
            <div class="form-group">
              <label>Фактический срок хранения (в месяцах)</label>
              <div class="readonly-field">{{ drawerActualMonthsPreview }}</div>
            </div>
            <div class="form-group">
              <label>Срок хранения (светофор)</label>
              <div class="readonly-field">{{ drawerExpiryPreview }}</div>
            </div>
            <div class="form-group">
              <label for="drawerContainer">Контейнер</label>
              <select
                id="drawerContainer"
                v-model="drawerSampleForm.containerId"
                class="form-control"
              >
                <option value="">Не указано</option>
                <option
                  v-for="container in containerRefs"
                  :key="container.containerId"
                  :value="container.containerId"
                >
                  {{ container.templateName || "Контейнер" }}
                  №{{ container.containerNumber ?? "—" }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>Аликвоты</label>
              <div v-if="drawerTubeCount > 0" class="tube-grid">
                <div
                  v-for="(specimen, index) in drawerSampleForm.specimens"
                  :key="index"
                  class="tube-row"
                >
                  <div class="tube-label">Аликвота {{ index + 1 }}</div>
                  <input
                    v-model="specimen.barcode"
                    type="text"
                    class="form-control"
                    placeholder="Штрихкод"
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
                    :disabled="!drawerSampleForm.containerId"
                  >
                    <option value="">Не указано</option>
                    <option
                      v-for="pos in drawerPositionsForSpecimen(
                        drawerSampleForm.containerId,
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
                Укажите количество аликвот.
              </div>
            </div>
            <div class="form-actions">
              <button type="submit" class="btn btn-primary">
                {{ sampleDrawerMode === "edit" ? "Обновить" : "Добавить" }}
              </button>
            </div>
          </form>
        </aside>
      </section>
    </div>

    <div v-if="modalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeModal">Закрыть</button>
        </div>

        <form class="form-grid" @submit.prevent="submitModal">
          <template v-if="modalType === 'location'">
            <div class="form-group">
              <label for="modalLocationName">Название *</label>
              <input
                id="modalLocationName"
                v-model="modalLocation.locationName"
                required
                type="text"
                class="form-control"
                placeholder="Основной склад"
              />
            </div>
            <div class="form-group">
              <label for="modalAddress">Адрес *</label>
              <input
                id="modalAddress"
                v-model="modalLocation.address"
                required
                type="text"
                class="form-control"
                placeholder="г. Иркутск, ул. Тимирязева, 16"
              />
            </div>
            <div class="form-group">
              <label for="modalRoom">Помещение *</label>
              <input
                id="modalRoom"
                v-model="modalLocation.roomNumber"
                required
                type="text"
                class="form-control"
                placeholder="Кабинет / Комната"
              />
            </div>
            <div class="form-group">
              <label for="modalDescription">Описание</label>
              <input
                id="modalDescription"
                v-model="modalLocation.description"
                type="text"
                class="form-control"
                placeholder="Доп. информация"
              />
            </div>
          </template>

          <template v-if="modalType === 'unit'">
            <div class="form-group with-action">
              <label for="modalUnitType">Тип хранилища *</label>
              <div class="input-action">
                <select
                  id="modalUnitType"
                  v-model.number="modalUnit.unitTypeId"
                  class="form-control"
                  required
                >
                  <option :value="null">— Выберите тип —</option>
                  <option
                    v-for="ut in unitTypes"
                    :key="ut.unitTypeId"
                    :value="ut.unitTypeId"
                  >
                    {{ ut.unitTypeName }}
                  </option>
                </select>
                <div class="icon-actions">
                  <button
                    type="button"
                    class="icon-button"
                    @click="openUnitTypeRefModal()"
                    aria-label="Добавить"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path
                        d="M11 5h2v14h-2zM5 11h14v2H5z"
                        fill="currentColor"
                      />
                    </svg>
                  </button>
                  <button
                    type="button"
                    class="icon-button"
                    :disabled="!modalUnit.unitTypeId"
                    @click="openUnitTypeRefModal(modalUnit.unitTypeId)"
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
                    :disabled="!modalUnit.unitTypeId"
                    @click="deleteUnitTypeQuick(modalUnit.unitTypeId)"
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
              <label for="modalUnitName">Название *</label>
              <input
                id="modalUnitName"
                v-model="modalUnit.unitName"
                required
                type="text"
                class="form-control"
                placeholder="Хранилище №1"
              />
            </div>
            <div class="form-group">
              <label for="modalShelves">Количество полок</label>
              <input
                id="modalShelves"
                v-model.number="modalUnit.shelvesCount"
                type="number"
                min="0"
                class="form-control"
                placeholder="Например, 5"
              />
            </div>
          </template>

          <template v-if="modalType === 'container'">
            <div class="form-group with-action">
              <label for="modalContainerTemplate">Шаблон контейнера *</label>
              <div class="input-action">
                <select
                  id="modalContainerTemplate"
                  v-model.number="modalContainer.templateId"
                  class="form-control"
                  required
                >
                  <option :value="null">— Выберите шаблон —</option>
                  <option
                    v-for="t in containerTemplates"
                    :key="t.templateId"
                    :value="t.templateId"
                  >
                    {{ t.templateName }} ({{ t.rowsCount }}×{{
                      t.columnsCount
                    }}, {{ numberingTypeLabel(t.numberingType) }})
                  </option>
                </select>
                <div class="icon-actions">
                  <button
                    type="button"
                    class="icon-button"
                    @click="openTemplateRefModal()"
                    aria-label="Добавить"
                  >
                    <svg viewBox="0 0 24 24" aria-hidden="true">
                      <path
                        d="M11 5h2v14h-2zM5 11h14v2H5z"
                        fill="currentColor"
                      />
                    </svg>
                  </button>
                  <button
                    type="button"
                    class="icon-button"
                    :disabled="!modalContainer.templateId"
                    @click="openTemplateRefModal(modalContainer.templateId)"
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
                    :disabled="!modalContainer.templateId"
                    @click="deleteTemplateQuick(modalContainer.templateId)"
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
              <label for="modalContainerNumber">Номер контейнера</label>
              <input
                id="modalContainerNumber"
                v-model="modalContainer.containerNumber"
                type="text"
                class="form-control"
                placeholder="A1, 1, Криобокс-1"
              />
            </div>
            <div class="form-group">
              <label for="modalShelf">Номер полки</label>
              <input
                id="modalShelf"
                v-model.number="modalContainer.shelfNumber"
                type="number"
                min="1"
                class="form-control"
                placeholder="1"
              />
            </div>
            <div v-if="modalMode === 'edit'" class="form-group">
              <label>Текущее кол-во образцов</label>
              <input
                :value="modalContainer.currentSamplesCount"
                type="number"
                class="form-control"
                readonly
              />
            </div>
          </template>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ modalMode === "edit" ? "Обновить" : "Добавить" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div
      v-if="unitTypeRefModalOpen"
      class="modal-overlay"
      @click.self="closeUnitTypeRefModal"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>{{ unitTypeRefModalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeUnitTypeRefModal">
            Закрыть
          </button>
        </div>
        <form class="form-grid" @submit.prevent="submitUnitTypeRefModal">
          <div class="form-group">
            <label for="unitTypeRefName">Название</label>
            <input
              id="unitTypeRefName"
              v-model="unitTypeRefModalName"
              type="text"
              class="form-control"
              placeholder="Введите название типа хранилища"
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ unitTypeRefModalMode === "edit" ? "Обновить" : "Добавить" }}
            </button>
          </div>
        </form>
      </div>
    </div>
    <div
      v-if="templateRefModalOpen"
      class="modal-overlay"
      @click.self="closeTemplateRefModal"
    >
      <div class="modal">
        <div class="modal-header">
          <h3>{{ templateRefModalTitle }}</h3>
          <button class="btn btn-secondary" @click="closeTemplateRefModal">
            Закрыть
          </button>
        </div>
        <form class="form-grid" @submit.prevent="submitTemplateRefModal">
          <div class="form-group">
            <label for="templateRefName">Название *</label>
            <input
              id="templateRefName"
              v-model="templateRefModalName"
              type="text"
              required
              class="form-control"
              placeholder="Введите название шаблона"
            />
          </div>
          <div class="form-group">
            <label for="templateRefRows">Строк</label>
            <input
              id="templateRefRows"
              v-model.number="templateRefModalRows"
              type="number"
              min="1"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="templateRefCols">Столбцов</label>
            <input
              id="templateRefCols"
              v-model.number="templateRefModalCols"
              type="number"
              min="1"
              class="form-control"
            />
          </div>
          <div class="form-group">
            <label for="templateRefNumbering">Тип нумерации</label>
            <select
              id="templateRefNumbering"
              v-model="templateRefModalNumbering"
              class="form-control"
            >
              <option value="LETTER_DIGIT">буква+цифра</option>
              <option value="DIGIT_LETTER">цифра+буква</option>
              <option value="DIGIT_DIGIT">цифра/цифра</option>
              <option value="SEQUENTIAL">сквозная</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary">
              {{ templateRefModalMode === "edit" ? "Обновить" : "Добавить" }}
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

interface StorageContainer {
  containerId: number;
  containerNumber: string | null;
  currentSamplesCount: number;
  unitId: number;
  shelfNumber: number | null;
  templateId: number;
  templateName: string;
  rowsCount: number;
  columnsCount: number;
  maxSamplesCount: number;
  numberingType: string;
}

interface ContainerTypeTemplate {
  templateId: number;
  templateName: string;
  rowsCount: number;
  columnsCount: number;
  maxSamplesCount: number;
  numberingType: string;
}

interface StorageUnit {
  unitId: number;
  locationId: number;
  unitTypeId: number;
  unitTypeName: string;
  unitName: string;
  shelvesCount: number | null;
  containers: StorageContainer[];
}

interface StorageLocation {
  locationId: number;
  locationName: string;
  address: string;
  roomNumber: string;
  description: string | null;
  units: StorageUnit[];
}

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
  initialQuantity: number | null;
  containerId: number | null;
  currentQuantity: number;
  recommendedStorageMonths: number | null;
  actualStorageMonths: number | null;
  expiryStatus: string;
  createdAtSample: string;
  specimens?: Specimen[];
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
  iconPath?: string | null;
}

interface SampleStatusRef {
  sampleStatusId: number;
  sampleStatusName: string;
}

interface NewLocationForm {
  locationName: string;
  address: string;
  roomNumber: string;
  description: string;
}

interface NewUnitForm {
  unitTypeId: number | null;
  unitName: string;
  shelvesCount: number | null;
}

interface NewContainerForm {
  templateId: number | null;
  containerNumber: string;
  shelfNumber: number | null;
  currentSamplesCount?: number;
}

export default defineComponent({
  name: "StorageView",
  components: {
    BarcodeSvg,
  },
  data() {
    return {
      locations: [] as StorageLocation[],
      samples: [] as Sample[],
      loading: false,
      successMessage: "",
      errorMessage: "",
      selectedLocationId: null as number | null,
      selectedUnitId: null as number | null,
      selectedShelfIndex: null as number | null,
      selectedContainerId: null as number | null,
      modalOpen: false,
      modalType: null as "location" | "unit" | "container" | null,
      modalMode: "create" as "create" | "edit",
      modalTitle: "",
      modalLocation: {
        locationName: "",
        address: "",
        roomNumber: "",
        description: "",
      } as NewLocationForm,
      modalUnit: {
        unitTypeId: null,
        unitName: "",
        shelvesCount: null,
      } as NewUnitForm,
      modalContainer: {
        templateId: null,
        containerNumber: "",
        shelfNumber: null,
      } as NewContainerForm,
      dragContainerId: null as number | null,
      dragSourceShelfIndex: null as number | null,
      containerRefs: [] as StorageContainer[],
      visits: [] as VisitRef[],
      patients: [] as PatientRef[],
      researches: [] as ResearchRef[],
      diagnoses: [] as DiagnosisRef[],
      sampleTypes: [] as SampleTypeRef[],
      sampleStatuses: [] as SampleStatusRef[],
      containerTemplates: [] as ContainerTypeTemplate[],
      unitTypes: [] as Array<{ unitTypeId: number; unitTypeName: string }>,
      unitTypeRefModalOpen: false,
      unitTypeRefModalTitle: "",
      unitTypeRefModalMode: "create" as "create" | "edit",
      unitTypeRefModalId: null as number | null,
      unitTypeRefModalName: "",
      templateRefModalOpen: false,
      templateRefModalTitle: "",
      templateRefModalMode: "create" as "create" | "edit",
      templateRefModalId: null as number | null,
      templateRefModalName: "",
      templateRefModalRows: 1,
      templateRefModalCols: 1,
      templateRefModalNumbering: "LETTER_DIGIT",
      sampleDrawerOpen: false,
      sampleDrawerMode: null as "details" | "create" | "edit" | null,
      drawerFormInitializing: false,
      selectedCell: null as { containerId: number; label: string } | null,
      selectedSample: null as Sample | null,
      selectedSpecimen: null as Specimen | null,
      drawerSampleForm: {
        visitId: null as number | null,
        barcode: "",
        sampleTypeId: null as number | null,
        initialQuantity: 1 as number | null,
        currentQuantity: 1 as number | null,
        recommendedStorageMonths: null as number | null,
        actualStorageMonths: null as number | null,
        expiryStatus: "",
        containerId: null as number | null,
        collectionDate: "",
        specimens: [] as Array<{
          barcode: string;
          sampleStatusId: number | null;
          containerId: number | null;
          positionInContainer: string;
        }>,
      },
    };
  },
  created() {
    this.fetchLocations();
    this.fetchReferenceData();
  },
  watch: {
    selectedLocationId() {
      this.selectedUnitId = null;
      this.selectedShelfIndex = null;
      this.selectedContainerId = null;
      this.closeSampleDrawer();
    },
    selectedUnitId() {
      this.selectedShelfIndex = null;
      this.selectedContainerId = null;
      this.closeSampleDrawer();
    },
    selectedShelfIndex() {
      this.selectedContainerId = null;
      this.closeSampleDrawer();
    },
    "drawerSampleForm.visitId": "syncDrawerCollectionFromVisit",
    "drawerSampleForm.containerId"(next: number | null, prev: number | null) {
      if (next === prev) {
        return;
      }
      if (this.drawerFormInitializing) {
        return;
      }
      if (
        this.sampleDrawerMode === "create" &&
        this.selectedCell &&
        next === this.selectedCell.containerId
      ) {
        this.drawerSampleForm.specimens = [
          {
            barcode: "",
            sampleStatusId: this.getStorageStatusId(),
            containerId: this.selectedCell.containerId,
            positionInContainer: this.selectedCell.label,
          },
        ];
        this.syncDrawerSpecimens();
        return;
      }
      this.drawerSampleForm.specimens = [];
      this.syncDrawerSpecimens();
    },
    "drawerSampleForm.initialQuantity"() {
      if (this.sampleDrawerMode === "create") {
        this.drawerSampleForm.currentQuantity =
          this.drawerSampleForm.initialQuantity;
      }
      this.syncDrawerSpecimens();
    },
    "drawerSampleForm.barcode"() {
      if (this.sampleDrawerMode === "create" && this.drawerTubeCount > 0) {
        this.syncDrawerSpecimens();
      }
    },
  },
  computed: {
    selectedLocation(): StorageLocation | null {
      if (this.selectedLocationId === null) {
        return null;
      }
      return (
        this.locations.find(
          (location) => location.locationId === this.selectedLocationId
        ) || null
      );
    },
    selectedUnit(): StorageUnit | null {
      if (!this.selectedLocation || this.selectedUnitId === null) {
        return null;
      }
      return (
        this.selectedLocation.units.find(
          (unit) => unit.unitId === this.selectedUnitId
        ) || null
      );
    },
    selectedContainer(): StorageContainer | null {
      if (!this.selectedUnit || this.selectedContainerId === null) {
        return null;
      }
      return (
        this.selectedUnit.containers.find(
          (container) => container.containerId === this.selectedContainerId
        ) || null
      );
    },
    unitShelves(): Array<{ index: number; label: string }> {
      if (!this.selectedUnit || !this.selectedUnit.shelvesCount) {
        return [];
      }
      return Array.from({ length: this.selectedUnit.shelvesCount }, (_, i) => ({
        index: i + 1,
        label: `Полка ${i + 1}`,
      }));
    },
    schemeShelves(): Array<{
      key: string;
      label: string;
      index: number | null;
      containers: StorageContainer[];
    }> {
      if (!this.selectedUnit) {
        return [];
      }
      const shelves: Array<{
        key: string;
        label: string;
        index: number | null;
        containers: StorageContainer[];
      }> = this.unitShelves.map((shelf) => ({
        key: `shelf-${shelf.index}`,
        label: shelf.label,
        index: shelf.index,
        containers: this.selectedUnit
          ? this.selectedUnit.containers.filter((container) => {
              const parsed = this.parseShelfNumber(container.shelfNumber);
              return parsed === shelf.index;
            })
          : [],
      }));
      const withoutShelf = this.selectedUnit.containers.filter((container) => {
        return this.parseShelfNumber(container.shelfNumber) === null;
      });
      if (withoutShelf.length > 0) {
        shelves.push({
          key: "shelf-unknown",
          label: "Полка не указана",
          index: null,
          containers: withoutShelf,
        });
      }
      return shelves;
    },
    filteredContainers(): StorageContainer[] {
      if (!this.selectedUnit || !this.selectedShelfIndex) {
        return [];
      }
      const targetIndex = this.selectedShelfIndex;
      return this.selectedUnit.containers.filter((container) => {
        const parsed = this.parseShelfNumber(container.shelfNumber);
        return parsed === targetIndex;
      });
    },
    drawerContainer(): StorageContainer | null {
      if (!this.selectedUnit || !this.selectedCell) {
        return null;
      }
      const container = this.selectedUnit.containers.find(
        (item) => item.containerId === this.selectedCell?.containerId
      );
      return container || null;
    },
    drawerShelfLabel(): string {
      if (!this.drawerContainer) {
        return "Полка не указана";
      }
      return this.drawerContainer.shelfNumber != null
        ? `Полка ${this.drawerContainer.shelfNumber}`
        : "Полка не указана";
    },
    drawerTubeCount(): number {
      const quantity = Number(this.drawerSampleForm.initialQuantity || 0);
      return Number.isFinite(quantity) && quantity > 0 ? quantity : 0;
    },
    drawerActualMonthsPreview(): string {
      return this.calculateActualMonths(this.drawerSampleForm.collectionDate);
    },
    drawerExpiryPreview(): string {
      return this.getExpiryStatusLabel(
        this.getExpiryStatusCode(
          this.drawerSampleForm.collectionDate,
          this.drawerSampleForm.recommendedStorageMonths
        )
      );
    },
    sampleDetailFields(): Array<{ key: string; label: string }> {
      return [
        { key: "barcode", label: "Штрихкод" },
        { key: "sampleTypeId", label: "Тип" },
        { key: "containerId", label: "Контейнер" },
        { key: "initialQuantity", label: "Начальное кол-во" },
        { key: "currentQuantity", label: "Текущее кол-во" },
        { key: "createdAtSample", label: "Дата забора" },
        { key: "actualStorageMonths", label: "Факт. хранение" },
        { key: "recommendedStorageMonths", label: "Рек. хранение" },
        { key: "expiryStatus", label: "Срок хранения" },
        { key: "patientAge", label: "Возраст" },
        { key: "mainDiagnosis", label: "Основной диагноз" },
        { key: "patientGender", label: "Пол" },
        { key: "patientBirthDate", label: "Дата рождения" },
        { key: "patientNationality", label: "Национальность" },
        { key: "researchInfo", label: "Исследование" },
        { key: "positionInContainer", label: "Позиция" },
        { key: "sampleStatusId", label: "Статус" },
      ];
    },
  },
  methods: {
    async fetchLocations() {
      this.loading = true;
      this.errorMessage = "";
      try {
        const [locationsResponse, samplesResponse] = await Promise.all([
          axios.get("/storage/locations"),
          axios.get("/samples"),
        ]);
        this.locations = locationsResponse.data;
        this.samples = samplesResponse.data;
        this.ensureSelections();
      } catch (error) {
        console.error("Ошибка при загрузке хранилищ:", error);
        this.errorMessage = "Не удалось загрузить хранилища";
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
          templatesResponse,
          unitTypesResponse,
          visitsResponse,
          patientsResponse,
          researchesResponse,
          diagnosesResponse,
        ] = await Promise.all([
          axios.get("/references/sample-types"),
          axios.get("/references/sample-statuses"),
          axios.get("/storage/containers"),
          axios.get("/references/container-templates"),
          axios.get("/references/unit-types"),
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
        this.containerTemplates = templatesResponse.data || [];
        this.unitTypes = unitTypesResponse.data || [];
        this.containerRefs = [...containersResponse.data].sort(
          (a: StorageContainer, b: StorageContainer) =>
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
    parseShelfNumber(value: string | number | null): number | null {
      if (value == null) {
        return null;
      }
      if (typeof value === "number") {
        return value;
      }
      const match = String(value).match(/\d+/);
      if (!match) {
        return null;
      }
      return Number(match[0]);
    },
    resolveContainerLayout(container: StorageContainer) {
      const total = container.maxSamplesCount || 0;
      const hasRows =
        typeof container.rowsCount === "number" && container.rowsCount > 0;
      const hasColumns =
        typeof container.columnsCount === "number" &&
        container.columnsCount > 0;
      if (hasRows && hasColumns) {
        return {
          rows: container.rowsCount as number,
          columns: container.columnsCount as number,
          total:
            (container.rowsCount as number) *
            (container.columnsCount as number),
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
    },
    getColumnLabel(index: number): string {
      let result = "";
      let current = index;
      while (current > 0) {
        const remainder = (current - 1) % 26;
        result = String.fromCharCode(65 + remainder) + result;
        current = Math.floor((current - 1) / 26);
      }
      return result;
    },
    getColumnHeaderLabel(container: StorageContainer, col: number): string {
      const t = container.numberingType || "LETTER_DIGIT";
      if (t === "SEQUENTIAL" || t === "DIGIT_DIGIT") return String(col);
      return this.getColumnLabel(col);
    },
    getRowHeaderLabel(container: StorageContainer, row: number): string {
      return String(row);
    },
    getPositionLabelForCell(
      container: StorageContainer,
      row: number,
      col: number,
      columns: number
    ): string {
      const numberingType = container.numberingType || "LETTER_DIGIT";
      const index0 = (row - 1) * columns + (col - 1);
      if (numberingType === "SEQUENTIAL") {
        return String(index0 + 1);
      }
      if (numberingType === "DIGIT_LETTER") {
        return `${row}${this.getColumnLabel(col)}`;
      }
      if (numberingType === "DIGIT_DIGIT") {
        return `${row}/${col}`;
      }
      return `${this.getColumnLabel(col)}${row}`;
    },
    getColumnIndex(label: string): number | null {
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
    },
    normalizePositionLabel(
      container: StorageContainer,
      value: string | null
    ): string | null {
      if (!value) {
        return null;
      }
      const { columns, total } = this.resolveContainerLayout(container);
      if (!columns || !total) {
        return null;
      }
      const trimmed = value.trim().toUpperCase();
      const letterMatch = trimmed.match(/^([A-Z]+)(\d+)$/);
      if (letterMatch) {
        const column = this.getColumnIndex(letterMatch[1]);
        const row = Number(letterMatch[2]);
        if (!column || row < 1) {
          return null;
        }
        const index = (row - 1) * columns + column;
        if (index < 1 || index > total) {
          return null;
        }
        return `${this.getColumnLabel(column)}${row}`;
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
      return `${this.getColumnLabel(column)}${row}`;
    },
    splitPositionValues(value: string | null): string[] {
      if (!value) {
        return [];
      }
      return value
        .split(/[,\n;]+/)
        .map((item) => item.trim())
        .filter((item) => item.length > 0);
    },
    getPositionLabels(
      container: StorageContainer,
      value: string | null
    ): string[] {
      const parts = this.splitPositionValues(value);
      const labels = parts
        .map((part) => this.normalizePositionLabel(container, part))
        .filter((label): label is string => !!label);
      return Array.from(new Set(labels));
    },
    getContainerSampleMap(container: StorageContainer) {
      const samples = this.getSamplesForContainer(container.containerId);
      const map = new Map<string, Sample>();
      samples.forEach((sample) => {
        (sample.specimens || []).forEach((a) => {
          if (
            a.containerId === container.containerId &&
            a.positionInContainer
          ) {
            const label = a.positionInContainer.trim();
            if (label && !map.has(label)) {
              map.set(label, sample);
            }
          }
        });
      });
      return map;
    },
    getContainerCellMap(container: StorageContainer) {
      const samples = this.getSamplesForContainer(container.containerId);
      const map = new Map<string, { sample: Sample; specimen: Specimen }>();
      samples.forEach((sample) => {
        (sample.specimens || []).forEach((a) => {
          if (
            a.containerId === container.containerId &&
            a.positionInContainer
          ) {
            const label = a.positionInContainer.trim();
            if (label && !map.has(label)) {
              map.set(label, { sample, specimen: a });
            }
          }
        });
      });
      return map;
    },
    containerGridColumns(container: StorageContainer): number {
      const { columns } = this.resolveContainerLayout(container);
      return columns || 1;
    },
    containerGridRows(container: StorageContainer): number {
      const { rows } = this.resolveContainerLayout(container);
      return rows || 1;
    },
    containerGridWidth(container: StorageContainer): string {
      const columns = this.containerGridColumns(container);
      const cellSize = 36;
      const gap = 8;
      const rowHeader = 24;
      const width = rowHeader + columns * cellSize + columns * gap;
      return `${width}px`;
    },
    containerRowCells(
      container: StorageContainer,
      row: number
    ): Array<{
      label: string;
      filled: boolean;
      sampleBarcode?: string;
      sampleTypeIconUrl?: string | null;
      expiryStatus?: string;
    }> {
      const { columns } = this.resolveContainerLayout(container);
      if (!columns) {
        return [];
      }
      const cellMap = this.getContainerCellMap(container);
      return Array.from({ length: columns }, (_, i) => {
        const column = i + 1;
        const label = this.getPositionLabelForCell(
          container,
          row,
          column,
          columns
        );
        const cellData = cellMap.get(label);
        return {
          label,
          filled: cellMap.has(label),
          sampleBarcode: cellData?.specimen.barcode ?? cellData?.sample.barcode,
          sampleTypeIconUrl: cellData?.sample
            ? this.getSampleTypeIconUrl(cellData.sample.sampleTypeId)
            : undefined,
          expiryStatus: cellData?.sample
            ? this.getSampleExpiryStatus(cellData.sample)
            : undefined,
        };
      });
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
      return remainingMonths <= 2 ? "YELLOW" : "GREEN";
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
    getStorageStatusId() {
      const target = this.sampleStatuses.find(
        (status) =>
          status.sampleStatusName?.trim().toLowerCase() === "в хранилище"
      );
      return target?.sampleStatusId ?? null;
    },
    getContainerLabel(containerId: number | null | undefined) {
      if (!containerId) return "—";
      const container = this.containerRefs.find(
        (item) => item.containerId === containerId
      );
      if (!container) return "—";
      const type = container.templateName || "Контейнер";
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
    formatDateOnly(value: string) {
      if (!value) return "—";
      return new Date(value).toLocaleDateString("ru-RU");
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
    parseIdList(value?: string | null): number[] {
      if (!value) return [];
      return value
        .split(",")
        .map((item) => Number(item.trim()))
        .filter((item) => Number.isFinite(item));
    },
    getDisplayBarcode(): string {
      if (this.selectedSpecimen?.barcode?.trim()) {
        return this.selectedSpecimen.barcode.trim();
      }
      return this.selectedSample?.barcode?.trim() || "";
    },
    formatSampleValue(sample: Sample, key: string) {
      const specimen = this.selectedSpecimen;
      if (key === "barcode" && specimen) {
        return specimen.barcode?.trim() || "—";
      }
      if (key === "positionInContainer" && specimen) {
        return specimen.positionInContainer?.trim() || "—";
      }
      if (key === "sampleStatusId" && specimen) {
        return this.getSampleStatusName(specimen.sampleStatusId);
      }
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
        const specimens = sample.specimens || [];
        const labels = specimens
          .map((a) => this.getSampleStatusName(a.sampleStatusId))
          .filter((l) => l && l !== "—");
        return labels.length > 0 ? labels.join("\n") : "—";
      }
      if (key === "actualStorageMonths") {
        return this.calculateActualMonths(sample.createdAtSample);
      }
      if (key === "createdAtSample") {
        return sample.createdAtSample
          ? this.formatDate(sample.createdAtSample)
          : "—";
      }
      if (key === "containerId") {
        return this.getContainerLabel(sample.containerId);
      }
      if (key === "positionInContainer") {
        const specimens = sample.specimens || [];
        const labels = specimens
          .map((a) => a.positionInContainer)
          .filter((p): p is string => !!p && p.trim() !== "");
        return labels.length > 0 ? labels.join("\n") : "—";
      }
      return value;
    },
    toDatetimeLocal(value: string) {
      if (!value) return "";
      return value.replace(" ", "T").slice(0, 16);
    },
    syncDrawerCollectionFromVisit() {
      if (
        this.sampleDrawerMode === "edit" &&
        this.drawerSampleForm.collectionDate
      ) {
        return;
      }
      const visit = this.visits.find(
        (item) => item.visitId === this.drawerSampleForm.visitId
      );
      if (!visit?.collectionDate) {
        return;
      }
      this.drawerSampleForm.collectionDate = this.toDatetimeLocal(
        visit.collectionDate
      );
    },
    syncDrawerSpecimens() {
      const quantity = this.drawerTubeCount;
      if (quantity <= 0) {
        this.drawerSampleForm.specimens = [];
        return;
      }
      const baseBarcode = this.drawerSampleForm.barcode || "";
      const current = [...(this.drawerSampleForm.specimens || [])];
      const result = [];
      for (let i = 0; i < quantity; i += 1) {
        if (i < current.length) {
          result.push(current[i]);
        } else {
          result.push({
            barcode: baseBarcode ? `${baseBarcode}-${i + 1}` : "",
            sampleStatusId: this.getStorageStatusId(),
            containerId:
              this.drawerSampleForm.containerId ??
              this.selectedCell?.containerId ??
              null,
            positionInContainer: this.selectedCell?.label ?? "",
          });
        }
      }
      this.drawerSampleForm.specimens = result;
    },
    drawerPositionsForSpecimen(
      containerId: number | null,
      currentIndex: number
    ): Array<{ value: string; disabled: boolean }> {
      if (!containerId) return [];
      const container = this.containerRefs.find(
        (c) => c.containerId === containerId
      );
      if (!container) return [];
      const layout = this.resolveContainerLayout(container);
      if (!layout.total || !layout.columns) return [];
      const occupied = new Set<string>();
      this.samples.forEach((s) => {
        if (
          this.sampleDrawerMode === "edit" &&
          s.sampleId === this.selectedSample?.sampleId
        )
          return;
        (s.specimens || []).forEach((a) => {
          if (a.containerId === containerId && a.positionInContainer)
            occupied.add(a.positionInContainer);
        });
      });
      (this.drawerSampleForm.specimens || []).forEach((a, idx) => {
        if (
          idx !== currentIndex &&
          a.containerId === containerId &&
          a.positionInContainer
        ) {
          occupied.add(a.positionInContainer);
        }
      });
      return Array.from({ length: layout.total }, (_, i) => {
        const index = i + 1;
        const row = Math.floor((index - 1) / layout.columns) + 1;
        const column = ((index - 1) % layout.columns) + 1;
        const value = this.getPositionLabelForCell(
          container,
          row,
          column,
          layout.columns
        );
        return { value, disabled: occupied.has(value) };
      });
    },
    onCellClick(
      container: StorageContainer,
      cell: { label: string; filled: boolean; sampleBarcode?: string }
    ) {
      this.selectedContainerId = container.containerId;
      if (cell.filled) {
        const sample = this.findSampleForCell(container, cell.label);
        this.openSampleDetails(container.containerId, cell.label, sample);
      } else {
        this.openSampleCreate(container.containerId, cell.label);
      }
    },
    findSampleForCell(
      container: StorageContainer,
      label: string
    ): Sample | null {
      const samples = this.getSamplesForContainer(container.containerId);
      return (
        samples.find((sample) =>
          (sample.specimens || []).some(
            (a) =>
              a.containerId === container.containerId &&
              a.positionInContainer?.trim() === label
          )
        ) || null
      );
    },
    findSpecimenForCell(
      container: StorageContainer,
      label: string
    ): Specimen | null {
      const samples = this.getSamplesForContainer(container.containerId);
      for (const sample of samples) {
        const specimen = (sample.specimens || []).find(
          (a) =>
            a.containerId === container.containerId &&
            a.positionInContainer?.trim() === label
        );
        if (specimen) return specimen;
      }
      return null;
    },
    openSampleDetails(
      containerId: number,
      label: string,
      sample: Sample | null
    ) {
      this.sampleDrawerOpen = true;
      this.sampleDrawerMode = "details";
      this.selectedCell = { containerId, label };
      this.selectedSample = sample;
      const container = this.containerRefs.find(
        (c) => c.containerId === containerId
      );
      this.selectedSpecimen =
        container && sample ? this.findSpecimenForCell(container, label) : null;
      if (!sample) {
        this.errorMessage = "Образец для позиции не найден.";
      }
    },
    openSampleCreate(containerId: number, label: string) {
      this.sampleDrawerOpen = true;
      this.sampleDrawerMode = "create";
      this.selectedCell = { containerId, label };
      this.selectedSample = null;
      this.selectedSpecimen = null;
      const storageStatusId = this.getStorageStatusId();
      this.drawerSampleForm = {
        visitId: null,
        barcode: "",
        sampleTypeId: null,
        initialQuantity: 1,
        currentQuantity: 1,
        recommendedStorageMonths: null,
        actualStorageMonths: null,
        expiryStatus: "",
        containerId: containerId,
        collectionDate: "",
        specimens: [
          {
            barcode: "",
            sampleStatusId: storageStatusId,
            containerId: containerId,
            positionInContainer: label,
          },
        ],
      };
      this.syncDrawerSpecimens();
    },
    openSampleEdit() {
      if (!this.selectedSample) {
        return;
      }
      this.drawerFormInitializing = true;
      this.sampleDrawerMode = "edit";
      const specimens = this.selectedSample.specimens || [];
      this.drawerSampleForm = {
        visitId: this.selectedSample.visitId,
        barcode: this.selectedSample.barcode,
        sampleTypeId: this.selectedSample.sampleTypeId,
        initialQuantity: this.selectedSample.initialQuantity,
        currentQuantity: this.selectedSample.currentQuantity,
        recommendedStorageMonths: this.selectedSample.recommendedStorageMonths,
        actualStorageMonths: this.selectedSample.actualStorageMonths,
        expiryStatus: this.selectedSample.expiryStatus || "",
        containerId: this.selectedSample.containerId,
        collectionDate: this.toDatetimeLocal(
          this.selectedSample.createdAtSample
        ),
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
      this.syncDrawerSpecimens();
      this.$nextTick(() => {
        this.drawerFormInitializing = false;
      });
    },
    closeSampleDrawer() {
      this.sampleDrawerOpen = false;
      this.sampleDrawerMode = null;
      this.selectedCell = null;
      this.selectedSample = null;
      this.selectedSpecimen = null;
    },
    async submitSampleDrawer() {
      this.errorMessage = "";
      this.successMessage = "";
      try {
        if (this.sampleDrawerMode === "edit" && this.selectedSample) {
          await this.updateSampleInDrawer();
        } else {
          await this.createSampleInDrawer();
        }
      } catch (error: unknown) {
        console.error("Ошибка при сохранении образца:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при сохранении образца. Проверьте поля."
        );
      }
    },
    async createSampleInDrawer() {
      const quantity = Number(this.drawerSampleForm.initialQuantity || 0);
      if (!this.drawerSampleForm.visitId || !this.drawerSampleForm.barcode) {
        this.errorMessage = "Заполните обязательные поля.";
        return;
      }
      const specimens = this.drawerSampleForm.specimens || [];
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
      const response = await axios.post("/samples", this.serializeSampleForm());
      this.successMessage = "Образец добавлен";
      await this.fetchLocations();
      const created = response.data as Sample;
      const refreshed =
        this.samples.find((sample) => sample.sampleId === created.sampleId) ||
        created;
      const firstSpecimen = this.drawerSampleForm.specimens?.[0];
      const containerId =
        firstSpecimen?.containerId ??
        this.drawerSampleForm.containerId ??
        this.selectedCell?.containerId ??
        0;
      const label =
        firstSpecimen?.positionInContainer || this.selectedCell?.label || "";
      this.openSampleDetails(containerId, label, refreshed);
    },
    async updateSampleInDrawer() {
      if (!this.selectedSample) {
        return;
      }
      const quantity = Number(this.drawerSampleForm.initialQuantity || 0);
      if (!this.drawerSampleForm.visitId || !this.drawerSampleForm.barcode) {
        this.errorMessage = "Заполните обязательные поля.";
        return;
      }
      const specimens = this.drawerSampleForm.specimens || [];
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
      await axios.put(
        `/samples/${this.selectedSample.sampleId}`,
        this.serializeSampleForm()
      );
      this.successMessage = "Образец обновлен";
      await this.fetchLocations();
      const refreshed =
        this.samples.find(
          (sample) => sample.sampleId === this.selectedSample?.sampleId
        ) || null;
      const firstSpecimen = this.drawerSampleForm.specimens?.[0];
      const containerId =
        this.drawerSampleForm.containerId ??
        this.selectedCell?.containerId ??
        firstSpecimen?.containerId ??
        null;
      if (refreshed && containerId && firstSpecimen?.positionInContainer) {
        this.openSampleDetails(
          containerId,
          firstSpecimen.positionInContainer,
          refreshed
        );
      }
    },
    serializeSampleForm() {
      const sampleContainerId =
        this.drawerSampleForm.containerId ??
        this.selectedCell?.containerId ??
        null;
      const specimens = (this.drawerSampleForm.specimens || []).map((a) => ({
        barcode: a.barcode.trim(),
        sampleStatusId: a.sampleStatusId,
        containerId: sampleContainerId,
        positionInContainer: a.positionInContainer || null,
      }));
      return {
        visitId: this.toNullableNumber(this.drawerSampleForm.visitId),
        barcode: this.drawerSampleForm.barcode,
        sampleTypeId: this.toNullableNumber(this.drawerSampleForm.sampleTypeId),
        initialQuantity: this.toNullableNumber(
          this.drawerSampleForm.initialQuantity
        ),
        currentQuantity: this.toNullableNumber(
          this.drawerSampleForm.currentQuantity
        ),
        recommendedStorageMonths: this.toNullableNumber(
          this.drawerSampleForm.recommendedStorageMonths
        ),
        containerId: this.toNullableNumber(this.drawerSampleForm.containerId),
        collectionDate: this.drawerSampleForm.collectionDate || null,
        specimens,
      };
    },
    async deleteSampleInDrawer() {
      if (!this.selectedSample) {
        return;
      }
      const confirmed = window.confirm("Удалить выбранный образец?");
      if (!confirmed) {
        return;
      }
      try {
        await axios.delete(`/samples/${this.selectedSample.sampleId}`);
        this.successMessage = "Образец удален";
        await this.fetchLocations();
        this.closeSampleDrawer();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении образца."
        );
      }
    },
    numberingTypeLabel(type: string | null): string {
      if (!type) return "—";
      const map: Record<string, string> = {
        LETTER_DIGIT: "буква+цифра",
        DIGIT_LETTER: "цифра+буква",
        DIGIT_DIGIT: "цифра/цифра",
        SEQUENTIAL: "сквозная",
      };
      return map[type] || type;
    },
    getSamplesForContainer(containerId: number): Sample[] {
      return this.samples.filter((sample) => {
        if (sample.containerId === containerId) return true;
        return (sample.specimens || []).some(
          (a) => a.containerId === containerId
        );
      });
    },
    getContainersForShelf(shelfIndex: number) {
      if (!this.selectedUnit) {
        return [];
      }
      return this.selectedUnit.containers
        .filter((container) => {
          const parsed = this.parseShelfNumber(container.shelfNumber);
          return parsed === shelfIndex;
        })
        .sort((a, b) => {
          const aVal = a.containerNumber ?? "";
          const bVal = b.containerNumber ?? "";
          return String(aVal).localeCompare(String(bVal), undefined, {
            numeric: true,
          });
        });
    },
    ensureSelections() {
      if (this.selectedLocationId) {
        const exists = this.locations.some(
          (location) => location.locationId === this.selectedLocationId
        );
        if (!exists) {
          this.selectedLocationId = null;
          this.selectedUnitId = null;
          this.selectedShelfIndex = null;
          this.selectedContainerId = null;
        }
      }
      if (this.selectedLocationId && this.selectedUnitId) {
        const location = this.locations.find(
          (item) => item.locationId === this.selectedLocationId
        );
        const unitExists =
          location?.units.some((unit) => unit.unitId === this.selectedUnitId) ||
          false;
        if (!unitExists) {
          this.selectedUnitId = null;
          this.selectedShelfIndex = null;
          this.selectedContainerId = null;
        }
      }
      if (this.selectedUnitId && this.selectedShelfIndex) {
        const exists = this.unitShelves.some(
          (shelf) => shelf.index === this.selectedShelfIndex
        );
        if (!exists) {
          this.selectedShelfIndex = null;
          this.selectedContainerId = null;
        }
      }
      if (this.selectedUnitId && this.selectedContainerId) {
        const unit = this.selectedLocation?.units.find(
          (item) => item.unitId === this.selectedUnitId
        );
        const containerExists =
          unit?.containers.some(
            (container) => container.containerId === this.selectedContainerId
          ) || false;
        if (!containerExists) {
          this.selectedContainerId = null;
        }
      }
    },
    selectLocation(locationId: number) {
      this.selectedLocationId =
        this.selectedLocationId === locationId ? null : locationId;
    },
    selectUnit(unitId: number) {
      this.selectedUnitId = this.selectedUnitId === unitId ? null : unitId;
    },
    selectShelf(index: number) {
      this.selectedShelfIndex =
        this.selectedShelfIndex === index ? null : index;
    },
    selectContainer(containerId: number) {
      this.selectedContainerId =
        this.selectedContainerId === containerId ? null : containerId;
    },
    onDragStart(containerId: number, shelfIndex: number | null) {
      if (!shelfIndex) {
        return;
      }
      this.dragContainerId = containerId;
      this.dragSourceShelfIndex = shelfIndex;
    },
    onDragOver(event: DragEvent) {
      event.preventDefault();
    },
    async onDropOnShelf(targetShelfIndex: number) {
      if (!this.dragContainerId || !this.selectedUnit) {
        return;
      }
      await this.moveContainer(this.dragContainerId, targetShelfIndex, null);
    },
    async onDropOnContainer(
      targetShelfIndex: number,
      targetContainerId: number
    ) {
      if (!this.dragContainerId || !this.selectedUnit) {
        return;
      }
      await this.moveContainer(
        this.dragContainerId,
        targetShelfIndex,
        targetContainerId
      );
    },
    async moveContainer(
      containerId: number,
      targetShelfIndex: number,
      beforeContainerId: number | null
    ) {
      if (!this.selectedUnit) {
        return;
      }
      const sourceShelf = this.dragSourceShelfIndex;
      if (!sourceShelf) {
        return;
      }
      const sourceList = this.getContainersForShelf(sourceShelf).filter(
        (container) => container.containerId !== containerId
      );
      const targetList = this.getContainersForShelf(targetShelfIndex).filter(
        (container) => container.containerId !== containerId
      );
      const moved = this.selectedUnit.containers.find(
        (container) => container.containerId === containerId
      );
      if (!moved) {
        return;
      }
      if (beforeContainerId) {
        const insertIndex = targetList.findIndex(
          (container) => container.containerId === beforeContainerId
        );
        if (insertIndex >= 0) {
          targetList.splice(insertIndex, 0, moved);
        } else {
          targetList.push(moved);
        }
      } else {
        targetList.push(moved);
      }
      const updates: StorageContainer[] = [];
      const applyNumbers = (
        list: StorageContainer[],
        shelfIndexValue: number
      ) => {
        list.forEach((container, index) => {
          const nextShelf = shelfIndexValue;
          const nextNumber = String(index + 1);
          if (
            container.shelfNumber !== nextShelf ||
            (container.containerNumber ?? "") !== nextNumber
          ) {
            container.shelfNumber = nextShelf;
            container.containerNumber = nextNumber;
            updates.push(container);
          }
        });
      };
      applyNumbers(sourceList, sourceShelf);
      applyNumbers(targetList, targetShelfIndex);
      await this.persistContainerUpdates(updates);
      await this.fetchLocations();
      this.dragContainerId = null;
      this.dragSourceShelfIndex = null;
      this.selectedShelfIndex = targetShelfIndex;
      this.selectedContainerId = containerId;
    },
    async persistContainerUpdates(containers: StorageContainer[]) {
      for (const container of containers) {
        await axios.put(`/storage/containers/${container.containerId}`, {
          templateId: container.templateId,
          containerNumber: container.containerNumber || null,
          shelfNumber: container.shelfNumber,
        });
      }
    },
    openLocationModal() {
      this.modalType = "location";
      this.modalMode = "create";
      this.modalTitle = "Добавить локацию";
      this.modalLocation = {
        locationName: "",
        address: "",
        roomNumber: "",
        description: "",
      };
      this.modalOpen = true;
    },
    openUnitModal() {
      if (!this.selectedLocationId) {
        this.errorMessage = "Сначала выберите локацию";
        return;
      }
      this.modalType = "unit";
      this.modalMode = "create";
      this.modalTitle = "Добавить хранилище";
      this.modalUnit = {
        unitTypeId: null,
        unitName: "",
        shelvesCount: null,
      };
      this.modalOpen = true;
    },
    openContainerModal() {
      if (!this.selectedUnitId) {
        this.errorMessage = "Сначала выберите хранилище";
        return;
      }
      const shelfNum = this.selectedShelfIndex ?? null;
      this.modalType = "container";
      this.modalMode = "create";
      this.modalTitle = "Добавить контейнер";
      this.modalContainer = {
        templateId: null,
        containerNumber: "",
        shelfNumber: shelfNum,
      };
      this.modalOpen = true;
    },
    closeModal() {
      this.modalOpen = false;
      this.modalType = null;
      this.modalTitle = "";
    },
    openUnitTypeRefModal(presetId: number | null = null) {
      this.unitTypeRefModalId = presetId;
      this.unitTypeRefModalName = "";
      this.unitTypeRefModalMode = presetId ? "edit" : "create";
      this.unitTypeRefModalTitle =
        this.unitTypeRefModalMode === "edit"
          ? "Обновить тип хранилища"
          : "Добавить тип хранилища";
      if (this.unitTypeRefModalId) {
        const item = this.unitTypes.find(
          (ut) => ut.unitTypeId === this.unitTypeRefModalId
        );
        this.unitTypeRefModalName = item?.unitTypeName || "";
      }
      this.unitTypeRefModalOpen = true;
    },
    closeUnitTypeRefModal() {
      this.unitTypeRefModalOpen = false;
    },
    async submitUnitTypeRefModal() {
      if (!this.unitTypeRefModalName.trim()) {
        return;
      }
      if (this.unitTypeRefModalMode === "edit") {
        await this.updateUnitTypeRefModal();
        return;
      }
      try {
        const response = await axios.post("/references/unit-types", {
          name: this.unitTypeRefModalName,
        });
        const created = response.data as { unitTypeId: number };
        if (created?.unitTypeId) {
          this.modalUnit.unitTypeId = created.unitTypeId;
        }
        await this.fetchReferenceData();
        this.closeUnitTypeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении типа хранилища";
      }
    },
    async updateUnitTypeRefModal() {
      if (!this.unitTypeRefModalId || !this.unitTypeRefModalName.trim()) {
        return;
      }
      try {
        await axios.put(`/references/unit-types/${this.unitTypeRefModalId}`, {
          name: this.unitTypeRefModalName,
        });
        await this.fetchReferenceData();
        this.closeUnitTypeRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении типа хранилища";
      }
    },
    async deleteUnitTypeQuick(id: number | null) {
      if (!id) return;
      const confirmed = window.confirm("Удалить выбранный тип хранилища?");
      if (!confirmed) return;
      try {
        await axios.delete(`/references/unit-types/${id}`);
        if (this.modalUnit.unitTypeId === id) {
          this.modalUnit.unitTypeId = null;
        }
        await this.fetchReferenceData();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении типа хранилища";
      }
    },
    openTemplateRefModal(presetId: number | null = null) {
      this.templateRefModalId = presetId;
      this.templateRefModalName = "";
      this.templateRefModalRows = 1;
      this.templateRefModalCols = 1;
      this.templateRefModalNumbering = "LETTER_DIGIT";
      this.templateRefModalMode = presetId ? "edit" : "create";
      this.templateRefModalTitle =
        this.templateRefModalMode === "edit"
          ? "Обновить шаблон контейнера"
          : "Добавить шаблон контейнера";
      if (this.templateRefModalId) {
        const t = this.containerTemplates.find(
          (c) => c.templateId === this.templateRefModalId
        );
        if (t) {
          this.templateRefModalName = t.templateName;
          this.templateRefModalRows = t.rowsCount;
          this.templateRefModalCols = t.columnsCount;
          this.templateRefModalNumbering = t.numberingType || "LETTER_DIGIT";
        }
      }
      this.templateRefModalOpen = true;
    },
    closeTemplateRefModal() {
      this.templateRefModalOpen = false;
    },
    async submitTemplateRefModal() {
      if (!this.templateRefModalName.trim()) return;
      if (this.templateRefModalMode === "edit") {
        await this.updateTemplateRefModal();
        return;
      }
      try {
        const response = await axios.post("/references/container-templates", {
          templateName: this.templateRefModalName,
          rowsCount: this.templateRefModalRows || 1,
          columnsCount: this.templateRefModalCols || 1,
          numberingType: this.templateRefModalNumbering,
        });
        const created = response.data as { templateId: number };
        if (created?.templateId) {
          this.modalContainer.templateId = created.templateId;
        }
        await this.fetchReferenceData();
        this.closeTemplateRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при добавлении шаблона контейнера";
      }
    },
    async updateTemplateRefModal() {
      if (!this.templateRefModalId || !this.templateRefModalName.trim()) return;
      try {
        await axios.put(
          `/references/container-templates/${this.templateRefModalId}`,
          {
            templateName: this.templateRefModalName,
            rowsCount: this.templateRefModalRows || 1,
            columnsCount: this.templateRefModalCols || 1,
            numberingType: this.templateRefModalNumbering,
          }
        );
        await this.fetchReferenceData();
        this.closeTemplateRefModal();
      } catch (error) {
        this.errorMessage = "Ошибка при обновлении шаблона контейнера";
      }
    },
    async deleteTemplateQuick(id: number | null) {
      if (!id) return;
      const confirmed = window.confirm("Удалить выбранный шаблон контейнера?");
      if (!confirmed) return;
      try {
        await axios.delete(`/references/container-templates/${id}`);
        if (this.modalContainer.templateId === id) {
          this.modalContainer.templateId = null;
        }
        await this.fetchReferenceData();
      } catch (error) {
        this.errorMessage = "Ошибка при удалении шаблона контейнера";
      }
    },
    async submitModal() {
      if (this.modalType === "location" && this.modalMode === "create") {
        await this.addLocation();
      }
      if (this.modalType === "unit" && this.modalMode === "create") {
        await this.addUnit();
      }
      if (this.modalType === "container" && this.modalMode === "create") {
        await this.addContainer();
      }
      if (this.modalType === "location" && this.modalMode === "edit") {
        await this.updateLocation();
      }
      if (this.modalType === "unit" && this.modalMode === "edit") {
        await this.updateUnit();
      }
      if (this.modalType === "container" && this.modalMode === "edit") {
        await this.updateContainer();
      }
    },
    async addLocation() {
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.post("/storage/locations", this.modalLocation);
        this.successMessage = "Локация успешно добавлена";
        this.closeModal();
        await this.fetchLocations();
      } catch (error: unknown) {
        console.error("Ошибка при добавлении локации:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении локации. Проверьте поля."
        );
      }
    },
    async addUnit() {
      this.successMessage = "";
      this.errorMessage = "";
      const locationId = this.selectedLocationId;
      if (!locationId) {
        this.errorMessage = "Сначала выберите локацию";
        return;
      }
      const payload = {
        unitTypeId: this.modalUnit.unitTypeId,
        unitName: this.modalUnit.unitName,
        shelvesCount: this.modalUnit.shelvesCount,
      };
      try {
        await axios.post(`/storage/locations/${locationId}/units`, payload);
        this.successMessage = "Хранилище успешно добавлено";
        this.closeModal();
        await this.fetchLocations();
        this.selectedLocationId = locationId;
      } catch (error: unknown) {
        console.error("Ошибка при добавлении хранилища:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении хранилища. Проверьте поля."
        );
      }
    },
    async addContainer() {
      this.successMessage = "";
      this.errorMessage = "";
      const unitId = this.selectedUnitId;
      if (!unitId) {
        this.errorMessage = "Сначала выберите хранилище";
        return;
      }
      const payload = {
        templateId: this.modalContainer.templateId,
        containerNumber: this.modalContainer.containerNumber || null,
        shelfNumber: this.modalContainer.shelfNumber,
      };
      try {
        await axios.post(`/storage/units/${unitId}/containers`, payload);
        this.successMessage = "Контейнер успешно добавлен";
        this.closeModal();
        await this.fetchLocations();
        this.selectedUnitId = unitId;
      } catch (error: unknown) {
        console.error("Ошибка при добавлении контейнера:", error);
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при добавлении контейнера. Проверьте поля."
        );
      }
    },
    openEditLocationModal() {
      if (!this.selectedLocation) {
        this.errorMessage = "Сначала выберите локацию";
        return;
      }
      this.modalType = "location";
      this.modalMode = "edit";
      this.modalTitle = "Обновить локацию";
      this.modalLocation = {
        locationName: this.selectedLocation.locationName,
        address: this.selectedLocation.address,
        roomNumber: this.selectedLocation.roomNumber,
        description: this.selectedLocation.description || "",
      };
      this.modalOpen = true;
    },
    openEditUnitModal() {
      if (!this.selectedUnit) {
        this.errorMessage = "Сначала выберите хранилище";
        return;
      }
      this.modalType = "unit";
      this.modalMode = "edit";
      this.modalTitle = "Обновить хранилище";
      this.modalUnit = {
        unitTypeId: this.selectedUnit.unitTypeId,
        unitName: this.selectedUnit.unitName,
        shelvesCount: this.selectedUnit.shelvesCount,
      };
      this.modalOpen = true;
    },
    openEditContainerModal() {
      if (!this.selectedContainer) {
        this.errorMessage = "Сначала выберите контейнер";
        return;
      }
      this.modalType = "container";
      this.modalMode = "edit";
      this.modalTitle = "Обновить контейнер";
      this.modalContainer = {
        templateId: this.selectedContainer.templateId,
        containerNumber: this.selectedContainer.containerNumber ?? "",
        shelfNumber: this.selectedContainer.shelfNumber,
        currentSamplesCount: this.selectedContainer.currentSamplesCount,
      };
      this.modalOpen = true;
    },
    async updateLocation() {
      if (!this.selectedLocationId) {
        return;
      }
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.put(
          `/storage/locations/${this.selectedLocationId}`,
          this.modalLocation
        );
        this.successMessage = "Локация обновлена";
        this.closeModal();
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении локации."
        );
      }
    },
    async updateUnit() {
      if (!this.selectedUnitId) {
        return;
      }
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.put(`/storage/units/${this.selectedUnitId}`, {
          unitTypeId: this.modalUnit.unitTypeId,
          unitName: this.modalUnit.unitName,
          shelvesCount: this.modalUnit.shelvesCount,
        });
        this.successMessage = "Хранилище обновлено";
        this.closeModal();
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении хранилища."
        );
      }
    },
    async updateContainer() {
      if (!this.selectedContainerId) {
        return;
      }
      this.successMessage = "";
      this.errorMessage = "";
      try {
        await axios.put(`/storage/containers/${this.selectedContainerId}`, {
          templateId: this.modalContainer.templateId,
          containerNumber: this.modalContainer.containerNumber || null,
          shelfNumber: this.modalContainer.shelfNumber,
        });
        this.successMessage = "Контейнер обновлен";
        this.closeModal();
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при обновлении контейнера."
        );
      }
    },
    containerPath(container: StorageContainer, shelfLabel: string): string {
      if (!this.selectedLocation || !this.selectedUnit) {
        return "";
      }
      const containerName = container.templateName || "Контейнер";
      const number = container.containerNumber ?? "—";
      return `${this.selectedLocation.locationName} → ${
        this.selectedUnit.unitName
      } → ${shelfLabel} → ${containerName} ${number ? `№${number}` : ""}`;
    },
    async deleteLocation() {
      if (!this.selectedLocationId) {
        return;
      }
      const shouldDelete = window.confirm(
        "Удалить выбранную локацию и все хранилища внутри?"
      );
      if (!shouldDelete) {
        return;
      }
      try {
        await axios.delete(`/storage/locations/${this.selectedLocationId}`);
        this.successMessage = "Локация удалена";
        this.selectedLocationId = null;
        this.selectedUnitId = null;
        this.selectedShelfIndex = null;
        this.selectedContainerId = null;
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении локации."
        );
      }
    },
    async deleteUnit() {
      if (!this.selectedUnitId) {
        return;
      }
      const shouldDelete = window.confirm(
        "Удалить выбранное хранилище и контейнеры?"
      );
      if (!shouldDelete) {
        return;
      }
      try {
        await axios.delete(`/storage/units/${this.selectedUnitId}`);
        this.successMessage = "Хранилище удалено";
        this.selectedUnitId = null;
        this.selectedShelfIndex = null;
        this.selectedContainerId = null;
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении хранилища."
        );
      }
    },
    async deleteContainer() {
      if (!this.selectedContainerId) {
        return;
      }
      const shouldDelete = window.confirm("Удалить выбранный контейнер?");
      if (!shouldDelete) {
        return;
      }
      try {
        await axios.delete(`/storage/containers/${this.selectedContainerId}`);
        this.successMessage = "Контейнер удален";
        this.selectedContainerId = null;
        await this.fetchLocations();
      } catch (error: unknown) {
        this.errorMessage = this.resolveErrorMessage(
          error,
          "Ошибка при удалении контейнера."
        );
      }
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
    toNullableNumber(value: number | null) {
      return value === null ? null : value;
    },
  },
});
</script>

<style scoped>
.storage-page {
  max-width: 98%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

h2 {
  text-align: center;
  color: var(--text-primary);
}

.storage-layout {
  display: grid;
  grid-template-columns: minmax(240px, 0.7fr) minmax(360px, 1.8fr);
  gap: 14px;
  align-items: stretch;
}

.card {
  background-color: var(--surface);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
}

.left-panel,
.right-panel {
  height: 100%;
  position: relative;
  overflow: hidden;
}

.left-panel {
  overflow: auto;
  border-radius: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.action-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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

.tree {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tree-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tree-children {
  margin-left: 16px;
  padding-left: 12px;
  border-left: 1px dashed var(--border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tree-button {
  text-align: left;
  background: #e2d6c8;
  border: 1px solid var(--border);
  padding: 8px 12px;
  border-radius: 8px;
  color: var(--text-primary);
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

.tree-button:hover {
  background-color: #cfc1ad;
}

.tree-button.active {
  background-color: #6e5a4b;
  color: #f2ede6;
  border-color: #6e5a4b;
  box-shadow: var(--shadow);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
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

.form-actions {
  display: flex;
  align-items: flex-end;
}

.location-title {
  font-weight: 700;
  color: var(--text-primary);
}

.location-subtitle,
.location-description {
  color: var(--text-secondary);
  margin-top: 4px;
}

.location-meta {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.unit-title {
  font-weight: 600;
  color: var(--text-primary);
}

.unit-subtitle {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.scheme {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.shelf-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shelf-title {
  font-weight: 600;
  color: var(--text-primary);
}

.shelf-containers {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.shelf-divider {
  height: 1px;
  background: var(--border);
}

.container-scheme {
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px;
  background: #e9dfd2;
  display: inline-flex;
  flex-direction: column;
  gap: 10px;
  width: max-content;
  align-self: flex-start;
}

.container-scheme.selected {
  border-color: var(--accent);
  box-shadow: var(--shadow);
}

.container-scheme:active {
  cursor: grabbing;
}

.container-path {
  background-color: #e2d6c8;
  border-radius: 8px;
  padding: 8px 10px;
  border: 1px solid var(--border);
  color: var(--text-secondary);
  font-size: 0.9rem;
  width: var(--grid-width);
  max-width: var(--grid-width);
  white-space: normal;
}

.scheme-grid {
  display: grid;
  --cell-size: 36px;
  --cell-label-height: 14px;
  gap: 8px;
  align-items: center;
  width: var(--grid-width);
}

.scheme-grid-sequential {
  align-items: stretch;
}

.scheme-grid-corner {
  width: 24px;
  height: 24px;
}

.scheme-grid-header {
  font-size: 0.75rem;
  color: var(--text-secondary);
  text-align: center;
  justify-self: center;
}

.scheme-grid-header.row-header {
  text-align: right;
  padding-right: 6px;
  justify-self: end;
}

.scheme-cell-sequential-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.scheme-cell-label-above {
  font-size: 0.7rem;
  line-height: 1;
  color: var(--text-secondary);
  min-height: var(--cell-label-height);
  display: flex;
  align-items: center;
  justify-content: center;
}

.scheme-cell {
  width: var(--cell-size);
  height: var(--cell-size);
  border-radius: 50%;
  border: 1px solid var(--border);
  background: #e2d6c8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.scheme-cell.filled {
  background: #8f6246;
  border-color: #733d31;
  color: #f2ede6;
}

.scheme-cell.filled-with-icon {
  background: #e2d6c8;
  border-color: var(--border);
  color: var(--text-primary);
}

.scheme-cell.expiry-green {
  box-shadow: 0 0 0 2px #2f9e44;
}

.scheme-cell.expiry-yellow {
  box-shadow: 0 0 0 2px #f0ad4e;
}

.scheme-cell.expiry-red {
  box-shadow: 0 0 0 2px #d94841;
}

.scheme-cell-icon-wrap {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #f0e9df;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scheme-cell-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.sample-drawer {
  position: absolute;
  top: 0;
  right: 0;
  height: 100%;
  width: min(520px, 96vw);
  background: var(--surface);
  border-left: 1px solid var(--border);
  box-shadow: var(--shadow);
  transform: translateX(100%);
  transition: transform 0.25s ease;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
  overflow: auto;
}

.sample-drawer.open {
  transform: translateX(0);
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.drawer-title {
  margin: 0 0 6px;
  font-size: 1.25rem;
  color: var(--text-primary);
}

.drawer-header h4 {
  margin: 0 0 6px;
  color: var(--text-primary);
}

.drawer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.drawer-subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.85rem;
}

.drawer-meta {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--border);
  color: var(--text-secondary);
}

.detail-item strong {
  color: var(--text-primary);
  font-weight: 600;
}

.drawer-barcode-block {
  display: grid;
  gap: 6px;
  align-content: start;
}

.drawer-barcode-text {
  font-size: 0.9rem;
  color: var(--text-primary);
}

.tube-grid {
  display: grid;
  gap: 10px;
}

.tube-row {
  display: grid;
  grid-template-columns: 100px 1fr 140px 120px;
  gap: 8px;
  align-items: center;
}

.tube-label {
  color: var(--text-secondary);
  font-weight: 600;
}

.readonly-field {
  padding: 10px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background-color: #e2d6c8;
  color: var(--text-secondary);
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

.empty-state {
  padding: 16px;
  background-color: #ddd1c4;
  border-radius: 8px;
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
  width: min(720px, 100%);
  background: var(--surface);
  border-radius: 12px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  padding: 20px;
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

@media (max-width: 980px) {
  .storage-layout {
    grid-template-columns: 1fr;
  }
}
.alert-success,
.alert-danger {
  background-color: #cfc1ad;
  color: var(--text-primary);
  border: 1px solid #cfc1ad;
}
</style>
