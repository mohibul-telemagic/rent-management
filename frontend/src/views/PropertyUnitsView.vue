<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Unit Management") }}</h2>
        <p v-if="property">{{ property.propertyName }} | {{ property.district }}</p>
        <p v-else class="muted">{{ i18n.t("Load a property to manage its units.") }}</p>
      </div>
      <div class="head-actions">
        <router-link class="btn btn-secondary" :to="{ name: 'properties' }">{{ i18n.t("Back to Properties") }}</router-link>
        <router-link class="btn btn-ghost" :to="{ name: 'property-settings', params: { id: propertyId } }">{{ i18n.t("Settings") }}</router-link>
      </div>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div v-else class="layout">
      <article class="surface panel" v-if="authStore.isOwner">
        <h3>{{ editingUnitId ? i18n.t("Edit unit") : i18n.t("Add new unit") }}</h3>
        <p class="muted">
          {{ i18n.t("Use consistent unit identifiers (for example: A-3, 5B, Shop-11) to keep billing and occupancy records clean.") }}
        </p>

        <form class="form-grid" @submit.prevent="saveUnit">
          <div class="field">
            <label for="unit-identifier">{{ i18n.t("Unit identifier") }}</label>
            <input id="unit-identifier" v-model="form.unitIdentifier" class="input" required />
          </div>

          <div class="field">
            <label for="unit-floor">{{ i18n.t("Floor number") }}</label>
            <input id="unit-floor" v-model="form.floorNumber" class="input" type="number" />
          </div>

          <div class="field">
            <label for="unit-area">{{ i18n.t("Area (sqft)") }}</label>
            <input id="unit-area" v-model="form.areaSqft" class="input" type="number" min="0.01" step="0.01" />
          </div>

          <div class="field">
            <label for="unit-type">{{ i18n.t("Unit type") }}</label>
            <input id="unit-type" v-model="form.unitType" class="input" :placeholder="i18n.t('Flat, Office, Shop')" />
          </div>

          <div class="field">
            <label for="unit-occupancy">{{ i18n.t("Occupancy") }}</label>
            <select id="unit-occupancy" v-model="form.occupancyStatus" class="select" required>
              <option v-for="status in occupancyStatuses" :key="status" :value="status">{{ enumLabel(status) }}</option>
            </select>
          </div>

          <div class="actions-row">
            <button class="btn" type="submit" :disabled="saving">
              {{ saving ? i18n.t("Saving...") : editingUnitId ? i18n.t("Update Unit") : i18n.t("Create Unit") }}
            </button>
            <button v-if="editingUnitId" class="btn btn-secondary" type="button" @click="cancelEdit">{{ i18n.t("Cancel") }}</button>
          </div>
        </form>

        <p v-if="formError" class="inline-error">{{ formError }}</p>
      </article>

      <article class="surface panel" v-else>
        <h3>{{ i18n.t("Unit catalog") }}</h3>
        <p class="muted">{{ i18n.t("Manager role is read-only for unit configuration.") }}</p>
      </article>

      <article class="surface panel">
        <div class="list-head">
          <h3>{{ i18n.t("Units") }}</h3>
          <button class="btn btn-secondary btn-small" @click="loadUnits" :disabled="loadingUnits">
            {{ loadingUnits ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
          </button>
        </div>

        <p v-if="loadingContext || loadingUnits" class="muted">{{ i18n.t("Loading units...") }}</p>

        <div v-else-if="units.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{{ i18n.t("Unit") }}</th>
                <th>{{ i18n.t("Floor") }}</th>
                <th>{{ i18n.t("Area") }}</th>
                <th>{{ i18n.t("Type") }}</th>
                <th>{{ i18n.t("Occupancy") }}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="unit in units" :key="unit.id">
                <td>{{ unit.unitIdentifier }}</td>
                <td>{{ unit.floorNumber ?? "-" }}</td>
                <td>{{ unit.areaSqft ?? "-" }}</td>
                <td>{{ unit.unitType || "-" }}</td>
                <td>
                  <span class="status-pill" :class="statusClass(unit.occupancyStatus)">
                    {{ enumLabel(unit.occupancyStatus) }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button v-if="authStore.isOwner" class="btn btn-secondary btn-small" @click="startEdit(unit)">{{ i18n.t("Edit") }}</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No units yet") }}</h4>
          <p class="muted">{{ i18n.t("Create the first unit so tenant assignment and occupancy tracking can start.") }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { apiClient } from "../api";
import { useI18n } from "../i18n";
import { useAuthStore } from "../stores/auth";

type Property = {
  id: number;
  propertyName: string;
  district: string;
};

type Unit = {
  id: number;
  propertyId: number;
  unitIdentifier: string;
  floorNumber: number | null;
  areaSqft: number | null;
  unitType: string | null;
  occupancyStatus: string;
};

type UpsertUnitPayload = {
  unitIdentifier: string;
  floorNumber: number | null;
  areaSqft: number | null;
  unitType: string | null;
  occupancyStatus: string;
};

type UnitForm = {
  unitIdentifier: string;
  floorNumber: string;
  areaSqft: string;
  unitType: string;
  occupancyStatus: string;
};

const route = useRoute();
const propertyId = computed(() => Number(route.params.id));
const i18n = useI18n();
const authStore = useAuthStore();

const occupancyStatuses = ["VACANT", "OCCUPIED", "UNDER_MAINTENANCE"];
const property = ref<Property | null>(null);
const units = ref<Unit[]>([]);
const loadingContext = ref(false);
const loadingUnits = ref(false);
const saving = ref(false);
const pageError = ref<string | null>(null);
const formError = ref<string | null>(null);
const editingUnitId = ref<number | null>(null);
const form = ref<UnitForm>({
  unitIdentifier: "",
  floorNumber: "",
  areaSqft: "",
  unitType: "",
  occupancyStatus: "VACANT"
});

const enumLabel = (value: string) => i18n.translateEnum(value);

const statusClass = (status: string) => {
  if (status === "UNDER_MAINTENANCE") {
    return "pending";
  }
  if (status === "OCCUPIED") {
    return "inactive";
  }
  return "";
};

const resetForm = () => {
  editingUnitId.value = null;
  form.value = {
    unitIdentifier: "",
    floorNumber: "",
    areaSqft: "",
    unitType: "",
    occupancyStatus: "VACANT"
  };
  formError.value = null;
};

const parseOptionalNumber = (rawValue: unknown, fieldName: string) => {
  if (rawValue === null || rawValue === undefined) {
    return null;
  }
  const trimmed = String(rawValue).trim();
  if (!trimmed) {
    return null;
  }
  const parsed = Number(trimmed);
  if (!Number.isFinite(parsed)) {
    throw new Error(i18n.t(`${fieldName} must be a valid number`));
  }
  return parsed;
};

const buildPayload = (): UpsertUnitPayload => {
  const unitIdentifier = form.value.unitIdentifier.trim();
  if (!unitIdentifier) {
    throw new Error(i18n.t("Unit identifier is required"));
  }

  const floorNumber = parseOptionalNumber(form.value.floorNumber, "Floor number");
  const areaSqft = parseOptionalNumber(form.value.areaSqft, "Area");
  if (areaSqft !== null && areaSqft <= 0) {
    throw new Error(i18n.t("Area must be greater than 0"));
  }

  return {
    unitIdentifier,
    floorNumber,
    areaSqft,
    unitType: form.value.unitType.trim() || null,
    occupancyStatus: form.value.occupancyStatus
  };
};

const loadProperty = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    pageError.value = i18n.t("Invalid property ID.");
    return;
  }
  const response = await apiClient.get<{ success: boolean; data: Property }>(`/properties/${id}`);
  property.value = response.data.data;
};

const loadUnits = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }
  loadingUnits.value = true;
  try {
    const response = await apiClient.get<{ success: boolean; data: Unit[] }>(`/properties/${id}/units`);
    units.value = response.data.data;
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load units.");
  } finally {
    loadingUnits.value = false;
  }
};

