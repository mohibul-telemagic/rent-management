<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Notifications") }}</h2>
        <p>{{ i18n.t("Mock delivery workflow log for invoice, payment, overdue, and move-out events.") }}</p>
      </div>
      <div class="actions">
        <button class="btn btn-secondary" @click="loadEvents" :disabled="loading">
          {{ loading ? i18n.t("Loading...") : i18n.t("Refresh") }}
        </button>
        <button class="btn btn-ghost" @click="runOverdueReminder" :disabled="loading">
          {{ i18n.t("Run Overdue Reminders") }}
        </button>
      </div>
    </header>

    <p v-if="error" class="inline-error">{{ error }}</p>
    <p v-if="message" class="muted">{{ message }}</p>

    <article class="surface panel">
      <div class="table-wrap" v-if="events.length">
        <table>
          <thead>
            <tr>
              <th>{{ i18n.t("Created") }}</th>
              <th>{{ i18n.t("Event") }}</th>
              <th>{{ i18n.t("Tenant") }}</th>
              <th>{{ i18n.t("Channel") }}</th>
              <th>{{ i18n.t("Status") }}</th>
              <th>{{ i18n.t("Destination") }}</th>
              <th>{{ i18n.t("Message") }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="event in events" :key="event.id">
              <td>{{ event.createdAt }}</td>
              <td>{{ event.eventType }}</td>
              <td>{{ event.tenantId ? `#${event.tenantId}` : "-" }}</td>
              <td>{{ event.channel }}</td>
              <td>{{ event.status }}</td>
              <td>{{ event.destination || "-" }}</td>
              <td>{{ event.message }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">
        <h4>{{ i18n.t("No notification events yet.") }}</h4>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type NotificationEvent = {
  id: number;
  tenantId: number | null;
  eventType: string;
  channel: string;
  status: string;
  destination: string | null;
  message: string;
  createdAt: string;
};

const i18n = useI18n();
const events = ref<NotificationEvent[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const message = ref<string | null>(null);

async function loadEvents() {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get<ApiEnvelope<NotificationEvent[]>>("/notifications");
    events.value = response.data.data;
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to load notifications.");
  } finally {
    loading.value = false;
  }
}

async function runOverdueReminder() {
  loading.value = true;
  error.value = null;
  message.value = null;
  try {
    const response = await apiClient.post<ApiEnvelope<{ createdCount: number }>>("/notifications/overdue-reminders/run");
    message.value = i18n.t("Created overdue reminders: {count}", { count: response.data.data.createdCount });
    await loadEvents();
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to run overdue reminders.");
  } finally {
    loading.value = false;
  }
}

onMounted(loadEvents);
</script>

<style scoped>
.actions {
  display: flex;
  gap: 0.5rem;
}

.panel {
  padding: 1rem;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 1000px;
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

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 10px;
  padding: 1rem;
}

@media (max-width: 760px) {
  .actions {
    width: 100%;
  }

  .actions .btn {
    flex: 1;
  }
}
</style>
