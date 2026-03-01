import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import store from "@/store";
import PatientList from "@/views/PatientList.vue";
import StorageView from "@/views/StorageView.vue";
import SampleList from "@/views/SampleList.vue";
import ResearchList from "@/views/ResearchList.vue";
import VisitList from "@/views/VisitList.vue";
import ReferencesView from "@/views/ReferencesView.vue";
import Login from "@/views/Login.vue";
import Register from "@/views/Register.vue";
import UserList from "@/views/UserList.vue";
import RoleList from "@/views/RoleList.vue";
import ProfileView from "@/views/ProfileView.vue";
import UserProfileView from "@/views/UserProfileView.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    name: "Login",
    component: Login,
    meta: { public: true },
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
    meta: { public: true },
  },
  {
    path: "/",
    redirect: "/samples",
  },
  {
    path: "/profile",
    name: "Profile",
    component: ProfileView,
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
  {
    path: "/references",
    name: "References",
    component: ReferencesView,
  },
  {
    path: "/users",
    name: "UserList",
    component: UserList,
    meta: { requiresUserManage: true },
  },
  {
    path: "/users/:userId",
    name: "UserProfile",
    component: UserProfileView,
    meta: { requiresUserManage: true },
  },
  {
    path: "/roles",
    name: "RoleList",
    component: RoleList,
    meta: { requiresRoleManage: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, _from, next) => {
  const isPublic = to.matched.some((r) => r.meta?.public);
  const isAuthenticated = store.getters.isAuthenticated;

  if (!isPublic && !isAuthenticated) {
    next({ path: "/login" });
  } else {
    next();
  }
});

export default router;
