<template>
  <section class="login-page">
    <article class="surface login-card">
      <p class="kicker">{{ i18n.t("Secure Access") }}</p>
      <h2>{{ i18n.t("Sign in to your workspace") }}</h2>
      <p class="muted">
        {{ i18n.t("Access management, tenant records, and billing operations from a single dashboard.") }}
      </p>

      <form class="stack-md" @submit.prevent="submit">
        <div class="field">
          <label for="email">{{ i18n.t("Email") }}</label>
          <input
            id="email"
            v-model="email"
            class="input"
            type="email"
            autocomplete="username"
            required
            placeholder="owner@rentease.bd"
          />
        </div>

        <div class="field">
          <label for="password">{{ i18n.t("Password") }}</label>
          <input
            id="password"
            v-model="password"
            class="input"
            type="password"
            autocomplete="current-password"
            required
            :placeholder="i18n.t('Enter your password')"
          />
        </div>

        <button class="btn" type="submit" :disabled="authStore.loading">
          {{ authStore.loading ? i18n.t("Signing in...") : i18n.t("Sign in") }}
        </button>
        <p v-if="authStore.error" class="inline-error">{{ authStore.error }}</p>
      </form>

      <div class="demo-note">
        <p><strong>{{ i18n.t("Local demo account") }}</strong></p>
        <p class="muted">owner@rentease.bd / Owner@123</p>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "../i18n";
import { useAuthStore } from "../stores/auth";

const email = ref("");
const password = ref("");
const authStore = useAuthStore();
const router = useRouter();
const i18n = useI18n();

const submit = async () => {
  try {
    await authStore.login(email.value, password.value);
    await router.push({ name: "home" });
  } catch {
    // Error is handled by store state.
  }
};
</script>

<style scoped>
.login-page {
  display: grid;
  justify-content: center;
  min-height: calc(100vh - 240px);
  padding-top: 0.6rem;
}

.login-card {
  display: grid;
  gap: 0.95rem;
  max-width: 440px;
  padding: 1.35rem;
  width: min(100%, 440px);
}

.kicker {
  color: #4f6b86;
  font-size: 0.76rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.field {
  display: grid;
  gap: 0.35rem;
}

label {
  color: #415467;
  font-size: 0.88rem;
  font-weight: 600;
}

.demo-note {
  border-top: 1px solid var(--border);
  display: grid;
  gap: 0.25rem;
  padding-top: 0.85rem;
  margin: 0;
}
</style>
