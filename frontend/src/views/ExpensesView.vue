<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Expenses") }}</h2>
        <p>{{ i18n.t("Track operational costs, keep receipt evidence, and maintain clean property-level records.") }}</p>
      </div>
      <button class="btn btn-secondary" @click="refreshAll" :disabled="loadingExpenses">
        {{ loadingExpenses ? i18n.t("Refreshing...") : i18n.t("Refresh") }}
      </button>
    </header>

    <p v-if="pageError" class="inline-error">{{ pageError }}</p>

    <div class="layout">
      <article class="surface panel stack-md">
        <div class="panel-head">
          <h3>{{ editingExpenseId ? i18n.t("Edit Expense") : i18n.t("Add Expense") }}</h3>
          <span class="status-pill">{{ editingExpenseId ? i18n.t("Update") : i18n.t("Create") }}</span>
        </div>

        <form class="form-grid" @submit.prevent="saveExpense">
          <div class="field">
            <label for="expense-property">{{ i18n.t("Property") }}</label>
            <select id="expense-property" v-model.number="form.propertyId" class="select" required>
              <option :value="null" disabled>{{ i18n.t("Select property") }}</option>
              <option v-for="property in properties" :key="property.id" :value="property.id">
                {{ property.propertyName }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="expense-category">{{ i18n.t("Category") }}</label>
            <input id="expense-category" v-model="form.category" class="input" :placeholder="i18n.t('Maintenance, Utility, Service')" required />
          </div>

          <div class="field">
            <label for="expense-amount">{{ i18n.t("Amount (BDT)") }}</label>
            <input id="expense-amount" v-model="form.amountBdt" class="input" type="number" min="0.01" step="0.01" required />
          </div>

          <div class="field">
            <label for="expense-date">{{ i18n.t("Expense date") }}</label>
            <input id="expense-date" v-model="form.expenseDate" class="input" type="date" required />
          </div>

          <div class="field form-span-2">
            <label for="expense-description">{{ i18n.t("Description (optional)") }}</label>
            <textarea id="expense-description" v-model="form.description" class="textarea" :placeholder="i18n.t('Context for audit/reporting')" />
          </div>

          <div class="field form-span-2">
            <label for="expense-receipt">{{ i18n.t("Receipt (JPEG/PNG/PDF, max 5MB)") }}</label>
            <input
              id="expense-receipt"
              class="input"
              type="file"
              accept="image/jpeg,image/png,application/pdf"
              @change="onReceiptSelected"
            />
          </div>

          <div class="actions-row form-span-2">
            <button class="btn" type="submit" :disabled="savingExpense">
              {{ savingExpense ? i18n.t("Saving...") : editingExpenseId ? i18n.t("Update Expense") : i18n.t("Create Expense") }}
            </button>
            <button v-if="editingExpenseId" class="btn btn-secondary" type="button" @click="resetForm">{{ i18n.t("Cancel") }}</button>
          </div>
        </form>

        <p v-if="formMessage" class="muted">{{ formMessage }}</p>
        <p v-if="formError" class="inline-error">{{ formError }}</p>
      </article>

      <article class="surface panel stack-md">
        <div class="section-head compact">
          <div>
            <h3>{{ i18n.t("Expense Registry") }}</h3>
            <p class="muted">{{ i18n.t("Review costs by property and manage receipt files.") }}</p>
          </div>
          <span class="status-pill">{{ expenses.length }} {{ i18n.t("records") }}</span>
        </div>

        <div class="filters">
          <select v-model.number="filters.propertyId" class="select">
            <option :value="null">{{ i18n.t("All properties") }}</option>
            <option v-for="property in properties" :key="property.id" :value="property.id">
              {{ property.propertyName }}
            </option>
          </select>
          <button class="btn btn-secondary" @click="loadExpenses" :disabled="loadingExpenses">
            {{ loadingExpenses ? i18n.t("Loading...") : i18n.t("Apply") }}
          </button>
        </div>

        <div v-if="loadingExpenses" class="muted">{{ i18n.t("Loading expenses...") }}</div>

        <div v-else-if="expenses.length" class="table-wrap">
          <table>
            <thead>
            <tr>
                <th>{{ i18n.t("Date") }}</th>
                <th>{{ i18n.t("Property") }}</th>
                <th>{{ i18n.t("Category") }}</th>
                <th>{{ i18n.t("Amount (BDT)") }}</th>
                <th>{{ i18n.t("Receipt") }}</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="expense in expenses" :key="expense.id">
                <td>{{ expense.expenseDate }}</td>
                <td>{{ expense.propertyName || `#${expense.propertyId}` }}</td>
                <td>{{ expense.category }}</td>
                <td>{{ asMoney(expense.amountBdt) }}</td>
                <td>
                  <span class="status-pill" :class="expense.hasReceipt ? '' : 'inactive'">
                    {{ expense.hasReceipt ? i18n.t("Attached") : i18n.t("Missing") }}
                  </span>
                </td>
                <td class="actions-cell">
                  <div class="row-actions">
                    <button class="btn btn-secondary btn-small" @click="startEdit(expense)">{{ i18n.t("Edit") }}</button>
                    <button class="btn btn-ghost btn-small" @click="deleteExpense(expense.id)">{{ i18n.t("Delete") }}</button>
                    <button
                      v-if="expense.hasReceipt"
                      class="btn btn-ghost btn-small"
                      @click="previewReceipt(expense.id)"
                    >
                      {{ i18n.t("Preview") }}
                    </button>
                    <button
                      v-if="expense.hasReceipt"
                      class="btn btn-ghost btn-small"
                      @click="downloadReceipt(expense.id, expense.receiptMimeType)"
                    >
                      {{ i18n.t("Receipt") }}
                    </button>
                    <button
                      v-if="expense.hasReceipt"
                      class="btn btn-ghost btn-small"
                      @click="removeReceipt(expense.id)"
                    >
                      {{ i18n.t("Remove") }} {{ i18n.t("Receipt") }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="empty-state">
          <h4>{{ i18n.t("No expenses yet") }}</h4>
          <p class="muted">{{ i18n.t("Record property expenses to enable accurate financial tracking and exports.") }}</p>
        </div>

        <p v-if="listError" class="inline-error">{{ listError }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type Property = {
  id: number;
  propertyName: string;
};

type Expense = {
  id: number;
  propertyId: number;
  propertyName: string | null;
  category: string;
  amountBdt: number;
  expenseDate: string;
  description: string | null;
  hasReceipt: boolean;
  receiptMimeType: string | null;
};

const properties = ref<Property[]>([]);
const expenses = ref<Expense[]>([]);
const loadingExpenses = ref(false);
const savingExpense = ref(false);

const pageError = ref<string | null>(null);
const listError = ref<string | null>(null);
const formError = ref<string | null>(null);
const formMessage = ref<string | null>(null);

const editingExpenseId = ref<number | null>(null);
const selectedReceipt = ref<File | null>(null);
const i18n = useI18n();

const filters = ref<{ propertyId: number | null }>({
  propertyId: null
});

const form = ref<{
  propertyId: number | null;
  category: string;
  amountBdt: string;
  expenseDate: string;
  description: string;
}>({
  propertyId: null,
  category: "",
  amountBdt: "",
  expenseDate: todayIso(),
  description: ""
});

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function asMoney(value: number) {
  return i18n.formatMoney(value);
}

function resetForm() {
  editingExpenseId.value = null;
  selectedReceipt.value = null;
  form.value = {
    propertyId: null,
    category: "",
    amountBdt: "",
    expenseDate: todayIso(),
    description: ""
  };
  formError.value = null;
  formMessage.value = null;
}

function onReceiptSelected(event: Event) {
  const input = event.target as HTMLInputElement;
  selectedReceipt.value = input.files?.[0] ?? null;
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

function extensionFromMime(mimeType: string | null) {
  if (mimeType === "application/pdf") {
    return "pdf";
  }
  if (mimeType === "image/png") {
    return "png";
  }
  return "jpg";
}

async function loadProperties() {
  const response = await apiClient.get<ApiEnvelope<Property[]>>("/properties");
  properties.value = response.data.data;
}

async function loadExpenses() {
  loadingExpenses.value = true;
  listError.value = null;
  try {
    const params: Record<string, number> = {};
    if (filters.value.propertyId) {
      params.propertyId = filters.value.propertyId;
    }

    const response = await apiClient.get<ApiEnvelope<Expense[]>>("/expenses", { params });
    expenses.value = response.data.data;
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load expenses.");
  } finally {
    loadingExpenses.value = false;
  }
}

function buildUpdatePayload() {
  const propertyId = form.value.propertyId;
  const category = form.value.category.trim();
  const amountBdt = Number(form.value.amountBdt);
  const expenseDate = form.value.expenseDate;

  if (!propertyId) {
    throw new Error(i18n.t("Property is required."));
  }
  if (!category) {
    throw new Error(i18n.t("Category is required."));
  }
  if (!Number.isFinite(amountBdt) || amountBdt <= 0) {
    throw new Error(i18n.t("Amount must be greater than 0."));
  }
  if (!expenseDate) {
    throw new Error(i18n.t("Expense date is required."));
  }

  return {
    propertyId,
    category,
    amountBdt,
    expenseDate,
    description: form.value.description.trim() || null
  };
}

async function saveExpense() {
  let payload: ReturnType<typeof buildUpdatePayload>;
  let successMessage = i18n.t("Expense saved.");
  try {
    payload = buildUpdatePayload();
  } catch (error: any) {
    formError.value = error.message ?? i18n.t("Please check your input.");
    return;
  }

  savingExpense.value = true;
  formError.value = null;
  formMessage.value = null;

  try {
    if (editingExpenseId.value) {
      await apiClient.put(`/expenses/${editingExpenseId.value}`, payload);

      if (selectedReceipt.value) {
        const formData = new FormData();
        formData.append("receipt", selectedReceipt.value);
        await apiClient.put(`/expenses/${editingExpenseId.value}/receipt`, formData);
      }

      successMessage = i18n.t("Expense updated.");
    } else {
      const formData = new FormData();
      formData.append("propertyId", String(payload.propertyId));
      formData.append("category", payload.category);
      formData.append("amountBdt", String(payload.amountBdt));
      formData.append("expenseDate", payload.expenseDate);
      if (payload.description) {
        formData.append("description", payload.description);
      }
      if (selectedReceipt.value) {
        formData.append("receipt", selectedReceipt.value);
      }

      await apiClient.post("/expenses", formData);
      successMessage = i18n.t("Expense created.");
    }

    resetForm();
    formMessage.value = successMessage;
    await loadExpenses();
  } catch (error: any) {
    formError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to save expense.");
  } finally {
    savingExpense.value = false;
  }
}

function startEdit(expense: Expense) {
  editingExpenseId.value = expense.id;
  selectedReceipt.value = null;
  formError.value = null;
  formMessage.value = null;
  form.value = {
    propertyId: expense.propertyId,
    category: expense.category,
    amountBdt: String(expense.amountBdt),
    expenseDate: expense.expenseDate,
    description: expense.description ?? ""
  };
}

async function deleteExpense(expenseId: number) {
  if (!window.confirm(i18n.t("Delete expense #{id}? This cannot be undone.", { id: expenseId }))) {
    return;
  }

  listError.value = null;
  try {
    await apiClient.delete(`/expenses/${expenseId}`);
    await loadExpenses();
    if (editingExpenseId.value === expenseId) {
      resetForm();
    }
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to delete expense.");
  }
}

async function downloadReceipt(expenseId: number, mimeType: string | null) {
  listError.value = null;
  try {
    const response = await apiClient.get(`/expenses/${expenseId}/receipt`, { responseType: "blob" });
    const extension = extensionFromMime(mimeType);
    triggerBrowserDownload(response.data, `expense-${expenseId}-receipt.${extension}`);
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download receipt.");
  }
}

async function previewReceipt(expenseId: number) {
  listError.value = null;
  try {
    const response = await apiClient.get(`/expenses/${expenseId}/receipt`, { responseType: "blob" });
    openBlobPreview(response.data);
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to download receipt.");
  }
}

async function removeReceipt(expenseId: number) {
  if (!window.confirm(i18n.t("Remove receipt from expense #{id}?", { id: expenseId }))) {
    return;
  }
  listError.value = null;
  try {
    await apiClient.delete(`/expenses/${expenseId}/receipt`);
    await loadExpenses();
  } catch (error: any) {
    listError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to remove receipt.");
  }
}

async function refreshAll() {
  pageError.value = null;
  try {
    await Promise.all([loadProperties(), loadExpenses()]);
  } catch (error: any) {
    pageError.value = error?.response?.data?.error?.message ?? i18n.t("Failed to load expenses context.");
  }
}

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

.form-span-2 {
  grid-column: span 2;
}

.actions-row {
  display: flex;
  gap: 0.55rem;
}

.filters {
  display: flex;
  gap: 0.55rem;
}

.filters .select {
  max-width: 320px;
}

.compact {
  align-items: flex-start;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 880px;
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

  .form-grid {
    grid-template-columns: 1fr;
  }

  .form-span-2 {
    grid-column: span 1;
  }

  .actions-row {
    flex-wrap: wrap;
  }
}
</style>
