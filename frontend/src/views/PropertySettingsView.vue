<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Property Settings") }}</h2>
        <p v-if="property">{{ property.propertyName }} | {{ property.district }}</p>
        <p v-else class="muted">{{ i18n.t("Configure operational defaults for this property.") }}</p>
      </div>
      <div class="head-actions">
        <router-link class="btn btn-secondary" :to="{ name: 'properties' }">{{ i18n.t("Back to Properties") }}</router-link>
        <router-link class="btn btn-ghost" :to="{ name: 'property-units', params: { id: propertyId } }">{{ i18n.t("Manage Units") }}</router-link>
      </div>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div v-else class="stack-lg">
      <article class="surface panel">
        <div class="card-head">
          <h3>{{ i18n.t("Property profile") }}</h3>
          <span class="status-pill">{{ i18n.t("Live") }}</span>
        </div>
        <p class="muted">Saved to `/api/v1/properties/:id`.</p>

        <form class="form-grid" @submit.prevent="saveProfile">
          <div class="field">
            <label for="settings-name">{{ i18n.t("Property name") }}</label>
            <input id="settings-name" v-model="form.propertyName" class="input" required />
          </div>
          <div class="field">
            <label for="settings-address1">{{ i18n.t("Address line 1") }}</label>
            <input id="settings-address1" v-model="form.addressLine1" class="input" required />
          </div>
          <div class="field">
            <label for="settings-address2">{{ i18n.t("Address line 2 (optional)") }}</label>
            <input id="settings-address2" v-model="form.addressLine2" class="input" />
          </div>
          <div class="field">
            <label for="settings-thana">{{ i18n.t("Thana") }}</label>
            <input id="settings-thana" v-model="form.thana" class="input" required />
          </div>
          <div class="field">
            <label for="settings-district">{{ i18n.t("District") }}</label>
            <input id="settings-district" v-model="form.district" class="input" required />
          </div>
          <div class="field">
            <label for="settings-division">{{ i18n.t("Division") }}</label>
            <select id="settings-division" v-model="form.division" class="select" required>
              <option v-for="division in divisions" :key="division" :value="division">{{ division }}</option>
            </select>
          </div>
          <div class="field">
            <label for="settings-type">{{ i18n.t("Type") }}</label>
            <select id="settings-type" v-model="form.propertyType" class="select" required>
              <option v-for="propertyType in propertyTypes" :key="propertyType" :value="propertyType">
                {{ enumLabel(propertyType) }}
              </option>
            </select>
          </div>
          <div class="field">
            <label for="settings-units">{{ i18n.t("Total units") }}</label>
            <input id="settings-units" v-model.number="form.totalUnits" class="input" type="number" min="1" required />
          </div>
          <div class="field form-span-2">
            <label for="settings-notes">{{ i18n.t("Owner notes (optional)") }}</label>
            <textarea id="settings-notes" v-model="form.ownerNotes" class="textarea" />
          </div>
          <button v-if="authStore.isOwner" class="btn form-submit" type="submit" :disabled="savingProfile">
            {{ savingProfile ? i18n.t("Saving...") : i18n.t("Save Profile") }}
          </button>
        </form>
        <p v-if="!authStore.isOwner" class="muted">{{ i18n.t("Manager role is read-only for property profile.") }}</p>

        <p v-if="profileMessage" class="muted">{{ profileMessage }}</p>
        <p v-if="profileError" class="inline-error">{{ profileError }}</p>
      </article>

      <article class="surface panel">
        <div class="card-head">
          <h3>{{ i18n.t("Billing defaults") }}</h3>
          <span class="status-pill">{{ i18n.t("Live") }}</span>
        </div>
        <p class="muted">Saved to `/api/v1/properties/:id/settings`.</p>

        <form class="form-grid" @submit.prevent="saveBillingSettings">
          <div class="field">
            <label for="billing-due-day">{{ i18n.t("Invoice due day") }}</label>
            <input id="billing-due-day" v-model.number="billingSettings.invoiceDueDayOfMonth" class="input" type="number" min="1" max="31" required />
          </div>
          <div class="field">
            <label for="billing-grace">{{ i18n.t("Late fee grace days") }}</label>
            <input id="billing-grace" v-model.number="billingSettings.lateFeeGraceDays" class="input" type="number" min="0" required />
          </div>
          <div class="field">
            <label for="billing-late-fee">{{ i18n.t("Late fee flat (BDT)") }}</label>
            <input id="billing-late-fee" v-model.number="billingSettings.lateFeeFlatBdt" class="input" type="number" min="0" step="0.01" required />
          </div>
          <div class="field">
            <label for="billing-tax">{{ i18n.t("Tax percent") }}</label>
            <input id="billing-tax" v-model.number="billingSettings.taxPercent" class="input" type="number" min="0" max="100" step="0.01" required />
          </div>
          <div class="field form-span-2">
            <label for="billing-footer">{{ i18n.t("Invoice footer text (optional)") }}</label>
            <textarea id="billing-footer" v-model="billingSettings.invoiceFooterText" class="textarea" />
          </div>
          <button v-if="authStore.isOwner" class="btn form-submit" type="submit" :disabled="savingBilling">
            {{ savingBilling ? i18n.t("Saving...") : i18n.t("Save Billing Settings") }}
          </button>
        </form>
        <p v-if="!authStore.isOwner" class="muted">{{ i18n.t("Manager role is read-only for billing defaults.") }}</p>

        <p v-if="billingMessage" class="muted">{{ billingMessage }}</p>
        <p v-if="billingError" class="inline-error">{{ billingError }}</p>
      </article>

      <article class="surface panel">
        <div class="card-head">
          <h3>{{ i18n.t("Utility charge config") }}</h3>
          <span class="status-pill">{{ i18n.t("Live") }}</span>
        </div>
        <p class="muted">Saved to `/api/v1/properties/:id/utility-charge-configs`.</p>

        <form v-if="authStore.isOwner" class="utility-form" @submit.prevent="saveUtility">
          <input v-model="utilityForm.label" class="input" :placeholder="i18n.t('Utility label (Water, Gas, Internet)')" required />
          <select v-model.number="utilityForm.displayOrder" class="select" required>
            <option v-for="idx in 50" :key="idx" :value="idx - 1">{{ i18n.t("Order") }} {{ idx - 1 }}</option>
          </select>
          <label class="toggle">
            <input v-model="utilityForm.isEnabled" type="checkbox" />
            <span>{{ i18n.t("Enabled") }}</span>
          </label>
          <div class="utility-actions">
            <button class="btn" type="submit" :disabled="savingUtility">
              {{ savingUtility ? i18n.t("Saving...") : editingUtilityId ? i18n.t("Update Utility") : i18n.t("Add Utility") }}
            </button>
            <button v-if="editingUtilityId" class="btn btn-secondary" type="button" @click="resetUtilityForm">{{ i18n.t("Cancel") }}</button>
          </div>
        </form>
        <p v-else class="muted">{{ i18n.t("Manager role is read-only for utility config.") }}</p>
        <p v-if="utilityError" class="inline-error">{{ utilityError }}</p>

        <ul v-if="utilityConfigs.length" class="utility-list">
          <li v-for="item in utilityConfigs" :key="item.id" class="utility-item">
            <div>
              <p>{{ item.label }}</p>
              <p class="muted">{{ i18n.t("Display order") }}: {{ item.displayOrder }}</p>
            </div>
            <div class="item-actions">
              <span class="status-pill" :class="{ inactive: !item.isEnabled }">{{ item.isEnabled ? i18n.t("Enabled") : i18n.t("Disabled") }}</span>
              <button v-if="authStore.isOwner" class="btn btn-secondary btn-small" @click="startEditUtility(item)">{{ i18n.t("Edit") }}</button>
              <button v-if="authStore.isOwner" class="btn btn-ghost btn-small" @click="deleteUtility(item.id)">{{ i18n.t("Delete") }}</button>
            </div>
          </li>
        </ul>
        <div v-else class="empty-state">
          <h4>{{ i18n.t("No utility configs yet") }}</h4>
          <p class="muted">{{ i18n.t("Add common monthly utility lines for invoice generation.") }}</p>
        </div>
      </article>

      <article class="surface panel">
        <div class="card-head">
          <h3>{{ i18n.t("Activity Timeline") }}</h3>
          <span class="status-pill">{{ activity.length }}</span>
        </div>
        <div class="table-wrap" v-if="activity.length">
          <table>
            <thead>
              <tr>
                <th>{{ i18n.t("Created") }}</th>
                <th>{{ i18n.t("Action") }}</th>
                <th>{{ i18n.t("Actor") }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in activity" :key="item.id">
                <td>{{ item.createdAt }}</td>
                <td>{{ item.action }}</td>
                <td>#{{ item.actorUserId }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else class="muted">{{ i18n.t("No records") }}</p>
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

type PropertyDetails = {
  id: number;
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

type UpdatePropertyPayload = {
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

type PropertySettings = {
  id: number | null;
  propertyId: number;
  invoiceDueDayOfMonth: number;
  lateFeeFlatBdt: number;
  lateFeeGraceDays: number;
  taxPercent: number;
  invoiceFooterText: string | null;
};

type UpsertPropertySettingsPayload = {
  invoiceDueDayOfMonth: number;
  lateFeeFlatBdt: number;
  lateFeeGraceDays: number;
  taxPercent: number;
  invoiceFooterText: string | null;
};

type UtilityConfig = {
  id: number;
  propertyId: number;
  label: string;
  isEnabled: boolean;
  displayOrder: number;
};

type UpsertUtilityConfigPayload = {
  label: string;
  isEnabled: boolean;
  displayOrder: number;
};

type AuditTimelineItem = {
  id: number;
  actorUserId: number;
  action: string;
  entityType: string;
  entityId: string;
  createdAt: string;
};

const route = useRoute();
const propertyId = computed(() => Number(route.params.id));
const i18n = useI18n();
const authStore = useAuthStore();

const divisions = ["DHAKA", "CHATTOGRAM", "RAJSHAHI", "KHULNA", "BARISHAL", "SYLHET", "RANGPUR", "MYMENSINGH"];
const propertyTypes = ["RESIDENTIAL_FLAT", "RESIDENTIAL_BUILDING", "COMMERCIAL", "MIXED_USE", "LAND"];

const property = ref<PropertyDetails | null>(null);
const pageError = ref<string | null>(null);
const savingProfile = ref(false);
const savingBilling = ref(false);
const savingUtility = ref(false);
const profileError = ref<string | null>(null);
const billingError = ref<string | null>(null);
const utilityError = ref<string | null>(null);
const profileMessage = ref<string | null>(null);
const billingMessage = ref<string | null>(null);

const form = ref<UpdatePropertyPayload>({
  propertyName: "",
  addressLine1: "",
  addressLine2: "",
  thana: "",
  district: "",
  division: "DHAKA",
  propertyType: "RESIDENTIAL_FLAT",
  totalUnits: 1,
  ownerNotes: ""
});

const billingSettings = ref<PropertySettings>({
  id: null,
  propertyId: 0,
  invoiceDueDayOfMonth: 5,
  lateFeeFlatBdt: 0,
  lateFeeGraceDays: 0,
  taxPercent: 0,
  invoiceFooterText: ""
});

const utilityConfigs = ref<UtilityConfig[]>([]);
const activity = ref<AuditTimelineItem[]>([]);
const editingUtilityId = ref<number | null>(null);
const utilityForm = ref<UpsertUtilityConfigPayload>({
  label: "",
  isEnabled: true,
  displayOrder: 0
});

const enumLabel = (value: string) => i18n.translateEnum(value);

const resetUtilityForm = () => {
  editingUtilityId.value = null;
  utilityForm.value = {
    label: "",
    isEnabled: true,
    displayOrder: 0
  };
  utilityError.value = null;
};

const loadProperty = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    pageError.value = i18n.t("Invalid property ID.");
    return;
  }
  const response = await apiClient.get<{ success: boolean; data: PropertyDetails }>(`/properties/${id}`);
  property.value = response.data.data;
  form.value = {
    propertyName: response.data.data.propertyName,
    addressLine1: response.data.data.addressLine1,
    addressLine2: response.data.data.addressLine2 ?? "",
    thana: response.data.data.thana,
    district: response.data.data.district,
    division: response.data.data.division,
    propertyType: response.data.data.propertyType,
    totalUnits: response.data.data.totalUnits,
    ownerNotes: response.data.data.ownerNotes ?? ""
  };
};

const loadBillingSettings = async () => {
  const id = propertyId.value;
  const response = await apiClient.get<{ success: boolean; data: PropertySettings }>(`/properties/${id}/settings`);
  billingSettings.value = {
    ...response.data.data,
    invoiceFooterText: response.data.data.invoiceFooterText ?? ""
  };
};

const loadUtilityConfigs = async () => {
  const id = propertyId.value;
  const response = await apiClient.get<{ success: boolean; data: UtilityConfig[] }>(`/properties/${id}/utility-charge-configs`);
  utilityConfigs.value = response.data.data;
};

const loadActivity = async () => {
  const id = propertyId.value;
  const response = await apiClient.get<{ success: boolean; data: AuditTimelineItem[] }>(`/properties/${id}/activity`);
  activity.value = response.data.data;
};

const saveProfile = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  savingProfile.value = true;
  profileError.value = null;
  profileMessage.value = null;
  try {
    const payload: UpdatePropertyPayload = {
      propertyName: form.value.propertyName.trim(),
      addressLine1: form.value.addressLine1.trim(),
      addressLine2: form.value.addressLine2?.trim() || null,
      thana: form.value.thana.trim(),
      district: form.value.district.trim(),
      division: form.value.division,
      propertyType: form.value.propertyType,
      totalUnits: form.value.totalUnits,
      ownerNotes: form.value.ownerNotes?.trim() || null
    };
    await apiClient.put(`/properties/${id}`, payload);
    profileMessage.value = i18n.t("Property profile saved.");
    await loadProperty();
  } catch (error: any) {
    profileError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save property profile.");
  } finally {
    savingProfile.value = false;
  }
};

