<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Invoice Engine") }}</h2>
        <p>{{ i18n.t("Generate monthly invoices, review lifecycle state, and store SMS-ready messages.") }}</p>
      </div>
      <button class="btn btn-secondary" @click="refreshAll" :disabled="loadingInvoices">
        {{ loadingInvoices ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
      </button>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div class="layout">
      <article class="surface panel stack-md">
        <div class="panel-head">
          <h3>{{ i18n.t("Generate Single Invoice") }}</h3>
          <span class="status-pill">{{ i18n.t("Draft Flow") }}</span>
        </div>

        <form class="form-grid" @submit.prevent="generateSingle">
          <div class="field">
            <label for="single-tenant">{{ i18n.t("Tenant") }}</label>
            <select id="single-tenant" v-model.number="singleForm.tenantId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select active tenant") }}</option>
              <option v-for="tenant in activeTenants" :key="tenant.id" :value="tenant.id">
                {{ tenant.fullName }} (#{{ tenant.propertyUnitId }})
              </option>
            </select>
          </div>

          <div class="field">
            <label for="single-month">{{ i18n.t("Billing month") }}</label>
            <input id="single-month" v-model="singleForm.billingMonth" class="input" type="month" required />
          </div>

          <div class="field form-span-2">
            <label>{{ i18n.t("Utility charges") }}</label>
            <div class="charges-list">
              <div v-for="(charge, idx) in singleForm.utilityCharges" :key="`single-${idx}`" class="charge-row">
                <input v-model="charge.label" class="input" :placeholder="i18n.t('Label (Water, Gas, Service Charge)')" />
                <input v-model="charge.amount" class="input" type="number" min="0" step="0.01" :placeholder="i18n.t('Amount (BDT)')" />
                <button class="btn btn-ghost btn-small" type="button" @click="removeCharge(singleForm.utilityCharges, idx)">
                  {{ i18n.t("Remove") }}
                </button>
              </div>
            </div>
            <button class="btn btn-secondary btn-small" type="button" @click="addCharge(singleForm.utilityCharges)">
              {{ i18n.t("Add Utility") }}
            </button>
          </div>

          <button class="btn form-submit" type="submit" :disabled="singleSubmitting">
            {{ singleSubmitting ? i18n.t("Generating...") : i18n.t("Generate Invoice") }}
          </button>
        </form>

        <p v-if="singleMessage" class="muted">{{ singleMessage }}</p>
        <p v-if="singleError" class="inline-error">{{ singleError }}</p>
      </article>

      <article class="surface panel stack-md">
        <div class="panel-head">
          <h3>{{ i18n.t("Bulk Generate") }}</h3>
          <span class="status-pill pending">{{ i18n.t("Property Batch") }}</span>
        </div>

        <form class="form-grid" @submit.prevent="generateBulk">
          <div class="field">
            <label for="bulk-property">{{ i18n.t("Property") }}</label>
            <select id="bulk-property" v-model.number="bulkForm.propertyId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select property") }}</option>
              <option v-for="property in properties" :key="property.id" :value="property.id">
                {{ property.propertyName }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="bulk-month">{{ i18n.t("Billing month") }}</label>
            <input id="bulk-month" v-model="bulkForm.billingMonth" class="input" type="month" required />
          </div>

          <div class="field form-span-2">
            <label>{{ i18n.t("Utility charges (applied to all generated invoices)") }}</label>
            <div class="charges-list">
              <div v-for="(charge, idx) in bulkForm.utilityCharges" :key="`bulk-${idx}`" class="charge-row">
                <input v-model="charge.label" class="input" :placeholder="i18n.t('Label')" />
                <input v-model="charge.amount" class="input" type="number" min="0" step="0.01" :placeholder="i18n.t('Amount (BDT)')" />
                <button class="btn btn-ghost btn-small" type="button" @click="removeCharge(bulkForm.utilityCharges, idx)">
                  {{ i18n.t("Remove") }}
                </button>
              </div>
            </div>
            <button class="btn btn-secondary btn-small" type="button" @click="addCharge(bulkForm.utilityCharges)">
              {{ i18n.t("Add Utility") }}
            </button>
          </div>

          <button class="btn form-submit" type="submit" :disabled="bulkSubmitting">
            {{ bulkSubmitting ? i18n.t("Generating...") : i18n.t("Generate Bulk") }}
          </button>
        </form>

        <p v-if="bulkMessage" class="muted">{{ bulkMessage }}</p>
        <p v-if="bulkError" class="inline-error">{{ bulkError }}</p>
      </article>
    </div>

    <article class="surface panel stack-md">
      <div class="section-head compact">
        <div>
          <h3>{{ i18n.t("Invoice Registry") }}</h3>
          <p class="muted">{{ i18n.t("Newest invoices are shown first. Filter by property, billing month, and lifecycle status.") }}</p>
        </div>
        <span class="status-pill">{{ invoices.length }} {{ i18n.t("records") }}</span>
      </div>

      <div class="filters">
        <select v-model.number="filters.propertyId" class="select">
          <option :value="null">{{ i18n.t("All properties") }}</option>
          <option v-for="property in properties" :key="property.id" :value="property.id">
            {{ property.propertyName }}
          </option>
        </select>

        <select v-model="filters.status" class="select">
          <option :value="null">{{ i18n.t("All statuses") }}</option>
          <option v-for="status in statuses" :key="status" :value="status">
            {{ enumLabel(status) }}
          </option>
        </select>

        <input v-model="filters.billingMonth" class="input" type="month" />

        <button class="btn btn-secondary" @click="loadInvoices" :disabled="loadingInvoices">
          {{ loadingInvoices ? i18n.t("Loading...") : i18n.t("Apply") }}
        </button>
        <button class="btn btn-ghost" @click="clearFiltersAndReload" :disabled="loadingInvoices">
          {{ i18n.t("Clear") }}
        </button>
      </div>

      <div class="table-wrap" v-if="invoices.length">
        <table>
          <thead>
            <tr>
              <th>{{ i18n.t("Tenant") }}</th>
              <th>{{ i18n.t("Property") }}</th>
              <th>{{ i18n.t("Period") }}</th>
              <th>{{ i18n.t("Due") }}</th>
              <th>{{ i18n.t("Total") }}</th>
              <th>{{ i18n.t("Balance") }}</th>
              <th>{{ i18n.t("Status") }}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="invoice in invoices"
              :key="invoice.id"
              :class="{ selected: selectedInvoiceId === invoice.id }"
              @click="selectInvoice(invoice.id)"
            >
              <td>{{ invoice.tenantName || `${i18n.t("Tenant")} #${invoice.tenantId}` }}</td>
              <td>{{ invoice.propertyName || `${i18n.t("Property")} #${invoice.propertyId}` }}</td>
              <td>{{ formatPeriod(invoice.billingPeriodStart, invoice.billingPeriodEnd) }}</td>
              <td>{{ invoice.dueDate }}</td>
              <td>{{ asMoney(invoice.totalDueBdt) }}</td>
              <td>{{ asMoney(invoice.balanceDueBdt) }}</td>
              <td>
                <span class="status-pill" :class="statusClass(invoice.status)">{{ enumLabel(invoice.status) }}</span>
              </td>
              <td class="actions-cell">
                <div class="row-actions">
                  <button
                    class="btn btn-secondary btn-small"
                    type="button"
                    @click.stop="sendInvoice(invoice.id)"
                    :disabled="actionLoadingId === invoice.id"
                  >
                    {{ i18n.t("Send") }}
                  </button>
                  <button
                    class="btn btn-ghost btn-small"
                    type="button"
                    @click.stop="cancelInvoice(invoice.id)"
                    :disabled="actionLoadingId === invoice.id"
                  >
                    {{ i18n.t("Cancel") }}
                  </button>
                  <button
                    v-if="authStore.isOwner"
                    class="btn btn-ghost btn-small"
                    type="button"
                    @click.stop="voidInvoice(invoice.id)"
                    :disabled="actionLoadingId === invoice.id"
                  >
                    {{ i18n.t("Void") }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else-if="loadingInvoices" class="muted">{{ i18n.t("Loading invoices...") }}</div>
      <div v-else class="empty-state">
        <h4>{{ i18n.t("No invoices found") }}</h4>
        <p class="muted">{{ i18n.t("Generate invoices to initialize billing cycles for active tenants.") }}</p>
      </div>

      <p v-if="listError" class="inline-error">{{ listError }}</p>
      <p v-if="actionMessage" class="muted">{{ actionMessage }}</p>
    </article>

    <article v-if="selectedInvoice" class="surface panel stack-md">
      <div class="panel-head">
        <h3>{{ i18n.t("Invoice Detail") }}</h3>
        <span class="status-pill" :class="statusClass(selectedInvoice.status)">
          {{ enumLabel(selectedInvoice.status) }}
        </span>
      </div>

      <div class="row-actions">
        <button class="btn btn-secondary btn-small" type="button" @click="downloadInvoicePdf('en')" :disabled="fileActionLoading">
          PDF (EN)
        </button>
        <button class="btn btn-secondary btn-small" type="button" @click="downloadInvoicePdf('bn')" :disabled="fileActionLoading">
          PDF (BN)
        </button>
        <button class="btn btn-ghost btn-small" type="button" @click="downloadCsv('invoices')" :disabled="fileActionLoading">
          {{ i18n.t("Export Invoices CSV") }}
        </button>
        <button class="btn btn-ghost btn-small" type="button" @click="createZipExportJob" :disabled="fileActionLoading">
          {{ i18n.t("Create ZIP Job") }}
        </button>
      </div>

      <div class="detail-grid">
        <div>
          <p class="muted">{{ i18n.t("Tenant") }}</p>
          <p>{{ selectedInvoice.tenantName || `${i18n.t("Tenant")} #${selectedInvoice.tenantId}` }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Property") }}</p>
          <p>{{ selectedInvoice.propertyName || `${i18n.t("Property")} #${selectedInvoice.propertyId}` }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Unit") }}</p>
          <p>{{ selectedInvoice.unitIdentifier || `#${selectedInvoice.propertyUnitId}` }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Billing Period") }}</p>
          <p>{{ formatPeriod(selectedInvoice.billingPeriodStart, selectedInvoice.billingPeriodEnd) }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Due Date") }}</p>
          <p>{{ selectedInvoice.dueDate }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Total Due") }}</p>
          <p>{{ asMoney(selectedInvoice.totalDueBdt) }}</p>
        </div>
        <div>
          <p class="muted">{{ i18n.t("Balance Due") }}</p>
          <p>{{ asMoney(selectedInvoice.balanceDueBdt) }}</p>
        </div>
      </div>

      <div>
        <p class="muted">{{ i18n.t("Utility Snapshot") }}</p>
        <pre class="json-box">{{ formattedUtilitySnapshot }}</pre>
      </div>

      <div>
        <p class="muted">{{ i18n.t("SMS Text") }}</p>
        <p class="sms-box">{{ selectedInvoice.smsText || i18n.t("SMS text will be generated when invoice is sent.") }}</p>
      </div>

      <div class="payment-layout">
        <article class="surface payment-card">
          <h4>{{ i18n.t("Record Payment (Invoice)") }}</h4>
          <p class="muted">{{ i18n.t("Apply payment to this invoice only.") }}</p>
          <form class="payment-form" @submit.prevent="recordInvoicePayment">
            <input v-model="paymentForm.amountBdt" class="input" type="number" min="0.01" step="0.01" :placeholder="i18n.t('Amount (BDT)')" required />
            <input v-model="paymentForm.paymentDate" class="input" type="date" required />
            <select v-model="paymentForm.paymentMethod" class="select" required>
              <option value="CASH">{{ i18n.t("Cash") }}</option>
              <option value="BANK_TRANSFER">{{ i18n.t("Bank Transfer") }}</option>
              <option value="MOBILE_BANKING">{{ i18n.t("Mobile Banking") }}</option>
              <option value="OTHER">{{ i18n.t("Other") }}</option>
            </select>
            <textarea v-model="paymentForm.notes" class="textarea" :placeholder="i18n.t('Notes (optional)')" />
            <button class="btn" type="submit" :disabled="paymentSubmitting">
              {{ paymentSubmitting ? i18n.t("Recording...") : i18n.t("Record Payment") }}
            </button>
          </form>
        </article>

        <article class="surface payment-card">
          <h4>{{ i18n.t("Apply FIFO Payment (Tenant)") }}</h4>
          <p class="muted">{{ i18n.t("Distribute payment across oldest outstanding invoices.") }}</p>
          <form class="payment-form" @submit.prevent="applyFifoPayment">
            <select v-model.number="fifoForm.tenantId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select tenant") }}</option>
              <option v-for="tenant in activeTenants" :key="tenant.id" :value="tenant.id">{{ tenant.fullName }}</option>
            </select>
            <input v-model="fifoForm.totalAmountBdt" class="input" type="number" min="0.01" step="0.01" :placeholder="i18n.t('Total Amount (BDT)')" required />
            <input v-model="fifoForm.paymentDate" class="input" type="date" required />
            <select v-model="fifoForm.paymentMethod" class="select" required>
              <option value="CASH">{{ i18n.t("Cash") }}</option>
              <option value="BANK_TRANSFER">{{ i18n.t("Bank Transfer") }}</option>
              <option value="MOBILE_BANKING">{{ i18n.t("Mobile Banking") }}</option>
              <option value="OTHER">{{ i18n.t("Other") }}</option>
            </select>
            <textarea v-model="fifoForm.notes" class="textarea" :placeholder="i18n.t('Notes (optional)')" />
            <button class="btn btn-secondary" type="submit" :disabled="paymentSubmitting">
              {{ paymentSubmitting ? i18n.t("Applying...") : i18n.t("Apply FIFO") }}
            </button>
          </form>
        </article>
      </div>

      <p v-if="paymentError" class="inline-error">{{ paymentError }}</p>
      <p v-if="paymentMessage" class="muted">{{ paymentMessage }}</p>
      <p v-if="fileActionMessage" class="muted">{{ fileActionMessage }}</p>
      <p v-if="fileActionError" class="inline-error">{{ fileActionError }}</p>
      <p v-if="detailError" class="inline-error">{{ detailError }}</p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { apiClient } from "../api";
import { useI18n } from "../i18n";
import { useAuthStore } from "../stores/auth";

type ApiEnvelope<T> = { success: boolean; data: T };

type Property = {
  id: number;
  propertyName: string;
};

type Tenant = {
  id: number;
  fullName: string;
  propertyUnitId: number;
  status: string;
};

type Invoice = {
  id: number;
  tenantId: number;
  tenantName: string | null;
  propertyUnitId: number;
  unitIdentifier: string | null;
  propertyId: number | null;
  propertyName: string | null;
  billingPeriodStart: string;
  billingPeriodEnd: string;
  dueDate: string;
  status: string;
  baseRentBdt: number;
  lateFeeBdt: number;
  taxBdt: number;
  totalDueBdt: number;
  balanceDueBdt: number;
  smsText: string | null;
  utilityChargesJson: string | null;
};

type UtilityChargeLine = {
  label: string;
  amount: string;
};

type BulkGenerateResponse = {
  requestedCount: number;
  generatedCount: number;
  skippedCount: number;
};

type GenerateSingleResponse = {
  invoice: Invoice;
  warnings: string[];
};

type RecordPaymentResponse = {
  invoiceId: number;
  appliedAmountBdt: number;
  invoice: Invoice;
};

type ApplyFifoResponse = {
  tenantId: number;
  requestedAmountBdt: number;
  appliedAmountBdt: number;
  remainingUnappliedBdt: number;
  allocations: Array<{
    invoiceId: number;
    appliedAmountBdt: number;
    remainingBalanceBdt: number;
    newStatus: string;
  }>;
};

type ExportJobResponse = {
  id: string;
  status: "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";
  errorMessage: string | null;
  downloadable: boolean;
};

const statuses = ["DRAFT", "SENT", "PAID", "PARTIALLY_PAID", "OVERDUE", "CANCELLED", "VOID"];
const i18n = useI18n();
const route = useRoute();
const authStore = useAuthStore();

const properties = ref<Property[]>([]);
const tenants = ref<Tenant[]>([]);
const invoices = ref<Invoice[]>([]);
const selectedInvoice = ref<Invoice | null>(null);
const selectedInvoiceId = ref<number | null>(null);

const loadingInvoices = ref(false);
const singleSubmitting = ref(false);
const bulkSubmitting = ref(false);
const actionLoadingId = ref<number | null>(null);
const paymentSubmitting = ref(false);
const fileActionLoading = ref(false);

const pageError = ref<string | null>(null);
const listError = ref<string | null>(null);
const detailError = ref<string | null>(null);
const singleError = ref<string | null>(null);
const bulkError = ref<string | null>(null);
const paymentError = ref<string | null>(null);
const fileActionError = ref<string | null>(null);

const singleMessage = ref<string | null>(null);
const bulkMessage = ref<string | null>(null);
const actionMessage = ref<string | null>(null);
const paymentMessage = ref<string | null>(null);
const fileActionMessage = ref<string | null>(null);
const routeInvoiceId = ref<number | null>(null);

const filters = ref<{ propertyId: number | null; tenantId: number | null; status: string | null; billingMonth: string | null }>({
  propertyId: null,
  tenantId: null,
  status: null,
  billingMonth: null
});

const singleForm = ref<{ tenantId: number | null; billingMonth: string; utilityCharges: UtilityChargeLine[] }>({
  tenantId: null,
  billingMonth: currentMonth(),
  utilityCharges: []
});

const bulkForm = ref<{ propertyId: number | null; billingMonth: string; utilityCharges: UtilityChargeLine[] }>({
  propertyId: null,
  billingMonth: currentMonth(),
  utilityCharges: []
});

const paymentForm = ref<{
  amountBdt: string;
  paymentDate: string;
  paymentMethod: string;
  notes: string;
}>({
  amountBdt: "",
  paymentDate: todayIso(),
  paymentMethod: "CASH",
  notes: ""
});

const fifoForm = ref<{
  tenantId: number | null;
  totalAmountBdt: string;
  paymentDate: string;
  paymentMethod: string;
  notes: string;
}>({
  tenantId: null,
  totalAmountBdt: "",
  paymentDate: todayIso(),
  paymentMethod: "CASH",
  notes: ""
});

const activeTenants = computed(() => tenants.value.filter((tenant) => tenant.status === "ACTIVE"));

const formattedUtilitySnapshot = computed(() => {
  if (!selectedInvoice.value?.utilityChargesJson) {
    return i18n.t("No utility snapshot stored.");
  }
  try {
    return JSON.stringify(JSON.parse(selectedInvoice.value.utilityChargesJson), null, 2);
  } catch {
    return selectedInvoice.value.utilityChargesJson;
  }
});

function currentMonth() {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  return `${now.getFullYear()}-${month}`;
}

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function monthToStartDate(monthValue: string) {
  return `${monthValue}-01`;
}

function enumLabel(status: string) {
  return i18n.translateEnum(status);
}

function asMoney(value: number) {
  return i18n.formatMoney(value);
}

function formatPeriod(start: string, end: string) {
  return `${start} ${i18n.t("to")} ${end}`;
}

function statusClass(status: string) {
  if (status === "VOID" || status === "CANCELLED") {
    return "inactive";
  }
  if (status === "OVERDUE" || status === "PARTIALLY_PAID") {
    return "pending";
  }
  return "";
}

function addCharge(list: UtilityChargeLine[]) {
  list.push({ label: "", amount: "" });
}

function removeCharge(list: UtilityChargeLine[], index: number) {
  list.splice(index, 1);
}

function buildChargesPayload(list: UtilityChargeLine[]) {
  return list
    .map((item) => ({
      label: item.label.trim(),
      amountBdt: Number(item.amount)
    }))
    .filter((item) => item.label.length > 0 && Number.isFinite(item.amountBdt) && item.amountBdt >= 0);
}

async function loadProperties() {
  const response = await apiClient.get<ApiEnvelope<Property[]>>("/properties");
  properties.value = response.data.data;
}

async function loadTenants() {
  const response = await apiClient.get<ApiEnvelope<Tenant[]>>("/tenants");
  tenants.value = [...response.data.data].sort((a, b) => b.id - a.id);
}

async function loadInvoices() {
  loadingInvoices.value = true;
  listError.value = null;
  try {
    const params: Record<string, string | number> = {};
    if (filters.value.propertyId) {
      params.propertyId = filters.value.propertyId;
    }
    if (filters.value.tenantId) {
      params.tenantId = filters.value.tenantId;
    }
    if (filters.value.status) {
      params.status = filters.value.status;
    }
    if (filters.value.billingMonth) {
      params.billingMonth = filters.value.billingMonth;
    }

    const response = await apiClient.get<ApiEnvelope<Invoice[]>>("/invoices", { params });
    invoices.value = [...response.data.data].sort((a, b) => b.id - a.id);

    if (selectedInvoiceId.value && !invoices.value.some((invoice) => invoice.id === selectedInvoiceId.value)) {
      selectedInvoice.value = null;
      selectedInvoiceId.value = null;
    }
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load invoices.");
  } finally {
    loadingInvoices.value = false;
  }
}

async function loadInvoiceDetail(invoiceId: number) {
  detailError.value = null;
  try {
    const response = await apiClient.get<ApiEnvelope<Invoice>>(`/invoices/${invoiceId}`);
    selectedInvoice.value = response.data.data;
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load invoice detail.");
  }
}

async function selectInvoice(invoiceId: number) {
  selectedInvoiceId.value = invoiceId;
  await loadInvoiceDetail(invoiceId);
  if (selectedInvoice.value) {
    fifoForm.value.tenantId = selectedInvoice.value.tenantId;
  }
}

async function generateSingle() {
  if (!singleForm.value.tenantId) {
    singleError.value = i18n.t("Select a tenant.");
    return;
  }

  singleSubmitting.value = true;
  singleError.value = null;
  singleMessage.value = null;
  try {
    const payload = {
      tenantId: singleForm.value.tenantId,
      billingPeriodStart: monthToStartDate(singleForm.value.billingMonth),
      utilityCharges: buildChargesPayload(singleForm.value.utilityCharges)
    };
    const response = await apiClient.post<ApiEnvelope<GenerateSingleResponse>>("/invoices", payload);
    const warnings = response.data.data.warnings;
    const createdInvoiceId = response.data.data.invoice.id;
    singleMessage.value = warnings?.length
      ? i18n.t("Invoice generated with warning: {warnings}", { warnings: warnings.join(" ") })
      : i18n.t("Invoice generated. It has been moved to the top of the registry.");

    singleForm.value.utilityCharges = [];
    filters.value = {
      propertyId: null,
      tenantId: null,
      status: null,
      billingMonth: null
    };
    await loadInvoices();
    await selectInvoice(createdInvoiceId);
  } catch (error: any) {
    singleError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to generate invoice.");
  } finally {
    singleSubmitting.value = false;
  }
}

async function clearFiltersAndReload() {
  filters.value = {
    propertyId: null,
    tenantId: null,
    status: null,
    billingMonth: null
  };
  await loadInvoices();
}

async function generateBulk() {
  if (!bulkForm.value.propertyId) {
    bulkError.value = i18n.t("Select a property.");
    return;
  }

  bulkSubmitting.value = true;
  bulkError.value = null;
  bulkMessage.value = null;
  try {
    const payload = {
      propertyId: bulkForm.value.propertyId,
      billingPeriodStart: monthToStartDate(bulkForm.value.billingMonth),
      utilityCharges: buildChargesPayload(bulkForm.value.utilityCharges)
    };
    const response = await apiClient.post<ApiEnvelope<BulkGenerateResponse>>("/invoices/bulk-generate", payload);
    const summary = response.data.data;
    bulkMessage.value = i18n.t("Bulk done. Generated: {generated}, skipped: {skipped}.", {
      generated: summary.generatedCount,
      skipped: summary.skippedCount
    });

    bulkForm.value.utilityCharges = [];
    await loadInvoices();
  } catch (error: any) {
    bulkError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to run bulk generation.");
  } finally {
    bulkSubmitting.value = false;
  }
}

async function sendInvoice(invoiceId: number) {
  await runAction(invoiceId, "send", null, i18n.t("Invoice sent."));
}

async function cancelInvoice(invoiceId: number) {
  if (!window.confirm(i18n.t("Cancel invoice #{id}? This can impact receivable reporting.", { id: invoiceId }))) {
    return;
  }
  const reason = window.prompt(i18n.t("Cancellation reason"), i18n.t("Cancelled by operator"))?.trim();
  if (!reason) {
    return;
  }
  await runAction(invoiceId, "cancel", { reason }, i18n.t("Invoice cancelled."));
}

async function voidInvoice(invoiceId: number) {
  if (!window.confirm(i18n.t("Void invoice #{id}? This is owner-only and irreversible.", { id: invoiceId }))) {
    return;
  }
  const reason = window.prompt(i18n.t("Void reason"), i18n.t("Voided by owner"))?.trim();
  if (!reason) {
    return;
  }
  await runAction(invoiceId, "void", { reason }, i18n.t("Invoice voided."));
}

async function runAction(
  invoiceId: number,
  action: "send" | "cancel" | "void",
  body: Record<string, unknown> | null,
  successMessage: string
) {
  actionLoadingId.value = invoiceId;
  actionMessage.value = null;
  listError.value = null;
  try {
    await apiClient.post(`/invoices/${invoiceId}/${action}`, body ?? {});
    actionMessage.value = successMessage;
    await loadInvoices();
    if (selectedInvoiceId.value === invoiceId) {
      await loadInvoiceDetail(invoiceId);
    }
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to {action} invoice.", { action });
  } finally {
    actionLoadingId.value = null;
  }
}

async function recordInvoicePayment() {
  if (!selectedInvoice.value) {
    paymentError.value = i18n.t("Select an invoice first.");
    return;
  }
  const amount = Number(paymentForm.value.amountBdt);
  if (!Number.isFinite(amount) || amount <= 0) {
    paymentError.value = i18n.t("Payment amount must be greater than 0.");
    return;
  }

  paymentSubmitting.value = true;
  paymentError.value = null;
  paymentMessage.value = null;
  try {
    const payload = {
      amountBdt: amount,
      paymentDate: paymentForm.value.paymentDate,
      paymentMethod: paymentForm.value.paymentMethod,
      notes: paymentForm.value.notes.trim() || null
    };
    const response = await apiClient.post<ApiEnvelope<RecordPaymentResponse>>(
      `/invoices/${selectedInvoice.value.id}/payments`,
      payload
    );
    paymentMessage.value = i18n.t("Payment recorded: {amount}.", { amount: asMoney(response.data.data.appliedAmountBdt) });
    paymentForm.value.amountBdt = "";
    paymentForm.value.notes = "";
    await loadInvoices();
    await loadInvoiceDetail(selectedInvoice.value.id);
  } catch (error: any) {
    paymentError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to record payment.");
  } finally {
    paymentSubmitting.value = false;
  }
}

async function applyFifoPayment() {
  if (!fifoForm.value.tenantId) {
    paymentError.value = i18n.t("Select a tenant for FIFO allocation.");
    return;
  }
  const amount = Number(fifoForm.value.totalAmountBdt);
  if (!Number.isFinite(amount) || amount <= 0) {
    paymentError.value = i18n.t("FIFO amount must be greater than 0.");
    return;
  }

  paymentSubmitting.value = true;
  paymentError.value = null;
  paymentMessage.value = null;
  try {
    const payload = {
      tenantId: fifoForm.value.tenantId,
      totalAmountBdt: amount,
      paymentDate: fifoForm.value.paymentDate,
      paymentMethod: fifoForm.value.paymentMethod,
      notes: fifoForm.value.notes.trim() || null
    };
    const response = await apiClient.post<ApiEnvelope<ApplyFifoResponse>>("/invoices/payments/apply", payload);
    const result = response.data.data;
    paymentMessage.value = i18n.t("FIFO applied {amount} across {count} invoice(s).", {
      amount: asMoney(result.appliedAmountBdt),
      count: result.allocations.length
    });
    fifoForm.value.totalAmountBdt = "";
    fifoForm.value.notes = "";
    await loadInvoices();
    if (selectedInvoiceId.value) {
      await loadInvoiceDetail(selectedInvoiceId.value);
    }
  } catch (error: any) {
    paymentError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to apply FIFO payment.");
  } finally {
    paymentSubmitting.value = false;
  }
}

function triggerBrowserDownload(data: Blob, fileName: string) {
  const url = URL.createObjectURL(data);
  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

async function downloadInvoicePdf(lang: "en" | "bn") {
  if (!selectedInvoice.value) {
    fileActionError.value = i18n.t("Select an invoice first.");
    return;
  }

  fileActionLoading.value = true;
  fileActionError.value = null;
  fileActionMessage.value = null;
  try {
    const response = await apiClient.get(`/invoices/${selectedInvoice.value.id}/pdf`, {
      params: { lang },
      responseType: "blob"
    });
    triggerBrowserDownload(response.data, `invoice-${selectedInvoice.value.id}-${lang}.pdf`);
    if (response.headers["x-language-fallback"] === "en" && lang === "bn") {
      fileActionMessage.value = i18n.t("Bangla font was unavailable. PDF generated in English fallback.");
    } else {
      fileActionMessage.value = i18n.t("Invoice PDF downloaded.");
    }
  } catch (error: any) {
    fileActionError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download invoice PDF.");
  } finally {
    fileActionLoading.value = false;
  }
}

async function downloadCsv(dataset: "invoices" | "tenants" | "payments" | "expenses" | "deposits") {
  fileActionLoading.value = true;
  fileActionError.value = null;
  fileActionMessage.value = null;
  try {
    const response = await apiClient.get(`/export/${dataset}`, { responseType: "blob" });
    triggerBrowserDownload(response.data, `${dataset}.csv`);
    fileActionMessage.value = i18n.t("{dataset} CSV downloaded.", { dataset });
  } catch (error: any) {
    fileActionError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to export {dataset} CSV.", { dataset });
  } finally {
    fileActionLoading.value = false;
  }
}

async function createZipExportJob() {
  fileActionLoading.value = true;
  fileActionError.value = null;
  fileActionMessage.value = null;
  try {
    const create = await apiClient.post<ApiEnvelope<ExportJobResponse>>("/export/jobs/zip", {
      datasets: ["tenants", "invoices", "payments"]
    });
    const jobId = create.data.data.id;
    let statusPayload: ExportJobResponse = create.data.data;
    for (let i = 0; i < 20; i++) {
      if (statusPayload.status === "COMPLETED" || statusPayload.status === "FAILED") {
        break;
      }
      await new Promise((resolve) => setTimeout(resolve, 1000));
      const poll = await apiClient.get<ApiEnvelope<ExportJobResponse>>(`/export/jobs/${jobId}`);
      statusPayload = poll.data.data;
    }

    if (statusPayload.status !== "COMPLETED") {
      throw new Error(statusPayload.errorMessage ?? `Export job ended with status ${statusPayload.status}`);
    }

    const zipResponse = await apiClient.get(`/export/jobs/${jobId}/download`, { responseType: "blob" });
    triggerBrowserDownload(zipResponse.data, `export-${jobId}.zip`);
    fileActionMessage.value = i18n.t("ZIP export downloaded.");
  } catch (error: any) {
    fileActionError.value = error?.response?.data?.error?.message ?? error?.message ?? i18n.t("ZIP export failed.");
  } finally {
    fileActionLoading.value = false;
  }
}

async function refreshAll() {
  pageError.value = null;
  try {
    await Promise.all([loadProperties(), loadTenants()]);
    await loadInvoices();
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load invoice page context.");
  }
}

function applyRouteFilters() {
  const parseOptionalNumber = (value: unknown) => {
    if (typeof value !== "string") {
      return null;
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  };
  filters.value.propertyId = parseOptionalNumber(route.query.propertyId);
  filters.value.tenantId = parseOptionalNumber(route.query.tenantId);
  filters.value.status = typeof route.query.status === "string" ? route.query.status : null;
  filters.value.billingMonth = typeof route.query.billingMonth === "string" ? route.query.billingMonth : null;
  routeInvoiceId.value = parseOptionalNumber(route.query.invoiceId);
}

onMounted(async () => {
  applyRouteFilters();
  await refreshAll();
  if (routeInvoiceId.value) {
    await selectInvoice(routeInvoiceId.value);
  }
});
</script>

<style scoped>
.layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.panel {
  padding: 1.1rem;
}

.panel-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.form-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field {
  display: grid;
  gap: 0.35rem;
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

.charges-list {
  display: grid;
  gap: 0.5rem;
  margin-bottom: 0.55rem;
}

.charge-row {
  display: grid;
  gap: 0.45rem;
  grid-template-columns: 1.5fr 1fr auto;
}

.compact {
  align-items: flex-start;
}

.filters {
  display: grid;
  gap: 0.55rem;
  grid-template-columns: 1fr 1fr 1fr auto auto;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 960px;
  width: 100%;
}

th,
td {
  border-bottom: 1px solid var(--border);
  padding: 0.65rem 0.42rem;
  text-align: left;
}

th {
  color: #4b6075;
  font-size: 0.81rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

tbody tr {
  cursor: pointer;
}

tbody tr:hover {
  background: rgba(124, 156, 184, 0.08);
}

tbody tr.selected {
  background: rgba(10, 105, 95, 0.1);
}

.actions-cell {
  text-align: right;
}

.row-actions {
  display: inline-flex;
  gap: 0.35rem;
}

.btn-small {
  min-height: 34px;
  padding: 0.35rem 0.58rem;
}

.detail-grid {
  display: grid;
  gap: 0.85rem;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.json-box {
  background: #102033;
  border-radius: 10px;
  color: #ecf3fa;
  font-size: 0.82rem;
  margin: 0;
  max-height: 260px;
  overflow: auto;
  padding: 0.8rem;
}

.sms-box {
  background: rgba(10, 105, 95, 0.08);
  border: 1px solid rgba(10, 105, 95, 0.2);
  border-radius: 10px;
  padding: 0.7rem;
}

.payment-layout {
  display: grid;
  gap: 0.8rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.payment-card {
  display: grid;
  gap: 0.55rem;
  padding: 0.9rem;
}

.payment-card h4 {
  margin: 0;
}

.payment-form {
  display: grid;
  gap: 0.55rem;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 1100px) {
  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .payment-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2,
  .form-submit {
    grid-column: span 1;
  }

  .charge-row {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
