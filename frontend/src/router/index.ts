import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import DepositsView from "../views/DepositsView.vue";
import ExpensesView from "../views/ExpensesView.vue";
import HomeView from "../views/HomeView.vue";
import InvoicesView from "../views/InvoicesView.vue";
import LoginView from "../views/LoginView.vue";
import NotificationsView from "../views/NotificationsView.vue";
import PropertiesView from "../views/PropertiesView.vue";
import PropertySettingsView from "../views/PropertySettingsView.vue";
import PropertyUnitsView from "../views/PropertyUnitsView.vue";
import SessionsView from "../views/SessionsView.vue";
import TenantsView from "../views/TenantsView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: HomeView, meta: { requiresAuth: true } },
    { path: "/properties", name: "properties", component: PropertiesView, meta: { requiresAuth: true } },
    { path: "/properties/:id/units", name: "property-units", component: PropertyUnitsView, meta: { requiresAuth: true } },
    {
      path: "/properties/:id/settings",
      name: "property-settings",
      component: PropertySettingsView,
      meta: { requiresAuth: true }
    },
    { path: "/invoices", name: "invoices", component: InvoicesView, meta: { requiresAuth: true } },
    { path: "/tenants", name: "tenants", component: TenantsView, meta: { requiresAuth: true } },
    { path: "/expenses", name: "expenses", component: ExpensesView, meta: { requiresAuth: true } },
    { path: "/deposits", name: "deposits", component: DepositsView, meta: { requiresAuth: true } },
    { path: "/sessions", name: "sessions", component: SessionsView, meta: { requiresAuth: true } },
    { path: "/notifications", name: "notifications", component: NotificationsView, meta: { requiresAuth: true } },
    { path: "/login", name: "login", component: LoginView }
  ]
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "login" };
  }
  if (to.name === "login" && authStore.isAuthenticated) {
    return { name: "home" };
  }
  return true;
});

export default router;