const saveBillingSettings = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  savingBilling.value = true;
  billingError.value = null;
  billingMessage.value = null;
  try {
    const payload: UpsertPropertySettingsPayload = {
      invoiceDueDayOfMonth: billingSettings.value.invoiceDueDayOfMonth,
      lateFeeFlatBdt: billingSettings.value.lateFeeFlatBdt,
      lateFeeGraceDays: billingSettings.value.lateFeeGraceDays,
      taxPercent: billingSettings.value.taxPercent,
      invoiceFooterText: billingSettings.value.invoiceFooterText?.trim() || null
    };
    await apiClient.put(`/properties/${id}/settings`, payload);
    billingMessage.value = i18n.t("Billing settings saved.");
    await loadBillingSettings();
  } catch (error: any) {
    billingError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save billing settings.");
  } finally {
    savingBilling.value = false;
  }
};

const saveUtility = async () => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }
  if (!utilityForm.value.label.trim()) {
    utilityError.value = i18n.t("Utility label is required.");
    return;
  }

  savingUtility.value = true;
  utilityError.value = null;
  try {
    const payload: UpsertUtilityConfigPayload = {
      label: utilityForm.value.label.trim(),
      isEnabled: utilityForm.value.isEnabled,
      displayOrder: utilityForm.value.displayOrder
    };
    if (editingUtilityId.value) {
      await apiClient.put(`/properties/${id}/utility-charge-configs/${editingUtilityId.value}`, payload);
    } else {
      await apiClient.post(`/properties/${id}/utility-charge-configs`, payload);
    }
    resetUtilityForm();
    await loadUtilityConfigs();
  } catch (error: any) {
    utilityError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save utility config.");
  } finally {
    savingUtility.value = false;
  }
};

