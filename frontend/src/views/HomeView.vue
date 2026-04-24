<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Portfolio Dashboard") }}</h2>
        <p>{{ i18n.t("Live operating view across occupancy, receivables, trends, and overdue exposure.") }}</p>
      </div>
      <div class="head-actions">
        <button class="btn btn-secondary" @click="loadDashboard" :disabled="loading">
          {{ loading ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
        </button>
        <router-link to="/invoices" class="btn">{{ i18n.t("Open Invoices") }}</router-link>
      </div>
    </header>

    <article class="surface panel stack-md">
      <div class="filters">
        <select v-model.number="filters.propertyId" class="select">
          <option :value="null">{{ i18n.t("All properties") }}</option>
          <option v-for="property in properties" :key="property.id" :value="property.id">
            {{ property.propertyName }}
          </option>
        </select>
        <input v-model="filters.fromMonth" class="input" type="month" />
        <input v-model="filters.toMonth" class="input" type="month" />
        <button class="btn btn-secondary" @click="loadDashboard" :disabled="loading">
          {{ i18n.t("Apply") }}
        </button>
        <button class="btn btn-ghost" @click="clearFilters" :disabled="loading">
          {{ i18n.t("Clear") }}
        </button>
      </div>
    </article>

    <p v-if="error" class="inline-error">{{ error }}</p>

    <section class="metrics-grid">
      <article class="surface metric-card">
        <p class="metric-label">{{ i18n.t("Properties") }}</p>
        <h3>{{ summary?.totalProperties ?? 0 }}</h3>
        <p class="muted">{{ i18n.t("Active properties in scope") }}</p>
      </article>

      <article class="surface metric-card">
        <p class="metric-label">{{ i18n.t("Occupancy") }}</p>
        <h3>{{ summary?.occupancyRatePercent ?? 0 }}%</h3>
        <p class="muted">
          {{ summary?.occupiedUnits ?? 0 }} {{ i18n.t("occupied of") }} {{ summary?.totalUnits ?? 0 }} {{ i18n.t("units") }}
        </p>
        <router-link class="inline-link" :to="{ path: '/tenants', query: tenantQuery() }">{{ i18n.t("Tenants") }}</router-link>
      </article>

      <article class="surface metric-card">
        <p class="metric-label">{{ i18n.t("Receivable") }}</p>
        <h3>{{ asMoney(summary?.receivableBdt ?? 0) }}</h3>
        <p class="muted">{{ i18n.t("Current outstanding invoice balance") }}</p>
        <router-link class="inline-link" :to="invoicesLink()">{{ i18n.t("Open Invoices") }}</router-link>
      </article>

      <article class="surface metric-card">
        <p class="metric-label">{{ i18n.t("Overdue Invoices") }}</p>
        <h3>{{ summary?.overdueInvoiceCount ?? 0 }}</h3>
        <p class="muted">{{ i18n.t("Total overdue amount") }}: {{ asMoney(overdueAging?.overdueAmountBdt ?? 0) }}</p>
        <router-link class="inline-link" :to="invoicesLink('OVERDUE')">{{ i18n.t("Open Invoices") }}</router-link>
      </article>
    </section>

    <div class="layout">
      <article class="surface panel stack-md">
        <div class="panel-head">
          <div>
            <h3>{{ i18n.t("12-Month Billing Trend") }}</h3>
            <p class="muted">{{ i18n.t("Due vs outstanding by billing month.") }}</p>
          </div>
          <span class="status-pill">{{ trend.length }} {{ i18n.t("months") }}</span>
        </div>

        <div v-if="trend.length" class="trend-list">
          <div v-for="point in trend" :key="point.month" class="trend-row">
            <div class="trend-head">
              <p>{{ monthLabel(point.month) }}</p>
              <p class="muted">{{ asMoney(point.totalDueBdt) }} {{ i18n.t("due") }} / {{ asMoney(point.outstandingBdt) }} {{ i18n.t("outstanding") }}</p>
            </div>
            <div class="trend-track">
              <span class="trend-total" :style="{ width: `${barWidth(point.totalDueBdt)}%` }" />
              <span class="trend-outstanding" :style="{ width: `${barWidth(point.outstandingBdt)}%` }" />
            </div>
          </div>
        </div>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No trend data") }}</h4>
          <p class="muted">{{ i18n.t("Generate invoices to populate monthly trend metrics.") }}</p>
        </div>
      </article>

      <article class="surface panel stack-md">
        <div class="panel-head">
          <div>
            <h3>{{ i18n.t("Overdue Aging") }}</h3>
            <p class="muted">{{ i18n.t("Balance concentration by days past due.") }}</p>
          </div>
          <span class="status-pill pending">{{ i18n.t("Risk View") }}</span>
        </div>

        <div v-if="overdueAging" class="aging-grid">
          <article v-for="bucket in overdueAging.buckets" :key="bucket.bucket" class="aging-card">
            <p class="metric-label">{{ bucket.bucket }}</p>
            <h4>{{ asMoney(bucket.amountBdt) }}</h4>
            <p class="muted">{{ bucket.invoiceCount }} {{ i18n.t("invoice(s)") }}</p>
          </article>
        </div>
      </article>
    </div>

    <article class="surface panel stack-md">
      <div class="panel-head">
        <div>
          <h3>{{ i18n.t("Property Breakdown") }}</h3>
          <p class="muted">{{ i18n.t("Per-property occupancy and receivable exposure.") }}</p>
        </div>
        <router-link to="/properties" class="btn btn-ghost btn-small">{{ i18n.t("Manage Properties") }}</router-link>
      </div>

      <div v-if="propertyBreakdown.length" class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ i18n.t("Property") }}</th>
              <th>{{ i18n.t("Units") }}</th>
              <th>{{ i18n.t("Occupancy") }}</th>
              <th>{{ i18n.t("Active Tenants") }}</th>
              <th>{{ i18n.t("Receivable") }}</th>
              <th>{{ i18n.t("Overdue Invoices") }}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in propertyBreakdown" :key="item.propertyId">
              <td>{{ item.propertyName }}</td>
              <td>{{ item.totalUnits }}</td>
              <td>{{ item.occupiedUnits }} / {{ item.totalUnits }}</td>
              <td>{{ item.activeTenants }}</td>
              <td>{{ asMoney(item.receivableBdt) }}</td>
              <td>
                <span class="status-pill" :class="item.overdueInvoiceCount > 0 ? 'pending' : ''">
                  {{ item.overdueInvoiceCount }}
                </span>
              </td>
              <td class="actions-cell">
                <div class="row-actions">
                  <router-link class="btn btn-secondary btn-small" :to="invoicesLink(null, item.propertyId)">
                    {{ i18n.t("Invoices") }}
                  </router-link>
                  <router-link class="btn btn-ghost btn-small" :to="{ path: '/tenants', query: { propertyId: String(item.propertyId) } }">
                    {{ i18n.t("Tenants") }}
                  </router-link>
                  <router-link class="btn btn-ghost btn-small" :to="{ path: '/deposits', query: { propertyId: String(item.propertyId) } }">
                    {{ i18n.t("Deposits") }}
                  </router-link>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state">
        <h4>{{ i18n.t("No property analytics yet") }}</h4>
        <p class="muted">{{ i18n.t("Create properties, tenants, and invoices to populate this section.") }}</p>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type DashboardSummary = {
  totalProperties: number;
  totalUnits: number;
  occupiedUnits: number;
  activeTenants: number;
  occupancyRatePercent: number;
  receivableBdt: number;
  overdueInvoiceCount: number;
};

