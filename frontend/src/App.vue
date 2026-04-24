<template>
  <div class="ambient-bg">
    <span class="orb orb-a" />
    <span class="orb orb-b" />
  </div>
  <div class="app-shell">
    <header class="surface topbar">
      <div class="topbar-main">
        <div class="brand">
          <p class="brand-kicker">{{ i18n.t("Rent Operations") }}</p>
          <h1>RentEase BD</h1>
        </div>

        <div class="topbar-actions">
          <div v-if="authStore.isAuthenticated" class="global-search">
            <input
              v-model.trim="searchQuery"
              class="input"
              type="search"
              :placeholder="i18n.t('Search tenant/property/unit/invoice')"
              @focus="searchFocused = true"
              @blur="onSearchBlur"
            />
            <div v-if="searchFocused && (searchLoading || searchResults.length)" class="search-results surface">
              <p v-if="searchLoading" class="muted">{{ i18n.t("Loading...") }}</p>
              <button
                v-for="item in searchResults"
                :key="item.key"
                class="search-item"
                type="button"
                @mousedown.prevent="openSearchResult(item.path)"
              >
                <strong>{{ item.label }}</strong>
                <span class="muted">{{ item.hint }}</span>
              </button>
            </div>
          </div>

          <div class="language-switch" :aria-label="i18n.t('Language')">
            <button
              class="lang-pill"
              type="button"
              :class="{ active: i18n.language.value === 'en' }"
              @click="switchLanguage('en')"
            >
              EN
            </button>
            <button
              class="lang-pill"
              type="button"
              :class="{ active: i18n.language.value === 'bn' }"
              @click="switchLanguage('bn')"
            >
              বাংলা
            </button>
          </div>

          <router-link v-if="!authStore.isAuthenticated" to="/login" class="btn btn-secondary">{{ i18n.t("Sign in") }}</router-link>
          <button v-else class="btn btn-ghost" @click="signOut">{{ i18n.t("Sign out") }}</button>
        </div>
      </div>

      <nav v-if="authStore.isAuthenticated" class="topnav" :aria-label="i18n.t('Primary navigation')">
        <router-link to="/" class="nav-link">{{ i18n.t("Overview") }}</router-link>
        <router-link to="/properties" class="nav-link">{{ i18n.t("Properties") }}</router-link>
        <router-link to="/invoices" class="nav-link">{{ i18n.t("Invoices") }}</router-link>
        <router-link to="/tenants" class="nav-link">{{ i18n.t("Tenants") }}</router-link>
        <router-link to="/expenses" class="nav-link">{{ i18n.t("Expenses") }}</router-link>
        <router-link to="/deposits" class="nav-link">{{ i18n.t("Deposits") }}</router-link>
        <router-link to="/sessions" class="nav-link">{{ i18n.t("Sessions") }}</router-link>
        <router-link to="/notifications" class="nav-link">{{ i18n.t("Notifications") }}</router-link>
      </nav>
    </header>

    <main class="content">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRouter } from "vue-router";
import { apiClient } from "./api";
import { setLanguage, useI18n } from "./i18n";
import { useAuthStore } from "./stores/auth";

const authStore = useAuthStore();
const router = useRouter();
const i18n = useI18n();
const searchQuery = ref("");
const searchResults = ref<Array<{ key: string; label: string; hint: string; path: string }>>([]);
const searchLoading = ref(false);
const searchFocused = ref(false);
let searchTimer: number | null = null;

const signOut = async () => {
  await authStore.logout();
  await router.push({ name: "login" });
};

const switchLanguage = async (language: "en" | "bn") => {
  const next = setLanguage(language);
  if (authStore.isAuthenticated) {
    try {
      await authStore.updatePreferredLanguage(next);
    } catch {
      // Local language state remains updated even if remote preference sync fails.
    }
  }
};

function onSearchBlur() {
  window.setTimeout(() => {
    searchFocused.value = false;
  }, 120);
}

