<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Properties") }}</h2>
        <p>{{ i18n.t("Create and monitor properties before moving to units and billing.") }}</p>
      </div>
      <button class="btn btn-secondary" @click="load" :disabled="loading">
        {{ loading ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
      </button>
    </header>

    <div class="properties-layout" :class="{ 'single-column': !authStore.isOwner }">
      <article class="surface panel" v-if="authStore.isOwner">
        <h3>{{ i18n.t("Create property") }}</h3>
        <p class="muted">{{ i18n.t("Owner-only action. Fill required operational details.") }}</p>

        <form class="form-grid" @submit.prevent="createProperty">
          <div class="field">
            <label for="property-name">{{ i18n.t("Property name") }}</label>
            <input id="property-name" v-model="form.propertyName" class="input" required />
          </div>

          <div class="field">
            <label for="property-address">{{ i18n.t("Address line 1") }}</label>
            <input id="property-address" v-model="form.addressLine1" class="input" required />
          </div>

          <div class="field">
            <label for="property-thana">{{ i18n.t("Thana") }}</label>
            <input id="property-thana" v-model="form.thana" class="input" required />
          </div>

          <div class="field">
            <label for="property-district">{{ i18n.t("District") }}</label>
            <input id="property-district" v-model="form.district" class="input" required />
          </div>

          <div class="field">
            <label for="property-division">{{ i18n.t("Division") }}</label>
            <select id="property-division" v-model="form.division" class="select" required>
              <option v-for="division in divisions" :key="division" :value="division">{{ division }}</option>
            </select>
          </div>

          <div class="field">
            <label for="property-type">{{ i18n.t("Type") }}</label>
            <select id="property-type" v-model="form.propertyType" class="select" required>
              <option v-for="propertyType in propertyTypes" :key="propertyType" :value="propertyType">{{ readableType(propertyType) }}</option>
            </select>
          </div>

          <div class="field">
            <label for="property-units">{{ i18n.t("Total units") }}</label>
            <input id="property-units" v-model.number="form.totalUnits" class="input" type="number" min="1" required />
          </div>

          <div class="field form-span-2">
            <label for="property-notes">{{ i18n.t("Owner notes (optional)") }}</label>
            <textarea id="property-notes" v-model="form.ownerNotes" class="textarea" />
          </div>

          <button class="btn form-submit" type="submit" :disabled="creating">
            {{ creating ? i18n.t("Creating...") : i18n.t("Create Property") }}
          </button>
        </form>

        <p v-if="createError" class="inline-error">{{ createError }}</p>
      </article>

      <article class="surface panel">
        <div class="list-head">
          <h3>{{ i18n.t("Portfolio list") }}</h3>
          <span class="status-pill">{{ properties.length }} {{ i18n.t("total") }}</span>
        </div>

        <p v-if="error" class="inline-error">{{ error }}</p>
        <p v-else-if="loading" class="muted">{{ i18n.t("Loading property data...") }}</p>

        <ul v-else-if="properties.length" class="property-list">
          <li v-for="p in properties" :key="p.id" class="property-row">
            <div class="title-line">
              <h4>{{ p.propertyName }}</h4>
              <span class="status-pill" :class="{ inactive: p.status !== 'ACTIVE' }">{{ p.status }}</span>
            </div>
            <p class="muted">{{ p.district }} | {{ readableType(p.propertyType) }}</p>
            <p class="units">{{ p.totalUnits }} {{ i18n.t("units") }}</p>
            <div class="row-actions">
              <router-link class="btn btn-secondary btn-small" :to="{ name: 'property-units', params: { id: p.id } }">{{ i18n.t("Units") }}</router-link>
              <router-link class="btn btn-ghost btn-small" :to="{ name: 'property-settings', params: { id: p.id } }">{{ i18n.t("Settings") }}</router-link>
            </div>
          </li>
        </ul>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No properties yet") }}</h4>
          <p class="muted">{{ i18n.t("Create your first property to start unit and tenant workflows.") }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { apiClient } from "../api";
import { useI18n } from "../i18n";
import { useAuthStore } from "../stores/auth";

type Property = {
  id: number;
  propertyName: string;
  district: string;
  propertyType: string;
  status: string;
  totalUnits: number;
};

type CreatePropertyPayload = {
  propertyName: string;
  addressLine1: string;
  addressLine2: string | null;
  thana: string;
  district: string;
  division: string;
  propertyType: string;
  totalUnits: number;
  ownerNotes: string | null;
};

const divisions = ["DHAKA", "CHATTOGRAM", "RAJSHAHI", "KHULNA", "BARISHAL", "SYLHET", "RANGPUR", "MYMENSINGH"];
const propertyTypes = ["RESIDENTIAL_FLAT", "RESIDENTIAL_BUILDING", "COMMERCIAL", "MIXED_USE", "LAND"];

const properties = ref<Property[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const creating = ref(false);
const createError = ref<string | null>(null);
const i18n = useI18n();
const authStore = useAuthStore();
const form = ref<CreatePropertyPayload>({
  propertyName: "",
  addressLine1: "",
  addressLine2: null,
  thana: "",
  district: "Dhaka",
  division: "DHAKA",
  propertyType: "RESIDENTIAL_FLAT",
  totalUnits: 1,
  ownerNotes: null
});

const load = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get<{ success: boolean; data: Property[] }>("/properties");
    properties.value = response.data.data;
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to load properties");
  } finally {
    loading.value = false;
  }
};

const createProperty = async () => {
  creating.value = true;
  createError.value = null;
  try {
    await apiClient.post<{ success: boolean; data: Property }>("/properties", form.value);
    form.value = {
      propertyName: "",
      addressLine1: "",
      addressLine2: null,
      thana: "",
      district: "Dhaka",
      division: "DHAKA",
      propertyType: "RESIDENTIAL_FLAT",
      totalUnits: 1,
      ownerNotes: null
    };
    await load();
  } catch (err: any) {
    createError.value = err?.response?.data?.error?.message ?? i18n.t("Failed to create property");
  } finally {
    creating.value = false;
  }
};

const readableType = (value: string) => value.replaceAll("_", " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());

onMounted(load);
</script>

<style scoped>
.properties-layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.panel {
  display: grid;
  gap: 0.9rem;
  padding: 1.1rem;
}

.form-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.form-span-2,
.form-submit {
  grid-column: span 2;
}

.list-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.property-list {
  display: grid;
  gap: 0.7rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.property-row {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 0.8rem;
}

.row-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.2rem;
}

.btn-small {
  min-height: 34px;
  padding: 0.36rem 0.66rem;
}

.title-line {
  align-items: center;
  display: flex;
  gap: 0.5rem;
  justify-content: space-between;
}

h4 {
  font-size: 1rem;
  margin: 0;
}

.units {
  color: #2f4357;
  font-size: 0.92rem;
  font-weight: 600;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 960px) {
  .properties-layout {
    grid-template-columns: 1fr;
  }
}

.properties-layout.single-column {
  grid-template-columns: 1fr;
}

@media (max-width: 620px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2,
  .form-submit {
    grid-column: span 1;
  }
}
</style>
