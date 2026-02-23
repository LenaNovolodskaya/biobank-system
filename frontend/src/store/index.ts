import { createStore } from "vuex";

export interface AuthState {
  token: string | null;
  username: string | null;
  fullName: string | null;
  userId: number | null;
  permissions: Set<string>;
}

const AUTH_KEY = "biobank_auth";

function loadAuth(): Partial<AuthState> {
  try {
    const raw = localStorage.getItem(AUTH_KEY);
    if (!raw) return {};
    const data = JSON.parse(raw);
    if (!data.token) return {};
    return {
      token: data.token,
      username: data.username,
      fullName: data.fullName,
      userId: data.userId,
      permissions: new Set(data.permissions || []),
    };
  } catch {
    return {};
  }
}

function saveAuth(state: AuthState) {
  const data = {
    token: state.token,
    username: state.username,
    fullName: state.fullName,
    userId: state.userId,
    permissions: Array.from(state.permissions),
  };
  localStorage.setItem(AUTH_KEY, JSON.stringify(data));
}

export default createStore<AuthState>({
  state: {
    token: null,
    username: null,
    fullName: null,
    userId: null,
    permissions: new Set(),
    ...loadAuth(),
  },
  mutations: {
    setAuth(
      state,
      payload: {
        token: string;
        username: string;
        fullName: string;
        userId: number;
        permissions: string[];
      }
    ) {
      state.token = payload.token;
      state.username = payload.username;
      state.fullName = payload.fullName;
      state.userId = payload.userId;
      state.permissions = new Set(payload.permissions);
      saveAuth(state);
    },
    clearAuth(state) {
      state.token = null;
      state.username = null;
      state.fullName = null;
      state.userId = null;
      state.permissions = new Set();
      localStorage.removeItem(AUTH_KEY);
    },
  },
  getters: {
    isAuthenticated: (state) => !!state.token,
    hasPermission: (state) => (permission: string) =>
      state.permissions.has(permission),
    canManageUsers: (state) => state.permissions.has("user.manage"),
    canManageRoles: (state) => state.permissions.has("role.manage"),
  },
  actions: {},
});