const loadContext = async () => {
  pageError.value = null;
  loadingContext.value = true;
  try {
    await Promise.all([loadProperty(), loadUnits()]);
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load property.");
  } finally {
    loadingContext.value = false;
  }
};

const startEdit = (unit: Unit) => {
  editingUnitId.value = unit.id;
  formError.value = null;
  form.value = {
    unitIdentifier: unit.unitIdentifier,
    floorNumber: unit.floorNumber == null ? "" : String(unit.floorNumber),
    areaSqft: unit.areaSqft == null ? "" : String(unit.areaSqft),
    unitType: unit.unitType ?? "",
    occupancyStatus: unit.occupancyStatus
  };
};

const cancelEdit = () => {
  resetForm();
};

const saveUnit = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  let payload: UpsertUnitPayload;
  try {
    payload = buildPayload();
  } catch (error: any) {
    formError.value = error.message ?? i18n.t("Please check your input.");
    return;
  }

  saving.value = true;
  formError.value = null;
  try {
    if (editingUnitId.value) {
      await apiClient.put(`/properties/${id}/units/${editingUnitId.value}`, payload);
    } else {
      await apiClient.post(`/properties/${id}/units`, payload);
    }
    resetForm();
    await loadUnits();
  } catch (error: any) {
    formError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save unit.");
  } finally {
    saving.value = false;
  }
};

onMounted(loadContext);
</script>

<style scoped>
.layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.head-actions {
  display: flex;
  gap: 0.5rem;
}

.panel {
  display: grid;
  gap: 0.9rem;
  padding: 1.1rem;
}

.form-grid {
  display: grid;
  gap: 0.75rem;
}

.field {
  display: grid;
  gap: 0.36rem;
}

label {
  color: #42566a;
  font-size: 0.88rem;
  font-weight: 600;
}

.actions-row {
  display: flex;
  gap: 0.55rem;
}

.list-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 640px;
  width: 100%;
}

th,
td {
  border-bottom: 1px solid var(--border);
  padding: 0.68rem 0.45rem;
  text-align: left;
}

th {
  color: #4b6075;
  font-size: 0.82rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.actions-cell {
  text-align: right;
}

.btn-small {
  min-height: 34px;
  padding: 0.38rem 0.66rem;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .head-actions {
    width: 100%;
  }

  .head-actions .btn {
    flex: 1;
  }
}
</style>
