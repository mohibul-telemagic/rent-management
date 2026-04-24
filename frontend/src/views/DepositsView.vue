<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Security Deposits") }}</h2>
        <p>{{ i18n.t("Manage deposit ledger entries, enforce refund bounds, and close tenancy with settlement records.") }}</p>
      </div>
      <button class="btn btn-secondary" @click="refreshAll" :disabled="loadingLedger">
        {{ loadingLedger ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
      </button>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div class="layout">
      <article class="surface panel stack-md">
        <div class="field">
          <label for="deposit-tenant">{{ i18n.t("Tenant") }}</label>
          <select id="deposit-tenant" v-model.number="selectedTenantId" class="select">
            <option :value="null">{{ i18n.t("Select tenant") }}</option>
            <option v-for="tenant in tenants" :key="tenant.id" :value="tenant.id">
              {{ tenant.fullName }} ({{ enumLabel(tenant.status) }})
            </option>
          </select>
        </div>

        <div v-if="ledger" class="balance-grid">
          <div class="metric-card">
            <p class="muted">{{ i18n.t("Opening") }}</p>
            <h3>{{ asMoney(ledger.balance.openingDepositBdt) }}</h3>
          </div>
          <div class="metric-card">
            <p class="muted">{{ i18n.t("Collected") }}</p>
            <h3>{{ asMoney(ledger.balance.collectedBdt) }}</h3>
          </div>
          <div class="metric-card">
            <p class="muted">{{ i18n.t("Deducted") }}</p>
            <h3>{{ asMoney(ledger.balance.deductedBdt) }}</h3>
          </div>
          <div class="metric-card">
            <p class="muted">{{ i18n.t("Refunded") }}</p>
            <h3>{{ asMoney(ledger.balance.refundedBdt) }}</h3>
          </div>
          <div class="metric-card strong">
            <p class="muted">{{ i18n.t("Current Balance") }}</p>
            <h3>{{ asMoney(ledger.balance.currentBalanceBdt) }}</h3>
            <span class="status-pill" :class="ledger.balance.securityDepositStatus === 'REFUNDED' ? 'inactive' : ''">
              {{ enumLabel(ledger.balance.securityDepositStatus) }}
            </span>
          </div>
        </div>

        <div v-if="selectedTenantId" class="stack-md">
          <article class="surface inner-card stack-md">
            <h3>{{ i18n.t("Add Ledger Transaction") }}</h3>
            <form class="form-grid" @submit.prevent="createTransaction">
              <div class="field">
                <label for="txn-type">{{ i18n.t("Transaction type") }}</label>
                <select id="txn-type" v-model="transactionForm.txnType" class="select" required>
                  <option value="COLLECTED">{{ enumLabel("COLLECTED") }}</option>
                  <option value="DEDUCTION">{{ enumLabel("DEDUCTION") }}</option>
                  <option value="REFUND">{{ enumLabel("REFUND") }}</option>
                </select>
              </div>

              <div class="field">
                <label for="txn-amount">{{ i18n.t("Amount (BDT)") }}</label>
                <input id="txn-amount" v-model="transactionForm.amountBdt" class="input" type="number" min="0.01" step="0.01" required />
              </div>

              <div class="field">
                <label for="txn-date">{{ i18n.t("Transaction date") }}</label>
                <input id="txn-date" v-model="transactionForm.transactionDate" class="input" type="date" required />
              </div>

              <div class="field form-span-2">
                <label for="txn-reason">{{ i18n.t("Reason (optional)") }}</label>
                <textarea id="txn-reason" v-model="transactionForm.reason" class="textarea" />
              </div>

              <button class="btn form-span-2" type="submit" :disabled="submittingTransaction">
                {{ submittingTransaction ? i18n.t("Saving...") : i18n.t("Save Transaction") }}
              </button>
            </form>
          </article>

          <article class="surface inner-card stack-md">
            <h3>{{ i18n.t("Move-out Settlement") }}</h3>
            <p class="muted">{{ i18n.t("Applies deduction and refund in one atomic operation. Active tenant will be deactivated.") }}</p>

            <form class="form-grid" @submit.prevent="settleMoveOut">
              <div class="field">
                <label for="moveout-deduction">{{ i18n.t("Deduction (BDT)") }}</label>
                <input id="moveout-deduction" v-model="settlementForm.deductionBdt" class="input" type="number" min="0" step="0.01" required />
              </div>

              <div class="field">
                <label for="moveout-refund">{{ i18n.t("Refund (BDT)") }}</label>
                <input id="moveout-refund" v-model="settlementForm.refundBdt" class="input" type="number" min="0" step="0.01" required />
              </div>

              <div class="field">
                <label for="moveout-date">{{ i18n.t("Move-out date") }}</label>
                <input id="moveout-date" v-model="settlementForm.moveOutDate" class="input" type="date" required />
              </div>

              <div class="field form-span-2">
                <label for="moveout-reason">{{ i18n.t("Reason (optional)") }}</label>
                <textarea id="moveout-reason" v-model="settlementForm.reason" class="textarea" />
              </div>

              <button class="btn btn-secondary form-span-2" type="submit" :disabled="submittingSettlement">
                {{ submittingSettlement ? i18n.t("Applying...") : i18n.t("Apply Move-out Settlement") }}
              </button>
            </form>
          </article>
        </div>

        <p v-if="actionMessage" class="muted">{{ actionMessage }}</p>
        <p v-if="actionError" class="inline-error">{{ actionError }}</p>
      </article>

      <article class="surface panel stack-md">
        <div class="section-head compact">
          <div>
            <h3>{{ i18n.t("Ledger") }}</h3>
            <p class="muted">{{ i18n.t("Chronological deposit transaction log for the selected tenant.") }}</p>
          </div>
          <span class="status-pill">{{ ledger?.transactions.length ?? 0 }} {{ i18n.t("entries") }}</span>
        </div>

        <div v-if="loadingLedger" class="muted">{{ i18n.t("Loading ledger...") }}</div>

        <div v-else-if="ledger?.transactions?.length" class="table-wrap">
          <table>
            <thead>
            <tr>
                <th>{{ i18n.t("Date") }}</th>
                <th>{{ i18n.t("Type") }}</th>
                <th>{{ i18n.t("Amount (BDT)") }}</th>
                <th>{{ i18n.t("Reason") }}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in ledger.transactions" :key="item.id">
                <td>{{ item.transactionDate }}</td>
                <td>
                  <span class="status-pill" :class="typeBadgeClass(item.txnType)">
                    {{ enumLabel(item.txnType) }}
                  </span>
                </td>
                <td>{{ asMoney(item.amountBdt) }}</td>
                <td>{{ item.reason || "-" }}</td>
                <td class="actions-cell">
                  <div class="row-actions">
                    <button
                      v-if="item.refundReceiptAvailable"
                      class="btn btn-ghost btn-small"
                      @click="downloadRefundReceipt(item.id, 'en')"
                      :disabled="fileActionLoading"
                    >
                      {{ i18n.t("Receipt EN") }}
                    </button>
                    <button
                      v-if="item.refundReceiptAvailable"
                      class="btn btn-ghost btn-small"
                      @click="downloadRefundReceipt(item.id, 'bn')"
                      :disabled="fileActionLoading"
                    >
                      {{ i18n.t("Receipt BN") }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No transactions") }}</h4>
          <p class="muted">{{ i18n.t("Select a tenant and add the first ledger entry.") }}</p>
        </div>

        <p v-if="fileActionMessage" class="muted">{{ fileActionMessage }}</p>
        <p v-if="fileActionError" class="inline-error">{{ fileActionError }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type Tenant = {
  id: number;
  fullName: string;
  status: string;
};

type DepositBalance = {
  tenantId: number;
  openingDepositBdt: number;
  collectedBdt: number;
  deductedBdt: number;
  refundedBdt: number;
  currentBalanceBdt: number;
  securityDepositStatus: string;
};

type DepositTransaction = {
  id: number;
  tenantId: number;
  tenantName: string;
  propertyUnitId: number;
  unitIdentifier: string;
  propertyId: number;
  propertyName: string;
  txnType: string;
  amountBdt: number;
  reason: string | null;
  transactionDate: string;
  refundReceiptAvailable: boolean;
};

type DepositLedger = {
  balance: DepositBalance;
  transactions: DepositTransaction[];
};

const tenants = ref<Tenant[]>([]);
const selectedTenantId = ref<number | null>(null);
const ledger = ref<DepositLedger | null>(null);

const loadingLedger = ref(false);
const submittingTransaction = ref(false);
const submittingSettlement = ref(false);
const fileActionLoading = ref(false);

const pageError = ref<string | null>(null);
const actionError = ref<string | null>(null);
const actionMessage = ref<string | null>(null);
const fileActionError = ref<string | null>(null);
const fileActionMessage = ref<string | null>(null);
const i18n = useI18n();
const route = useRoute();

const transactionForm = ref({
  txnType: "COLLECTED",
  amountBdt: "",
  transactionDate: todayIso(),
  reason: ""
});

const settlementForm = ref({
  deductionBdt: "0",
  refundBdt: "0",
  moveOutDate: todayIso(),
  reason: ""
});

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function enumLabel(value: string) {
  return i18n.translateEnum(value);
}

function asMoney(value: number) {
  return i18n.formatMoney(value);
}

function typeBadgeClass(type: string) {
  if (type === "DEDUCTION") {
    return "pending";
  }
  if (type === "REFUND") {
    return "inactive";
  }
  return "";
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

async function loadTenants() {
  const params: Record<string, number> = {};
  const propertyId = Number(route.query.propertyId);
  if (Number.isFinite(propertyId) && propertyId > 0) {
    params.propertyId = propertyId;
  }
  const response = await apiClient.get<ApiEnvelope<Tenant[]>>("/tenants", { params });
  tenants.value = response.data.data;

  const tenantIdFromQuery = Number(route.query.tenantId);
  if (Number.isFinite(tenantIdFromQuery) && tenantIdFromQuery > 0 && tenants.value.some((item) => item.id === tenantIdFromQuery)) {
    selectedTenantId.value = tenantIdFromQuery;
    return;
  }
  if (!selectedTenantId.value && tenants.value.length) {
    selectedTenantId.value = tenants.value[0].id;
  }
}

async function loadLedger() {
  if (!selectedTenantId.value) {
    ledger.value = null;
    return;
  }

  loadingLedger.value = true;
  actionError.value = null;
  try {
    const response = await apiClient.get<ApiEnvelope<DepositLedger>>(`/deposits/tenants/${selectedTenantId.value}/ledger`);
    ledger.value = response.data.data;
  } catch (error: any) {
    ledger.value = null;
    actionError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load deposit ledger.");
  } finally {
    loadingLedger.value = false;
  }
}

async function createTransaction() {
  if (!selectedTenantId.value) {
    actionError.value = i18n.t("Select a tenant first.");
    return;
  }

  const amountBdt = Number(transactionForm.value.amountBdt);
  if (!Number.isFinite(amountBdt) || amountBdt <= 0) {
    actionError.value = i18n.t("Transaction amount must be greater than 0.");
    return;
  }

  submittingTransaction.value = true;
  actionError.value = null;
  actionMessage.value = null;
  try {
    await apiClient.post(`/deposits/tenants/${selectedTenantId.value}/transactions`, {
      txnType: transactionForm.value.txnType,
      amountBdt,
      transactionDate: transactionForm.value.transactionDate,
      reason: transactionForm.value.reason.trim() || null
    });

    actionMessage.value = i18n.t("Deposit transaction saved.");
    transactionForm.value.amountBdt = "";
    transactionForm.value.reason = "";
    await loadLedger();
  } catch (error: any) {
    actionError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save deposit transaction.");
  } finally {
    submittingTransaction.value = false;
  }
}

async function settleMoveOut() {
  if (!selectedTenantId.value) {
    actionError.value = i18n.t("Select a tenant first.");
    return;
  }

  const deductionBdt = Number(settlementForm.value.deductionBdt);
  const refundBdt = Number(settlementForm.value.refundBdt);
  if (!Number.isFinite(deductionBdt) || deductionBdt < 0 || !Number.isFinite(refundBdt) || refundBdt < 0) {
    actionError.value = i18n.t("Settlement amounts must be zero or greater.");
    return;
  }
  if (deductionBdt === 0 && refundBdt === 0) {
    actionError.value = i18n.t("Provide deduction or refund amount.");
    return;
  }
  if (!window.confirm(i18n.t(
    "Apply move-out settlement for tenant #{tenantId}? Deduction: {deduction}, Refund: {refund}.",
    { tenantId: selectedTenantId.value, deduction: asMoney(deductionBdt), refund: asMoney(refundBdt) }
  ))) {
    return;
  }

  submittingSettlement.value = true;
  actionError.value = null;
  actionMessage.value = null;
  try {
    await apiClient.post(`/deposits/tenants/${selectedTenantId.value}/move-out-settlement`, {
      moveOutDate: settlementForm.value.moveOutDate,
      deductionBdt,
      refundBdt,
      reason: settlementForm.value.reason.trim() || null
    });

    actionMessage.value = i18n.t("Move-out settlement applied.");
    settlementForm.value.deductionBdt = "0";
    settlementForm.value.refundBdt = "0";
    settlementForm.value.reason = "";
    await Promise.all([loadLedger(), loadTenants()]);
  } catch (error: any) {
    actionError.value = error?.response?.data?.error?.message ?? i18n.t("Move-out settlement failed.");
  } finally {
    submittingSettlement.value = false;
  }
}

async function downloadRefundReceipt(transactionId: number, lang: "en" | "bn") {
  fileActionLoading.value = true;
  fileActionError.value = null;
  fileActionMessage.value = null;
  try {
    const response = await apiClient.get(`/deposits/${transactionId}/receipt-pdf`, {
      params: { lang },
      responseType: "blob"
    });
    triggerBrowserDownload(response.data, `deposit-refund-${transactionId}-${lang}.pdf`);
    if (response.headers["x-language-fallback"] === "en" && lang === "bn") {
      fileActionMessage.value = i18n.t("Bangla font unavailable, downloaded English fallback PDF.");
    }
  } catch (error: any) {
    fileActionError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download refund receipt.");
  } finally {
    fileActionLoading.value = false;
  }
}

async function refreshAll() {
  pageError.value = null;
  try {
    await loadTenants();
    await loadLedger();
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load deposit context.");
  }
}

watch(selectedTenantId, async () => {
  await loadLedger();
});

onMounted(refreshAll);
</script>

<style scoped>
.layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 420px) minmax(0, 1fr);
}

.panel {
  padding: 1.1rem;
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

.balance-grid {
  display: grid;
  gap: 0.65rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.metric-card {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.25rem;
  padding: 0.7rem;
}

.metric-card h3 {
  font-size: 1.1rem;
}

.metric-card.strong {
  background: linear-gradient(180deg, rgba(10, 105, 95, 0.14), rgba(10, 105, 95, 0.08));
  grid-column: span 2;
}

.inner-card {
  border-radius: 12px;
  box-shadow: none;
  padding: 0.9rem;
}

.form-grid {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-span-2 {
  grid-column: span 2;
}

.compact {
  align-items: flex-start;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 760px;
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

.actions-cell {
  width: 1%;
}

.row-actions {
  display: flex;
  gap: 0.35rem;
}

.btn-small {
  min-height: 32px;
  padding: 0.4rem 0.65rem;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 1040px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .balance-grid {
    grid-template-columns: 1fr;
  }

  .metric-card.strong {
    grid-column: span 1;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2 {
    grid-column: span 1;
  }
}
</style>