type DashboardTrendPoint = {
  month: string;
  invoiceCount: number;
  totalDueBdt: number;
  collectedBdt: number;
  outstandingBdt: number;
};

type DashboardPropertyBreakdown = {
  propertyId: number;
  propertyName: string;
  totalUnits: number;
  occupiedUnits: number;
  activeTenants: number;
  receivableBdt: number;
  overdueInvoiceCount: number;
};

type DashboardOverdueBucket = {
  bucket: string;
  invoiceCount: number;
  amountBdt: number;
};

type DashboardOverdueAging = {
  buckets: DashboardOverdueBucket[];
  overdueInvoiceCount: number;
  overdueAmountBdt: number;
};

type Property = {
  id: number;
  propertyName: string;
};

const loading = ref(false);
const error = ref<string | null>(null);
const i18n = useI18n();
const properties = ref<Property[]>([]);
const filters = ref<{ propertyId: number | null; fromMonth: string; toMonth: string }>({
  propertyId: null,
  fromMonth: "",
  toMonth: ""
});

const summary = ref<DashboardSummary | null>(null);
const trend = ref<DashboardTrendPoint[]>([]);
const propertyBreakdown = ref<DashboardPropertyBreakdown[]>([]);
const overdueAging = ref<DashboardOverdueAging | null>(null);

const maxTrendAmount = computed(() => {
  const amounts = trend.value.map((item) => item.totalDueBdt);
  return amounts.length ? Math.max(...amounts, 1) : 1;
});

function asMoney(value: number) {
  return i18n.formatMoney(value);
}

function monthLabel(month: string) {
  return i18n.monthYearLabel(month);
}

function barWidth(value: number) {
  const max = maxTrendAmount.value;
  if (max <= 0) {
    return 0;
  }
  return Math.max(4, Math.round((value / max) * 100));
}

