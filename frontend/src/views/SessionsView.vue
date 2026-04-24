<template>
  <section class="stack-lg">
    <header class="section-head">
      <div>
        <h2>{{ i18n.t("Sessions") }}</h2>
        <p>{{ i18n.t("Review active login sessions and revoke access from old devices.") }}</p>
      </div>
      <div class="row-actions">
        <button class="btn btn-secondary" @click="loadSessions" :disabled="loading">
          {{ loading ? i18n.t("Loading...") : i18n.t("Refresh") }}
        </button>
        <button class="btn btn-ghost" @click="logoutAll" :disabled="loading || sessions.length === 0">
          {{ i18n.t("Logout All") }}
        </button>
      </div>
    </header>

    <p v-if="error" class="inline-error">{{ error }}</p>
    <p v-if="message" class="muted">{{ message }}</p>

    <article class="surface panel">
      <div v-if="loading" class="muted">{{ i18n.t("Loading...") }}</div>
      <div v-else-if="sessions.length" class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>{{ i18n.t("Device") }}</th>
              <th>{{ i18n.t("Created") }}</th>
              <th>{{ i18n.t("Expires") }}</th>
              <th />
            </tr>
          </thead>
          <tbody>
            <tr v-for="session in sessions" :key="session.id">
              <td>{{ session.deviceHint || "unknown" }}</td>
              <td>{{ session.createdAt }}</td>
              <td>{{ session.expiresAt }}</td>
              <td class="actions-cell">
                <button class="btn btn-secondary btn-small" @click="revokeSession(session.id)" :disabled="loading">
                  {{ i18n.t("Revoke") }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-state">
        <h4>{{ i18n.t("No active sessions found.") }}</h4>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { apiClient } from "../api";
import { useI18n } from "../i18n";

type ApiEnvelope<T> = { success: boolean; data: T };

type AuthSession = {
  id: string;
  deviceHint: string | null;
  createdAt: string;
  expiresAt: string;
};

const i18n = useI18n();
const sessions = ref<AuthSession[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const message = ref<string | null>(null);

async function loadSessions() {
  loading.value = true;
  error.value = null;
  message.value = null;
  try {
    const response = await apiClient.get<ApiEnvelope<AuthSession[]>>("/auth/sessions");
    sessions.value = response.data.data;
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to load sessions.");
  } finally {
    loading.value = false;
  }
}

async function revokeSession(sessionId: string) {
  loading.value = true;
  error.value = null;
  message.value = null;
  try {
    await apiClient.delete(`/auth/sessions/${sessionId}`);
    sessions.value = sessions.value.filter((session) => session.id !== sessionId);
    message.value = i18n.t("Session revoked.");
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to revoke session.");
  } finally {
    loading.value = false;
  }
}

async function logoutAll() {
  loading.value = true;
  error.value = null;
  message.value = null;
  try {
    await apiClient.post("/auth/logout-all");
    sessions.value = [];
    message.value = i18n.t("All sessions revoked.");
  } catch (err: any) {
    error.value = err?.response?.data?.error?.message ?? i18n.t("Failed to logout all sessions.");
  } finally {
    loading.value = false;
  }
}

onMounted(loadSessions);
</script>

<style scoped>
.panel {
  padding: 1rem;
}

.row-actions {
  display: flex;
  gap: 0.55rem;
}

.table-wrap {
  overflow-x: auto;
}

table {
  border-collapse: collapse;
  min-width: 720px;
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

.actions-cell {
  text-align: right;
}

.btn-small {
  min-height: 34px;
}

.empty-state {
  border: 1px dashed var(--border);
  border-radius: 10px;
  padding: 1rem;
}

@media (max-width: 760px) {
  .row-actions {
    width: 100%;
  }

  .row-actions .btn {
    flex: 1;
  }
}
</style>