async function runGlobalSearch(query: string) {
  if (!authStore.isAuthenticated || query.length < 2) {
    searchResults.value = [];
    return;
  }
  searchLoading.value = true;
  try {
    const [tenantsResp, propertiesResp, invoicesResp] = await Promise.all([
      apiClient.get<{ success: boolean; data: Array<{ id: number; fullName: string; propertyUnitId: number }> }>("/tenants"),
      apiClient.get<{ success: boolean; data: Array<{ id: number; propertyName: string }> }>("/properties"),
      apiClient.get<{ success: boolean; data: Array<{ id: number; tenantName: string | null; propertyName: string | null; propertyUnitId: number }> }>("/invoices")
    ]);
    const q = query.toLowerCase();
    const results: Array<{ key: string; label: string; hint: string; path: string }> = [];

    for (const tenant of tenantsResp.data.data) {
      if (
        tenant.fullName.toLowerCase().includes(q)
        || String(tenant.id).includes(q)
        || String(tenant.propertyUnitId).includes(q)
      ) {
        results.push({
          key: `tenant-${tenant.id}`,
          label: `${i18n.t("Tenant")} #${tenant.id} - ${tenant.fullName}`,
          hint: `${i18n.t("Unit")} #${tenant.propertyUnitId}`,
          path: `/tenants?tenantId=${tenant.id}`
        });
      }
    }

    for (const property of propertiesResp.data.data) {
      if (property.propertyName.toLowerCase().includes(q) || String(property.id).includes(q)) {
        results.push({
          key: `property-${property.id}`,
          label: `${i18n.t("Property")} #${property.id} - ${property.propertyName}`,
          hint: i18n.t("Properties"),
          path: "/properties"
        });
      }
    }

    for (const invoice of invoicesResp.data.data) {
      if (
        String(invoice.id).includes(q)
        || (invoice.tenantName ?? "").toLowerCase().includes(q)
        || String(invoice.propertyUnitId).includes(q)
      ) {
        results.push({
          key: `invoice-${invoice.id}`,
          label: `${i18n.t("Invoice")} #${invoice.id}`,
          hint: `${invoice.tenantName ?? i18n.t("Tenant")} | ${invoice.propertyName ?? i18n.t("Property")} | ${i18n.t("Unit")} #${invoice.propertyUnitId}`,
          path: `/invoices?invoiceId=${invoice.id}`
        });
      }
    }

    searchResults.value = results.slice(0, 12);
  } catch {
    searchResults.value = [];
  } finally {
    searchLoading.value = false;
  }
}

async function openSearchResult(path: string) {
  searchFocused.value = false;
  searchQuery.value = "";
  searchResults.value = [];
  await router.push(path);
}

watch(searchQuery, (value) => {
  if (searchTimer) {
    window.clearTimeout(searchTimer);
  }
  searchTimer = window.setTimeout(() => {
    void runGlobalSearch(value);
  }, 240);
});
</script>

<style scoped>
.app-shell {
  margin: 0 auto;
  max-width: 1120px;
  padding: 1.2rem 1.1rem 2rem;
  position: relative;
}

.ambient-bg {
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  position: fixed;
  z-index: 0;
}

.orb {
  border-radius: 999px;
  filter: blur(1px);
  position: absolute;
}

.orb-a {
  background: radial-gradient(circle, rgba(99, 161, 219, 0.23), rgba(99, 161, 219, 0));
  height: 340px;
  left: -80px;
  top: -60px;
  width: 340px;
}

.orb-b {
  background: radial-gradient(circle, rgba(69, 153, 128, 0.2), rgba(69, 153, 128, 0));
  height: 280px;
  right: -40px;
  top: 0;
  width: 280px;
}

.topbar {
  display: grid;
  gap: 1.2rem;
  padding: 0.9rem 1rem;
  position: sticky;
  top: 0.7rem;
  z-index: 2;
}

.topbar-main {
  align-items: center;
  display: flex;
  gap: 1rem;
  justify-content: space-between;
}

.brand-kicker {
  color: #4f6a85;
  font-size: 0.74rem;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
}

.brand h1 {
  font-size: 1.25rem;
}

.topnav {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.nav-link {
  border-radius: 9px;
  color: #435568;
  font-size: 0.95rem;
  font-weight: 600;
  padding: 0.5rem 0.72rem;
  text-decoration: none;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.nav-link:hover {
  background: rgba(99, 129, 154, 0.12);
}

.topnav .router-link-active {
  background: rgba(10, 105, 95, 0.12);
  color: #0c5c54;
}

.topbar-actions {
  align-items: center;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: flex-end;
}

.global-search {
  min-width: 260px;
  width: min(360px, 44vw);
  position: relative;
}

.search-results {
  display: grid;
  gap: 0.2rem;
  left: 0;
  max-height: 280px;
  overflow-y: auto;
  padding: 0.35rem;
  position: absolute;
  right: 0;
  top: calc(100% + 0.35rem);
  z-index: 10;
}

.search-item {
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: grid;
  gap: 0.06rem;
  padding: 0.42rem;
  text-align: left;
}

.search-item:hover {
  background: rgba(99, 129, 154, 0.1);
}

.language-switch {
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--border);
  border-radius: 999px;
  display: inline-flex;
  gap: 0.2rem;
  padding: 0.2rem;
}

.lang-pill {
  background: transparent;
  border: none;
  border-radius: 999px;
  color: #4a5c70;
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 700;
  min-height: 30px;
  min-width: 52px;
  padding: 0 0.62rem;
}

.lang-pill.active {
  background: linear-gradient(160deg, #1f7d75, #256f9a);
  color: #fff;
}

.content {
  margin-top: 1.2rem;
  position: relative;
  z-index: 1;
}

@media (max-width: 900px) {
  .topbar {
    position: static;
  }

  .topbar-main {
    align-items: stretch;
    flex-direction: column;
  }

  .topnav {
    width: 100%;
  }

  .nav-link {
    text-align: center;
    width: 100%;
  }

  .topbar-actions {
    flex-wrap: wrap;
    width: 100%;
  }

  .global-search {
    min-width: 0;
    width: 100%;
  }

  .topbar-actions .btn {
    width: auto;
  }
}

@media (max-width: 640px) {
  .topbar-actions .btn {
    width: 100%;
  }
}
</style>