function dashboardParams() {
  const params: Record<string, string | number> = {};
  if (filters.value.propertyId) {
    params.propertyId = filters.value.propertyId;
  }
  if (filters.value.fromMonth) {
    params.fromMonth = filters.value.fromMonth;
  }
  if (filters.value.toMonth) {
    params.toMonth = filters.value.toMonth;
  }
  return params;
}

function invoicesLink(status?: string | null, propertyId?: number) {
  const query: Record<string, string> = {};
  const resolvedPropertyId = propertyId ?? filters.value.propertyId ?? null;
  if (resolvedPropertyId) {
    query.propertyId = String(resolvedPropertyId);
  }
  if (status) {
    query.status = status;
  }
  if (filters.value.fromMonth) {
    query.fromMonth = filters.value.fromMonth;
  }
  if (filters.value.toMonth) {
    query.toMonth = filters.value.toMonth;
  }
  return { path: "/invoices", query };
}

function tenantQuery() {
  if (filters.value.propertyId) {
    return { propertyId: String(filters.value.propertyId) };
  }
  return {};
}

async function loadProperties() {
  const response = await apiClient.get<ApiEnvelope<Property[]>>("/properties");
  properties.value = response.data.data;
}

function clearFilters() {
  filters.value = {
    propertyId: null,
    fromMonth: "",
    toMonth: ""
  };
  void loadDashboard();
}

async function loadDashboard() {
  loading.value = true;
  error.value = null;
  try {
    const params = dashboardParams();
    const [summaryResp, trendResp, breakdownResp, agingResp] = await Promise.all([
      apiClient.get<ApiEnvelope<DashboardSummary>>("/dashboard/summary", { params }),
      apiClient.get<ApiEnvelope<DashboardTrendPoint[]>>("/dashboard/trend", { params: { ...params, months: 12 } }),
      apiClient.get<ApiEnvelope<DashboardPropertyBreakdown[]>>("/dashboard/property-breakdown", { params }),
      apiClient.get<ApiEnvelope<DashboardOverdueAging>>("/dashboard/overdue-aging", { params })
    ]);

    summary.value = summaryResp.data.data;
    trend.value = trendResp.data.data;
    propertyBreakdown.value = breakdownResp.data.data;
    overdueAging.value = agingResp.data.data;
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to load dashboard analytics.");
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    await loadProperties();
    await loadDashboard();
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to load dashboard analytics.");
  }
});
</script>

<style scoped>
.head-actions {
  display: flex;
  gap: 0.5rem;
}

.metrics-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.filters {
  display: grid;
  gap: 0.55rem;
  grid-template-columns: 1fr 180px 180px auto auto;
}

.metric-card {
  display: grid;
  gap: 0.35rem;
  min-height: 122px;
  padding: 1rem;
}

.metric-label {
  color: #4f657d;
  font-size: 0.79rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.metric-card h3 {
  font-size: 1.55rem;
}

.inline-link {
  font-size: 0.86rem;
}

.layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
}

.panel {
  padding: 1.1rem;
}

.panel-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.trend-list {
  display: grid;
  gap: 0.65rem;
}

.trend-row {
  display: grid;
  gap: 0.36rem;
}

.trend-head {
  align-items: center;
  display: flex;
  justify-content: space-between;
}

.trend-track {
  background: rgba(214, 224, 235, 0.55);
  border-radius: 999px;
  height: 10px;
  overflow: hidden;
  position: relative;
}

.trend-total {
  background: linear-gradient(90deg, #4a95c9, #2f78aa);
  border-radius: 999px;
  display: block;
  height: 100%;
}

.trend-outstanding {
  background: linear-gradient(90deg, #c6653d, #b34d22);
  border-radius: 999px;
  display: block;
  height: 100%;
  left: 0;
  opacity: 0.92;
  position: absolute;
  top: 0;
}

.aging-grid {
  display: grid;
  gap: 0.7rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.aging-card {
  border: 1px solid var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.25rem;
  padding: 0.8rem;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 900px;
  width: 100%;
}

th,
td {
  border-bottom: 1px solid var(--border);
  padding: 0.65rem 0.45rem;
  text-align: left;
}

th {
  color: #4b6075;
  font-size: 0.81rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.btn-small {
  min-height: 32px;
  padding: 0.4rem 0.65rem;
}

.actions-cell {
  text-align: right;
}

.row-actions {
  display: inline-flex;
  gap: 0.35rem;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

@media (max-width: 1080px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filters {
    grid-template-columns: 1fr 1fr;
  }

  .layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 700px) {
  .head-actions {
    width: 100%;
  }

  .head-actions .btn,
  .head-actions a {
    width: 100%;
  }

  .metrics-grid,
  .aging-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    grid-template-columns: 1fr;
  }

  .trend-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 0.2rem;
  }
}
</style>
