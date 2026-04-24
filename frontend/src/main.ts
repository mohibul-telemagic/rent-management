import { createPinia } from "pinia";
import { createApp } from "vue";
import App from "./App.vue";
import { setAccessTokenProvider } from "./api";
import router from "./router";
import "./styles.css";
import { useAuthStore } from "./stores/auth";

const pinia = createPinia();
const app = createApp(App);
app.use(pinia);

const authStore = useAuthStore(pinia);
setAccessTokenProvider(() => authStore.accessToken);

const bootstrap = async () => {
  await authStore.bootstrapSession();
  app.use(router);
  app.mount("#app");
};

void bootstrap();
