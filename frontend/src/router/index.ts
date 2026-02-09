import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import PatientList from "@/views/PatientList.vue";
import StorageView from "@/views/StorageView.vue";
import SampleList from "@/views/SampleList.vue";
import ResearchList from "@/views/ResearchList.vue";
import VisitList from "@/views/VisitList.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/samples",
  },
  {
    path: "/patients",
    name: "PatientList",
    component: PatientList,
  },
  {
    path: "/storage",
    name: "Storage",
    component: StorageView,
  },
  {
    path: "/samples",
    name: "Samples",
    component: SampleList,
  },
  {
    path: "/researches",
    name: "Researches",
    component: ResearchList,
  },
  {
    path: "/visits",
    name: "Visits",
    component: VisitList,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