const startEditUtility = (item: UtilityConfig) => {
  editingUtilityId.value = item.id;
  utilityForm.value = {
    label: item.label,
    isEnabled: item.isEnabled,
    displayOrder: item.displayOrder
  };
};

const deleteUtility = async (utilityId: number) => {
  const id = propertyId.value;
  if (!Number.isFinite(id) || id <= 0) {
    return;
  }

  utilityError.value = null;
  if (!window.confirm(i18n.t("Delete utility config #{id}? Existing invoices will remain unchanged.", { id: utilityId }))) {
    return;
  }
  try {
    await apiClient.delete(`/properties/${id}/utility-charge-configs/${utilityId}`);
    if (editingUtilityId.value === utilityId) {
      resetUtilityForm();
    }
    await loadUtilityConfigs();
  } catch (error: any) {
    utilityError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to delete utility config.");
  }
};

onMounted(async () => {
  pageError.value = null;
  try {
    await Promise.all([loadProperty(), loadBillingSettings(), loadUtilityConfigs(), loadActivity()]);
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load property settings.");
  }
});
</script>

<style scoped>
.head-actions {
  display: flex;
  gap: 0.5rem;
}

.panel {
  display: grid;
  gap: 0.9rem;
  padding: 1.1rem;
}

.card-head {
  align-items: center;
  display: flex;
  gap: 0.6rem;
  justify-content: space-between;
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

.utility-form {
  align-items: end;
  display: grid;
  gap: 0.65rem;
  grid-template-columns: 1.8fr 1fr auto auto;
}

.utility-actions {
  display: flex;
  gap: 0.45rem;
}

.utility-list {
  display: grid;
  gap: 0.65rem;
  list-style: none;
  margin: 0;
  padding: 0;
}

.utility-item {
  align-items: center;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border);
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  padding: 0.65rem 0.75rem;
}

.item-actions {
  align-items: center;
  display: flex;
  gap: 0.4rem;
}

.toggle {
  align-items: center;
  cursor: pointer;
  display: inline-flex;
  gap: 0.45rem;
}

.btn-small {
  min-height: 34px;
  padding: 0.36rem 0.64rem;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 900px) {
  .utility-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .head-actions {
    width: 100%;
  }

  .head-actions .btn {
    flex: 1;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2,
  .form-submit {
    grid-column: span 1;
  }

  .utility-item {
    align-items: flex-start;
    flex-direction: column;
    gap: 0.45rem;
  }
}
</style>
