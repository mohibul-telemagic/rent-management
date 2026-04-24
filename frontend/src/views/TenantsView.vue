<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Tenants") }}</h2>
        <p>{{ i18n.t("Create tenants with NID upload, then track occupancy and lease status from one view.") }}</p>
      </div>
      <button class="btn btn-secondary" @click="refreshAll" :disabled="loadingTenants">
        {{ loadingTenants ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
      </button>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div class="layout">
      <article class="surface panel stack-md">
        <div class="panel-head">
          <h3>{{ i18n.t("Add Tenant") }}</h3>
          <span class="status-pill">{{ i18n.t("Onboarding") }}</span>
        </div>

        <form class="form-grid" @submit.prevent="createTenant">
          <div class="field">
            <label for="tenant-name">{{ i18n.t("Full name") }}</label>
            <input id="tenant-name" v-model="createForm.fullName" class="input" required />
          </div>

          <div class="field">
            <label for="tenant-phone-primary">{{ i18n.t("Primary phone") }}</label>
            <input id="tenant-phone-primary" v-model="createForm.phonePrimary" class="input" placeholder="+8801XXXXXXXXX" required />
          </div>

          <div class="field">
            <label for="tenant-phone-secondary">{{ i18n.t("Secondary phone") }}</label>
            <input id="tenant-phone-secondary" v-model="createForm.phoneSecondary" class="input" placeholder="+8801XXXXXXXXX" />
          </div>

          <div class="field">
            <label for="tenant-nid-number">{{ i18n.t("NID number") }}</label>
            <input id="tenant-nid-number" v-model="createForm.nidNumber" class="input" maxlength="17" required />
          </div>

          <div class="field form-span-2">
            <label for="tenant-nid-image">{{ i18n.t("NID image (JPEG/PNG, max 5MB)") }}</label>
            <input id="tenant-nid-image" class="input" type="file" accept="image/jpeg,image/png" @change="onCreateNidSelected" />
          </div>

          <div class="field form-span-2">
            <label for="tenant-permanent-address">{{ i18n.t("Permanent address") }}</label>
            <textarea id="tenant-permanent-address" v-model="createForm.permanentAddress" class="textarea" required />
          </div>

          <div class="field form-span-2">
            <label for="tenant-current-address">{{ i18n.t("Current address") }}</label>
            <textarea id="tenant-current-address" v-model="createForm.currentAddress" class="textarea" />
          </div>

          <div class="field">
            <label for="tenant-emergency-name">{{ i18n.t("Emergency contact name") }}</label>
            <input id="tenant-emergency-name" v-model="createForm.emergencyContactName" class="input" required />
          </div>

          <div class="field">
            <label for="tenant-emergency-phone">{{ i18n.t("Emergency contact phone") }}</label>
            <input id="tenant-emergency-phone" v-model="createForm.emergencyContactPhone" class="input" placeholder="+8801XXXXXXXXX" required />
          </div>

          <div class="field">
            <label for="tenant-emergency-relation">{{ i18n.t("Emergency relation") }}</label>
            <select id="tenant-emergency-relation" v-model="createForm.emergencyContactRelation" class="select" required>
              <option v-for="relation in emergencyRelations" :key="relation" :value="relation">
                {{ enumLabel(relation) }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="tenant-dob">{{ i18n.t("Date of birth") }}</label>
            <input id="tenant-dob" v-model="createForm.dateOfBirth" class="input" type="date" />
          </div>

          <div class="field">
            <label for="tenant-lease-start">{{ i18n.t("Lease start date") }}</label>
            <input id="tenant-lease-start" v-model="createForm.leaseStartDate" class="input" type="date" required />
          </div>

          <div class="field">
            <label for="tenant-lease-end">{{ i18n.t("Lease end date") }}</label>
            <input id="tenant-lease-end" v-model="createForm.leaseEndDate" class="input" type="date" />
          </div>

          <div class="field">
            <label for="tenant-rent">{{ i18n.t("Monthly rent (BDT)") }}</label>
            <input id="tenant-rent" v-model="createForm.monthlyRentBdt" class="input" type="number" min="0.01" step="0.01" required />
          </div>

          <div class="field">
            <label for="tenant-deposit">{{ i18n.t("Security deposit (BDT)") }}</label>
            <input id="tenant-deposit" v-model="createForm.securityDepositBdt" class="input" type="number" min="0" step="0.01" required />
          </div>

          <div class="field">
            <label for="tenant-property">{{ i18n.t("Property") }}</label>
            <select id="tenant-property" v-model.number="selectedPropertyId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select property") }}</option>
              <option v-for="property in properties" :key="property.id" :value="property.id">
                {{ property.propertyName }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="tenant-unit">{{ i18n.t("Unit (vacant)") }}</label>
            <select id="tenant-unit" v-model.number="createForm.propertyUnitId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select unit") }}</option>
              <option v-for="unit in availableCreateUnits" :key="unit.id" :value="unit.id">
                {{ unit.unitIdentifier }} ({{ enumLabel(unit.occupancyStatus) }})
              </option>
            </select>
          </div>

          <div class="field form-span-2">
            <label for="tenant-notes">{{ i18n.t("Notes (optional)") }}</label>
            <textarea id="tenant-notes" v-model="createForm.notes" class="textarea" />
          </div>

          <button class="btn form-span-2" type="submit" :disabled="creatingTenant">
            {{ creatingTenant ? i18n.t("Creating...") : i18n.t("Create Tenant") }}
          </button>
        </form>

        <p v-if="createMessage" class="muted">{{ createMessage }}</p>
        <p v-if="createError" class="inline-error">{{ createError }}</p>
      </article>

      <article class="surface panel stack-md">
        <div class="table-head">
          <h3>{{ i18n.t("Tenant Registry") }}</h3>
          <span class="status-pill">{{ visibleTenants.length }} {{ i18n.t("records") }}</span>
        </div>

        <div class="filters">
          <input v-model.trim="query" class="input" type="search" :placeholder="i18n.t('Search by name, tenant ID, or unit ID')" />
          <select v-model="statusFilter" class="select">
            <option value="ALL">{{ i18n.t("All statuses") }}</option>
            <option value="ACTIVE">{{ enumLabel("ACTIVE") }}</option>
            <option value="INACTIVE">{{ enumLabel("INACTIVE") }}</option>
            <option value="EVICTED">{{ enumLabel("EVICTED") }}</option>
          </select>
        </div>

        <p v-if="listError" class="inline-error">{{ listError }}</p>
        <p v-else-if="loadingTenants" class="muted">{{ i18n.t("Loading tenant data...") }}</p>

        <div v-else-if="visibleTenants.length" class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>{{ i18n.t("ID") }}</th>
                <th>{{ i18n.t("Name") }}</th>
                <th>{{ i18n.t("Unit") }}</th>
                <th>{{ i18n.t("Rent") }}</th>
                <th>{{ i18n.t("Deposit") }}</th>
                <th>{{ i18n.t("Status") }}</th>
                <th>{{ i18n.t("NID") }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="tenant in visibleTenants"
                :key="tenant.id"
                :class="{ created: lastCreatedTenantId === tenant.id, selected: selectedTenantId === tenant.id }"
                @click="selectTenant(tenant.id)"
              >
                <td>#{{ tenant.id }}</td>
                <td>{{ tenant.fullName }}</td>
                <td>#{{ tenant.propertyUnitId }}</td>
                <td>{{ asMoney(tenant.monthlyRentBdt) }}</td>
                <td>{{ asMoney(tenant.securityDepositBdt) }}</td>
                <td>
                  <span class="status-pill" :class="badgeClass(tenant.status)">{{ enumLabel(tenant.status) }}</span>
                </td>
                <td>
                  <span class="status-pill" :class="tenant.hasNidImage ? '' : 'inactive'">
                    {{ tenant.hasNidImage ? i18n.t("Uploaded") : i18n.t("Pending") }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No tenants found") }}</h4>
          <p class="muted">{{ i18n.t("Create a tenant from the form and it will appear at the top automatically.") }}</p>
        </div>
      </article>
    </div>

    <article class="surface panel stack-md" v-if="selectedTenantId">
      <div class="panel-head">
        <h3>{{ i18n.t("Tenant") }} #{{ selectedTenantId }} {{ i18n.t("Details") }}</h3>
        <button class="btn btn-secondary btn-small" @click="reloadSelectedTenant" :disabled="loadingDetail">
          {{ loadingDetail ? i18n.t("Loading...") : i18n.t("Refresh") }}
        </button>
      </div>

      <p v-if="detailError" class="inline-error">{{ detailError }}</p>
      <p v-else-if="loadingDetail" class="muted">{{ i18n.t("Loading...") }}</p>

      <template v-else-if="detailForm">
        <div class="sub-layout">
          <article class="surface inner-card stack-md">
            <h4>{{ i18n.t("Profile") }}</h4>
            <form class="form-grid" @submit.prevent="saveTenantProfile">
              <div class="field">
                <label>{{ i18n.t("Full name") }}</label>
                <input v-model="detailForm.fullName" class="input" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Primary phone") }}</label>
                <input v-model="detailForm.phonePrimary" class="input" placeholder="+8801XXXXXXXXX" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Secondary phone") }}</label>
                <input v-model="detailForm.phoneSecondary" class="input" placeholder="+8801XXXXXXXXX" />
              </div>
              <div class="field">
                <label>{{ i18n.t("NID number") }}</label>
                <input v-model="detailForm.nidNumber" class="input" maxlength="17" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Date of birth") }}</label>
                <input v-model="detailForm.dateOfBirth" class="input" type="date" />
              </div>
              <div class="field">
                <label>{{ i18n.t("Status") }}</label>
                <select v-model="detailForm.status" class="select" required>
                  <option v-for="status in tenantStatuses" :key="status" :value="status">{{ enumLabel(status) }}</option>
                </select>
              </div>
              <div class="field form-span-2">
                <label>{{ i18n.t("Permanent address") }}</label>
                <textarea v-model="detailForm.permanentAddress" class="textarea" required />
              </div>
              <div class="field form-span-2">
                <label>{{ i18n.t("Current address") }}</label>
                <textarea v-model="detailForm.currentAddress" class="textarea" />
              </div>
              <div class="field">
                <label>{{ i18n.t("Emergency contact name") }}</label>
                <input v-model="detailForm.emergencyContactName" class="input" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Emergency contact phone") }}</label>
                <input v-model="detailForm.emergencyContactPhone" class="input" placeholder="+8801XXXXXXXXX" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Emergency relation") }}</label>
                <select v-model="detailForm.emergencyContactRelation" class="select" required>
                  <option v-for="relation in emergencyRelations" :key="relation" :value="relation">{{ enumLabel(relation) }}</option>
                </select>
              </div>
              <div class="field">
                <label>{{ i18n.t("Monthly rent (BDT)") }}</label>
                <input v-model="detailForm.monthlyRentBdt" class="input" type="number" min="0.01" step="0.01" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Security deposit (BDT)") }}</label>
                <input v-model="detailForm.securityDepositBdt" class="input" type="number" min="0" step="0.01" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Lease start date") }}</label>
                <input v-model="detailForm.leaseStartDate" class="input" type="date" required />
              </div>
              <div class="field">
                <label>{{ i18n.t("Lease end date") }}</label>
                <input v-model="detailForm.leaseEndDate" class="input" type="date" />
              </div>
              <div class="field form-span-2">
                <label>{{ i18n.t("Notes (optional)") }}</label>
                <textarea v-model="detailForm.notes" class="textarea" />
              </div>
              <button class="btn form-span-2" type="submit" :disabled="savingDetail">
                {{ savingDetail ? i18n.t("Saving...") : i18n.t("Save Profile") }}
              </button>
            </form>
          </article>

          <article class="surface inner-card stack-md">
            <h4>{{ i18n.t("Lifecycle Actions") }}</h4>

            <form class="stack-sm" @submit.prevent="moveTenantUnit">
              <p class="muted">{{ i18n.t("Move Unit") }}</p>
              <select v-model.number="moveForm.propertyId" class="select" required>
                <option :value="null" disabled>{{ i18n.t("Select property") }}</option>
                <option v-for="property in properties" :key="property.id" :value="property.id">{{ property.propertyName }}</option>
              </select>
              <select v-model.number="moveForm.unitId" class="select" required>
                <option :value="null" disabled>{{ i18n.t("Select unit") }}</option>
                <option v-for="unit in availableMoveUnits" :key="unit.id" :value="unit.id">
                  {{ unit.unitIdentifier }} ({{ enumLabel(unit.occupancyStatus) }})
                </option>
              </select>
              <button class="btn btn-secondary" type="submit" :disabled="savingDetail">{{ i18n.t("Move Unit") }}</button>
            </form>

            <form class="stack-sm" @submit.prevent="renewLease">
              <p class="muted">{{ i18n.t("Lease Renewal") }}</p>
              <input v-model="renewalEndDate" class="input" type="date" required />
              <button class="btn btn-secondary" type="submit" :disabled="savingDetail">{{ i18n.t("Renew Lease") }}</button>
            </form>

            <form class="stack-sm" @submit.prevent="uploadNidImage">
              <p class="muted">{{ i18n.t("NID Document") }}</p>
              <input class="input" type="file" accept="image/jpeg,image/png" @change="onDetailNidSelected" />
              <div class="row-actions">
                <button class="btn btn-secondary btn-small" type="submit" :disabled="savingNid">{{ i18n.t("Re-upload NID") }}</button>
                <button class="btn btn-ghost btn-small" type="button" @click="previewNidImage">{{ i18n.t("Preview NID") }}</button>
                <button class="btn btn-ghost btn-small" type="button" @click="downloadNidImage">{{ i18n.t("Download NID") }}</button>
              </div>
            </form>

            <button class="btn btn-ghost danger" type="button" @click="deactivateTenant" :disabled="savingDetail">
              {{ i18n.t("Deactivate Tenant") }}
            </button>
          </article>
        </div>

        <article class="surface inner-card stack-md">
          <h4>{{ i18n.t("Financial Ledger") }}</h4>
          <div class="ledger-summary" v-if="tenantLedger">
            <div class="summary-item">
              <p class="muted">{{ i18n.t("Total Invoiced") }}</p>
              <strong>{{ asMoney(tenantLedger.totalInvoicedBdt) }}</strong>
            </div>
            <div class="summary-item">
              <p class="muted">{{ i18n.t("Outstanding") }}</p>
              <strong>{{ asMoney(tenantLedger.outstandingBdt) }}</strong>
            </div>
            <div class="summary-item">
              <p class="muted">{{ i18n.t("Total Paid") }}</p>
              <strong>{{ asMoney(tenantLedger.totalPaidBdt) }}</strong>
            </div>
            <div class="summary-item">
              <p class="muted">{{ i18n.t("Deposit Net") }}</p>
              <strong>{{ asMoney(tenantLedger.totalDepositNetBdt) }}</strong>
            </div>
          </div>

          <div class="table-wrap" v-if="tenantLedger?.invoices?.length">
            <table>
              <thead>
                <tr>
                  <th>{{ i18n.t("Invoice") }}</th>
                  <th>{{ i18n.t("Period") }}</th>
                  <th>{{ i18n.t("Due Date") }}</th>
                  <th>{{ i18n.t("Status") }}</th>
                  <th>{{ i18n.t("Total Due") }}</th>
                  <th>{{ i18n.t("Balance Due") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="invoice in tenantLedger.invoices" :key="invoice.invoiceId">
                  <td>#{{ invoice.invoiceId }}</td>
                  <td>{{ invoice.billingPeriodStart }} - {{ invoice.billingPeriodEnd }}</td>
                  <td>{{ invoice.dueDate }}</td>
                  <td>{{ enumLabel(invoice.status) }}</td>
                  <td>{{ asMoney(invoice.totalDueBdt) }}</td>
                  <td>{{ asMoney(invoice.balanceDueBdt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="table-wrap" v-if="tenantLedger?.payments?.length">
            <table>
              <thead>
                <tr>
                  <th>{{ i18n.t("Payment") }}</th>
                  <th>{{ i18n.t("Invoice") }}</th>
                  <th>{{ i18n.t("Date") }}</th>
                  <th>{{ i18n.t("Method") }}</th>
                  <th>{{ i18n.t("Amount (BDT)") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="payment in tenantLedger.payments" :key="payment.paymentId">
                  <td>#{{ payment.paymentId }}</td>
                  <td>#{{ payment.invoiceId }}</td>
                  <td>{{ payment.paymentDate }}</td>
                  <td>{{ enumLabel(payment.paymentMethod) }}</td>
                  <td>{{ asMoney(payment.amountBdt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="table-wrap" v-if="tenantLedger?.deposits?.length">
            <table>
              <thead>
                <tr>
                  <th>{{ i18n.t("Deposit") }}</th>
                  <th>{{ i18n.t("Date") }}</th>
                  <th>{{ i18n.t("Type") }}</th>
                  <th>{{ i18n.t("Amount (BDT)") }}</th>
                  <th>{{ i18n.t("Reason") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="deposit in tenantLedger.deposits" :key="deposit.transactionId">
                  <td>#{{ deposit.transactionId }}</td>
                  <td>{{ deposit.transactionDate }}</td>
                  <td>{{ enumLabel(deposit.txnType) }}</td>
                  <td>{{ asMoney(deposit.amountBdt) }}</td>
                  <td>{{ deposit.reason || "-" }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </article>

        <article class="surface inner-card stack-md">
          <h4>{{ i18n.t("Assignment History") }}</h4>
          <div class="table-wrap" v-if="tenantHistory.length">
            <table>
              <thead>
                <tr>
                  <th>{{ i18n.t("Unit") }}</th>
                  <th>{{ i18n.t("Start Date") }}</th>
                  <th>{{ i18n.t("End Date") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="history in tenantHistory" :key="history.id">
                  <td>#{{ history.propertyUnitId }}</td>
                  <td>{{ history.startDate }}</td>
                  <td>{{ history.endDate || "-" }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="muted">{{ i18n.t("No records") }}</p>
        </article>

        <article class="surface inner-card stack-md">
          <h4>{{ i18n.t("Activity Timeline") }}</h4>
          <div class="table-wrap" v-if="tenantActivity.length">
            <table>
              <thead>
                <tr>
                  <th>{{ i18n.t("Created") }}</th>
                  <th>{{ i18n.t("Action") }}</th>
                  <th>{{ i18n.t("Actor") }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="event in tenantActivity" :key="event.id">
                  <td>{{ event.createdAt }}</td>
                  <td>{{ event.action }}</td>
                  <td>#{{ event.actorUserId }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <p v-else class="muted">{{ i18n.t("No records") }}</p>
        </article>

        <p v-if="detailMessage" class="muted">{{ detailMessage }}</p>
      </template>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type Property = {
  id: number;
  propertyName: string;
};

type Unit = {
  id: number;
  propertyId: number;
  unitIdentifier: string;
  occupancyStatus: string;
};

type TenantListItem = {
  id: number;
  fullName: string;
  propertyUnitId: number;
  monthlyRentBdt: number;
  securityDepositBdt: number;
  status: string;
  hasNidImage: boolean;
};

type TenantDetail = {
  id: number;
  fullName: string;
  phonePrimary: string;
  phoneSecondary: string | null;
  nidNumber: string;
  dateOfBirth: string | null;
  permanentAddress: string;
  currentAddress: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  emergencyContactRelation: string;
  leaseStartDate: string;
  leaseEndDate: string | null;
  monthlyRentBdt: number;
  securityDepositBdt: number;
  securityDepositStatus: string;
  status: string;
  hasNidImage: boolean;
  notes: string | null;
  propertyUnitId: number;
};

type TenantHistory = {
  id: number;
  tenantId: number;
  propertyUnitId: number;
  startDate: string;
  endDate: string | null;
};

type TenantLedger = {
  tenantId: number;
  totalInvoicedBdt: number;
  outstandingBdt: number;
  totalPaidBdt: number;
  totalDepositNetBdt: number;
  invoices: Array<{
    invoiceId: number;
    billingPeriodStart: string;
    billingPeriodEnd: string;
    dueDate: string;
    status: string;
    totalDueBdt: number;
    balanceDueBdt: number;
  }>;
  payments: Array<{
    paymentId: number;
    invoiceId: number;
    paymentDate: string;
    amountBdt: number;
    paymentMethod: string;
    notes: string | null;
  }>;
  deposits: Array<{
    transactionId: number;
    transactionDate: string;
    txnType: string;
    amountBdt: number;
    reason: string | null;
  }>;
};

type AuditTimelineItem = {
  id: number;
  actorUserId: number;
  action: string;
  entityType: string;
  entityId: string;
  createdAt: string;
};

type TenantUpdatePayload = {
  fullName: string;
  phonePrimary: string;
  phoneSecondary: string | null;
  nidNumber: string;
  dateOfBirth: string | null;
  permanentAddress: string;
  currentAddress: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  emergencyContactRelation: string;
  leaseStartDate: string;
  leaseEndDate: string | null;
  monthlyRentBdt: number;
  securityDepositBdt: number;
  status: string;
  notes: string | null;
  propertyUnitId: number;
};

const i18n = useI18n();
const route = useRoute();
const emergencyRelations = ["FATHER", "MOTHER", "SPOUSE", "SIBLING", "FRIEND", "OTHER"];
const tenantStatuses = ["ACTIVE", "INACTIVE", "EVICTED"];

const properties = ref<Property[]>([]);
const createUnits = ref<Unit[]>([]);
const allUnits = ref<Unit[]>([]);
const tenants = ref<TenantListItem[]>([]);

const loadingTenants = ref(false);
const loadingDetail = ref(false);
const creatingTenant = ref(false);
const savingDetail = ref(false);
const savingNid = ref(false);

const pageError = ref<string | null>(null);
const listError = ref<string | null>(null);
const createError = ref<string | null>(null);
const createMessage = ref<string | null>(null);
const detailError = ref<string | null>(null);
const detailMessage = ref<string | null>(null);

const query = ref("");
const statusFilter = ref("ALL");
const selectedPropertyId = ref<number | null>(null);
const createNidImageFile = ref<File | null>(null);
const detailNidImageFile = ref<File | null>(null);
const lastCreatedTenantId = ref<number | null>(null);
const selectedTenantId = ref<number | null>(null);

const createForm = ref({
  fullName: "",
  phonePrimary: "",
  phoneSecondary: "",
  nidNumber: "",
  dateOfBirth: "",
  permanentAddress: "",
  currentAddress: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
  emergencyContactRelation: "FATHER",
  leaseStartDate: todayIso(),
  leaseEndDate: "",
  monthlyRentBdt: "",
  securityDepositBdt: "",
  notes: "",
  propertyUnitId: null as number | null
});

const detailForm = ref<null | {
  fullName: string;
  phonePrimary: string;
  phoneSecondary: string;
  nidNumber: string;
  dateOfBirth: string;
  permanentAddress: string;
  currentAddress: string;
  emergencyContactName: string;
  emergencyContactPhone: string;
  emergencyContactRelation: string;
  leaseStartDate: string;
  leaseEndDate: string;
  monthlyRentBdt: string;
  securityDepositBdt: string;
  status: string;
  notes: string;
  propertyUnitId: number;
}>(null);

const moveForm = ref<{ propertyId: number | null; unitId: number | null }>({
  propertyId: null,
  unitId: null
});

const renewalEndDate = ref("");
const tenantHistory = ref<TenantHistory[]>([]);
const tenantActivity = ref<AuditTimelineItem[]>([]);
const tenantLedger = ref<TenantLedger | null>(null);

const availableCreateUnits = computed(() => createUnits.value.filter((unit) => unit.occupancyStatus !== "OCCUPIED"));

const availableMoveUnits = computed(() => {
  if (!moveForm.value.propertyId || !detailForm.value) {
    return [];
  }
  return allUnits.value.filter((unit) => {
    if (unit.propertyId !== moveForm.value.propertyId) {
      return false;
    }
    return unit.occupancyStatus !== "OCCUPIED" || unit.id === detailForm.value.propertyUnitId;
  });
});

const visibleTenants = computed(() => {
  const sorted = [...tenants.value].sort((a, b) => b.id - a.id);
  const q = query.value.toLowerCase();
  return sorted.filter((tenant) => {
    const matchesStatus = statusFilter.value === "ALL" || tenant.status === statusFilter.value;
    if (!matchesStatus) {
      return false;
    }
    if (!q) {
      return true;
    }
    return tenant.fullName.toLowerCase().includes(q)
      || String(tenant.id).includes(q)
      || String(tenant.propertyUnitId).includes(q);
  });
});

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function enumLabel(status: string) {
  return i18n.translateEnum(status);
}

function asMoney(value: number) {
  return i18n.formatMoney(value);
}

function badgeClass(status: string) {
  if (status === "INACTIVE" || status === "EVICTED") {
    return "inactive";
  }
  return "";
}

function onCreateNidSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  createNidImageFile.value = input.files?.[0] ?? null;
}

function onDetailNidSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  detailNidImageFile.value = input.files?.[0] ?? null;
}

function resetCreateForm() {
  createForm.value = {
    fullName: "",
    phonePrimary: "",
    phoneSecondary: "",
    nidNumber: "",
    dateOfBirth: "",
    permanentAddress: "",
    currentAddress: "",
    emergencyContactName: "",
    emergencyContactPhone: "",
    emergencyContactRelation: "FATHER",
    leaseStartDate: todayIso(),
    leaseEndDate: "",
    monthlyRentBdt: "",
    securityDepositBdt: "",
    notes: "",
    propertyUnitId: null
  };
  createNidImageFile.value = null;
  createError.value = null;
}

function validateCreateForm() {
  if (!selectedPropertyId.value) {
    throw new Error(i18n.t("Property is required."));
  }
  if (!createForm.value.propertyUnitId) {
    throw new Error(i18n.t("Unit is required."));
  }
  if (!createForm.value.fullName.trim()) {
    throw new Error(i18n.t("Full name is required."));
  }
  if (!createForm.value.phonePrimary.trim()) {
    throw new Error(i18n.t("Primary phone is required."));
  }
  if (!createForm.value.nidNumber.trim()) {
    throw new Error(i18n.t("NID number is required."));
  }
  if (!createForm.value.leaseStartDate) {
    throw new Error(i18n.t("Lease start date is required."));
  }
  const rent = Number(createForm.value.monthlyRentBdt);
  if (!Number.isFinite(rent) || rent <= 0) {
    throw new Error(i18n.t("Monthly rent must be greater than 0."));
  }
  const deposit = Number(createForm.value.securityDepositBdt);
  if (!Number.isFinite(deposit) || deposit < 0) {
    throw new Error(i18n.t("Security deposit must be zero or greater."));
  }
}

function ensureTenantSelected(): number {
  if (!selectedTenantId.value) {
    throw new Error(i18n.t("Select a tenant."));
  }
  return selectedTenantId.value;
}

function buildDetailPayload(overrides?: Partial<TenantUpdatePayload>): TenantUpdatePayload {
  if (!detailForm.value) {
    throw new Error(i18n.t("Select a tenant."));
  }
  const monthlyRentBdt = Number(detailForm.value.monthlyRentBdt);
  const securityDepositBdt = Number(detailForm.value.securityDepositBdt);
  if (!Number.isFinite(monthlyRentBdt) || monthlyRentBdt <= 0) {
    throw new Error(i18n.t("Monthly rent must be greater than 0."));
  }
  if (!Number.isFinite(securityDepositBdt) || securityDepositBdt < 0) {
    throw new Error(i18n.t("Security deposit must be zero or greater."));
  }
  const payload: TenantUpdatePayload = {
    fullName: detailForm.value.fullName.trim(),
    phonePrimary: detailForm.value.phonePrimary.trim(),
    phoneSecondary: detailForm.value.phoneSecondary.trim() || null,
    nidNumber: detailForm.value.nidNumber.trim(),
    dateOfBirth: detailForm.value.dateOfBirth || null,
    permanentAddress: detailForm.value.permanentAddress.trim(),
    currentAddress: detailForm.value.currentAddress.trim(),
    emergencyContactName: detailForm.value.emergencyContactName.trim(),
    emergencyContactPhone: detailForm.value.emergencyContactPhone.trim(),
    emergencyContactRelation: detailForm.value.emergencyContactRelation,
    leaseStartDate: detailForm.value.leaseStartDate,
    leaseEndDate: detailForm.value.leaseEndDate || null,
    monthlyRentBdt,
    securityDepositBdt,
    status: detailForm.value.status,
    notes: detailForm.value.notes.trim() || null,
    propertyUnitId: detailForm.value.propertyUnitId
  };
  return { ...payload, ...overrides };
}

async function loadProperties() {
  const response = await apiClient.get<ApiEnvelope<Property[]>>("/properties");
  properties.value = response.data.data;
}

async function loadCreateUnits(propertyId: number) {
  const response = await apiClient.get<ApiEnvelope<Unit[]>>(`/properties/${propertyId}/units`);
  createUnits.value = response.data.data.map((unit) => ({ ...unit, propertyId }));
}

async function loadAllUnits() {
  const unitResponses = await Promise.all(
    properties.value.map((property) =>
      apiClient.get<ApiEnvelope<Unit[]>>(`/properties/${property.id}/units`)
        .then((response) => response.data.data.map((unit) => ({ ...unit, propertyId: property.id })))
    )
  );
  allUnits.value = unitResponses.flat();
}

async function loadTenants() {
  loadingTenants.value = true;
  listError.value = null;
  try {
    const params: Record<string, number> = {};
    if (selectedPropertyId.value) {
      params.propertyId = selectedPropertyId.value;
    }
    const response = await apiClient.get<ApiEnvelope<TenantListItem[]>>("/tenants", { params });
    tenants.value = [...response.data.data].sort((a, b) => b.id - a.id);
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load tenants.");
  } finally {
    loadingTenants.value = false;
  }
}

function applyTenantDetail(detail: TenantDetail) {
  detailForm.value = {
    fullName: detail.fullName,
    phonePrimary: detail.phonePrimary,
    phoneSecondary: detail.phoneSecondary ?? "",
    nidNumber: detail.nidNumber,
    dateOfBirth: detail.dateOfBirth ?? "",
    permanentAddress: detail.permanentAddress,
    currentAddress: detail.currentAddress ?? "",
    emergencyContactName: detail.emergencyContactName,
    emergencyContactPhone: detail.emergencyContactPhone,
    emergencyContactRelation: detail.emergencyContactRelation,
    leaseStartDate: detail.leaseStartDate,
    leaseEndDate: detail.leaseEndDate ?? "",
    monthlyRentBdt: String(detail.monthlyRentBdt),
    securityDepositBdt: String(detail.securityDepositBdt),
    status: detail.status,
    notes: detail.notes ?? "",
    propertyUnitId: detail.propertyUnitId
  };

  const currentUnit = allUnits.value.find((unit) => unit.id === detail.propertyUnitId);
  moveForm.value.propertyId = currentUnit?.propertyId ?? null;
  moveForm.value.unitId = detail.propertyUnitId;
  renewalEndDate.value = detail.leaseEndDate ?? "";
}

async function loadTenantDetail(tenantId: number) {
  const response = await apiClient.get<ApiEnvelope<TenantDetail>>(`/tenants/${tenantId}`);
  applyTenantDetail(response.data.data);
}

async function loadTenantHistory(tenantId: number) {
  const response = await apiClient.get<ApiEnvelope<TenantHistory[]>>(`/tenants/${tenantId}/history`);
  tenantHistory.value = response.data.data;
}

async function loadTenantActivity(tenantId: number) {
  const response = await apiClient.get<ApiEnvelope<AuditTimelineItem[]>>(`/tenants/${tenantId}/activity`);
  tenantActivity.value = response.data.data;
}

async function loadTenantLedger(tenantId: number) {
  const response = await apiClient.get<ApiEnvelope<TenantLedger>>(`/tenants/${tenantId}/ledger`);
  tenantLedger.value = response.data.data;
}

async function reloadSelectedTenant() {
  if (!selectedTenantId.value) {
    return;
  }
  loadingDetail.value = true;
  detailError.value = null;
  try {
    await Promise.all([
      loadTenantDetail(selectedTenantId.value),
      loadTenantHistory(selectedTenantId.value),
      loadTenantActivity(selectedTenantId.value),
      loadTenantLedger(selectedTenantId.value)
    ]);
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load tenant workspace.");
  } finally {
    loadingDetail.value = false;
  }
}

async function selectTenant(tenantId: number) {
  selectedTenantId.value = tenantId;
  detailMessage.value = null;
  detailNidImageFile.value = null;
  await reloadSelectedTenant();
}

async function createTenant() {
  try {
    validateCreateForm();
  } catch (error: any) {
    createError.value = error.message ?? i18n.t("Please check input values.");
    return;
  }

  creatingTenant.value = true;
  createError.value = null;
  createMessage.value = null;
  try {
    const formData = new FormData();
    formData.append("fullName", createForm.value.fullName.trim());
    formData.append("phonePrimary", createForm.value.phonePrimary.trim());
    if (createForm.value.phoneSecondary.trim()) {
      formData.append("phoneSecondary", createForm.value.phoneSecondary.trim());
    }
    formData.append("nidNumber", createForm.value.nidNumber.trim());
    if (createNidImageFile.value) {
      formData.append("nidImage", createNidImageFile.value);
    }
    if (createForm.value.dateOfBirth) {
      formData.append("dateOfBirth", createForm.value.dateOfBirth);
    }
    formData.append("permanentAddress", createForm.value.permanentAddress.trim());
    if (createForm.value.currentAddress.trim()) {
      formData.append("currentAddress", createForm.value.currentAddress.trim());
    }
    formData.append("emergencyContactName", createForm.value.emergencyContactName.trim());
    formData.append("emergencyContactPhone", createForm.value.emergencyContactPhone.trim());
    formData.append("emergencyContactRelation", createForm.value.emergencyContactRelation);
    formData.append("leaseStartDate", createForm.value.leaseStartDate);
    if (createForm.value.leaseEndDate) {
      formData.append("leaseEndDate", createForm.value.leaseEndDate);
    }
    formData.append("monthlyRentBdt", String(Number(createForm.value.monthlyRentBdt)));
    formData.append("securityDepositBdt", String(Number(createForm.value.securityDepositBdt)));
    if (createForm.value.notes.trim()) {
      formData.append("notes", createForm.value.notes.trim());
    }
    formData.append("propertyUnitId", String(createForm.value.propertyUnitId));

    const response = await apiClient.post<ApiEnvelope<TenantListItem>>("/tenants", formData);
    const created = response.data.data;
    lastCreatedTenantId.value = created.id;
    query.value = String(created.id);
    statusFilter.value = "ALL";
    createMessage.value = i18n.t("Tenant #{id} created successfully and pinned in search.", { id: created.id });
    resetCreateForm();
    await Promise.all([loadTenants(), loadAllUnits()]);
    await selectTenant(created.id);
  } catch (error: any) {
    createError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to create tenant.");
  } finally {
    creatingTenant.value = false;
  }
}

async function submitTenantUpdate(payload: TenantUpdatePayload, successMessage: string) {
  const tenantId = ensureTenantSelected();
  savingDetail.value = true;
  detailError.value = null;
  detailMessage.value = null;
  try {
    await apiClient.put(`/tenants/${tenantId}`, payload);
    detailMessage.value = successMessage;
    await Promise.all([loadTenants(), loadAllUnits(), reloadSelectedTenant()]);
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save tenant.");
  } finally {
    savingDetail.value = false;
  }
}

async function saveTenantProfile() {
  try {
    const payload = buildDetailPayload();
    await submitTenantUpdate(payload, i18n.t("Tenant profile saved."));
  } catch (error: any) {
    detailError.value = error.message ?? i18n.t("Please check your input.");
  }
}

async function moveTenantUnit() {
  if (!moveForm.value.unitId) {
    detailError.value = i18n.t("Unit is required.");
    return;
  }
  try {
    const payload = buildDetailPayload({ propertyUnitId: moveForm.value.unitId });
    await submitTenantUpdate(payload, i18n.t("Tenant moved to new unit."));
  } catch (error: any) {
    detailError.value = error.message ?? i18n.t("Please check your input.");
  }
}

async function renewLease() {
  if (!renewalEndDate.value) {
    detailError.value = i18n.t("Lease end date is required.");
    return;
  }
  try {
    const payload = buildDetailPayload({ leaseEndDate: renewalEndDate.value, status: "ACTIVE" });
    await submitTenantUpdate(payload, i18n.t("Lease renewed."));
  } catch (error: any) {
    detailError.value = error.message ?? i18n.t("Please check your input.");
  }
}

async function deactivateTenant() {
  const tenantId = selectedTenantId.value;
  if (!tenantId) {
    return;
  }
  const tenantName = detailForm.value?.fullName ?? `#${tenantId}`;
  if (!window.confirm(i18n.t("Deactivate tenant {tenant}? This will mark lease inactive and free the unit.", { tenant: tenantName }))) {
    return;
  }
  savingDetail.value = true;
  detailError.value = null;
  detailMessage.value = null;
  try {
    await apiClient.delete(`/tenants/${tenantId}`);
    detailMessage.value = i18n.t("Tenant deactivated.");
    await Promise.all([loadTenants(), loadAllUnits()]);
    await reloadSelectedTenant();
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to deactivate tenant.");
  } finally {
    savingDetail.value = false;
  }
}

async function uploadNidImage() {
  const tenantId = selectedTenantId.value;
  if (!tenantId) {
    return;
  }
  if (!detailNidImageFile.value) {
    detailError.value = i18n.t("NID image is required.");
    return;
  }
  savingNid.value = true;
  detailError.value = null;
  detailMessage.value = null;
  try {
    const formData = new FormData();
    formData.append("nidImage", detailNidImageFile.value);
    await apiClient.put(`/tenants/${tenantId}/nid-image`, formData);
    detailNidImageFile.value = null;
    detailMessage.value = i18n.t("NID image updated.");
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to update NID image.");
  } finally {
    savingNid.value = false;
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

function openBlobPreview(data: Blob) {
  const url = URL.createObjectURL(data);
  window.open(url, "_blank", "noopener");
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

async function previewNidImage() {
  const tenantId = selectedTenantId.value;
  if (!tenantId) {
    return;
  }
  detailError.value = null;
  try {
    const response = await apiClient.get(`/tenants/${tenantId}/nid-image`, { responseType: "blob" });
    openBlobPreview(response.data);
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download NID image.");
  }
}

async function downloadNidImage() {
  const tenantId = selectedTenantId.value;
  if (!tenantId) {
    return;
  }
  detailError.value = null;
  try {
    const response = await apiClient.get(`/tenants/${tenantId}/nid-image`, { responseType: "blob" });
    const mime = (response.headers["content-type"] as string) ?? "image/jpeg";
    const ext = mime.includes("png") ? "png" : "jpg";
    triggerBrowserDownload(response.data, `tenant-${tenantId}-nid.${ext}`);
  } catch (error: any) {
    detailError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download NID image.");
  }
}

async function refreshAll() {
  pageError.value = null;
  try {
    await loadProperties();
    await Promise.all([loadTenants(), loadAllUnits()]);
    if (selectedPropertyId.value) {
      await loadCreateUnits(selectedPropertyId.value);
    }
    if (selectedTenantId.value) {
      await reloadSelectedTenant();
    }
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load tenant workspace.");
  }
}

function applyRouteFilters() {
  const propertyId = Number(route.query.propertyId);
  const tenantId = Number(route.query.tenantId);
  if (Number.isFinite(propertyId) && propertyId > 0) {
    selectedPropertyId.value = propertyId;
  }
  if (Number.isFinite(tenantId) && tenantId > 0) {
    selectedTenantId.value = tenantId;
    query.value = String(tenantId);
  }
}

watch(selectedPropertyId, async (propertyId) => {
  createForm.value.propertyUnitId = null;
  createUnits.value = [];
  await loadTenants();
  if (!propertyId) {
    return;
  }
  try {
    await loadCreateUnits(propertyId);
  } catch (error: any) {
    createError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load units for selected property.");
  }
});

onMounted(async () => {
  applyRouteFilters();
  await refreshAll();
  if (selectedTenantId.value) {
    await selectTenant(selectedTenantId.value);
  }
});
</script>

<style scoped>
.layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.sub-layout {
  display: grid;
  gap: 1rem;
  grid-template-columns: minmax(0, 1.5fr) minmax(0, 1fr);
}

.panel,
.inner-card {
  display: grid;
  gap: 0.9rem;
  padding: 1.1rem;
}

.inner-card {
  box-shadow: none;
}

.panel-head,
.table-head {
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

.form-span-2 {
  grid-column: span 2;
}

.filters {
  display: grid;
  gap: 0.55rem;
  grid-template-columns: minmax(0, 1fr) 220px;
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
  padding: 0.7rem 0.5rem;
  text-align: left;
}

th {
  color: #4b6075;
  font-size: 0.83rem;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

tr.created {
  background: rgba(10, 105, 95, 0.08);
}

tr.selected {
  background: rgba(37, 111, 154, 0.1);
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 12px;
  display: grid;
  gap: 0.35rem;
  padding: 1rem;
}

.stack-sm {
  display: grid;
  gap: 0.45rem;
}

.row-actions {
  display: flex;
  gap: 0.5rem;
}

.ledger-summary {
  display: grid;
  gap: 0.6rem;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-item {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--border);
  border-radius: 10px;
  display: grid;
  gap: 0.2rem;
  padding: 0.65rem;
}

.danger {
  border-color: rgba(167, 42, 42, 0.28);
  color: #8d2727;
}

.btn-small {
  min-height: 34px;
  padding: 0.36rem 0.66rem;
}

@media (max-width: 1160px) {
  .layout,
  .sub-layout {
    grid-template-columns: 1fr;
  }

  .ledger-summary {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 700px) {
  .form-grid,
  .filters {
    grid-template-columns: 1fr;
  }

  .form-span-2 {
    grid-column: span 1;
  }

  .ledger-summary {
    grid-template-columns: 1fr;
  }
}
</style>
