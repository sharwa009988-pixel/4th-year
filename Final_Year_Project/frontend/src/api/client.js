import axios from "axios";

/* =============================
   AXIOS BASE CONFIG
============================= */

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ||
  import.meta.env.VITE_API_URL ||
  "/api";

const axiosApi = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  }
});

/* =============================
   COMMON RESPONSE HANDLER
============================= */

async function handleRes(promise) {
  try {
    const res = await promise;
    return res.data;
  } catch (err) {
    const status = err.response?.status;
    const data = err.response?.data;
    if (!err.response) {
      throw new Error(`Cannot reach API at ${BASE_URL}. Set VITE_API_BASE_URL to your backend URL.`);
    }
    if (status === 404) {
      throw new Error("Endpoint not found (check base URL and API path).");
    }
    if (status === 401 || status === 403) {
      throw new Error("Invalid email or password");
    }
    if (data && typeof data === "object" && data.message) {
      throw new Error(data.message);
    }
    if (typeof data === "string") {
      throw new Error("Server returned a non-JSON error. Check backend logs.");
    }
    throw new Error("Request failed. Please try again.");
  }
}

/* =============================
   GENERIC API FUNCTION
============================= */

export async function api(method, path, body = null) {
  const config = {
    method,
    url: path,
  };

  if (body !== null) config.data = body;

  return handleRes(axiosApi.request(config));
}

/* =============================
   AUTH APIs
============================= */

export const authApi = {

  // ✅ REGISTER WITH CONFIRM PASSWORD
  register: (name, email, password, confirmPassword) =>
    api("POST", "/auth/register", {
      name,
      email,
      password,
      confirmPassword,
    }),

  // ✅ LOGIN
  login: (email, password) =>
    api("POST", "/auth/login", {
      email,
      password,
    }),
};

/* =============================
   ROLE APIs
============================= */

export const rolesApi = {
  selectRole: (role) =>
    api("POST", "/roles/select", { role }),
};

export async function getPredefinedRoles() {
  return handleRes(axiosApi.get("/roles/predefined"));
}

/* =============================
   USER APIs
============================= */

export const userApi = {
  me: () => api("GET", "/users/me"),
};

/* =============================
   DASHBOARD APIs
============================= */

export const dashboardApi = {
  stats: () => api("GET", "/dashboard/stats"),
};

/* =============================
   INTERVIEW APIs
============================= */

export const interviewApi = {

  start: (mode, topic, difficulty) =>
    api("POST", "/interview/start", {
      mode,
      topic: topic || null,
      difficulty: difficulty || null,
    }),

  generateQuestion: (mode, topic, difficulty) =>
    api("POST", "/interview/question/generate", {
      mode,
      topic: topic || null,
      difficulty: difficulty || null,
    }),

  generateCodingProblem: (topic) =>
    api("POST", "/interview/coding/problem", {
      topic: topic || null,
    }),

  evaluate: (
    questionId,
    questionText,
    userAnswer,
    sessionId,
    topic,
    difficulty
  ) =>
    api("POST", "/interview/evaluate", {
      questionId,
      questionText,
      userAnswer,
      sessionId,
      topic,
      difficulty,
    }),

  evaluateCode: (
    code,
    problemStatement,
    executionOutput,
    sessionId
  ) =>
    api("POST", "/interview/evaluate-code", {
      code,
      problemStatement,
      executionOutput,
      sessionId,
    }),

  endSession: (sessionId) =>
    api("POST", `/interview/sessions/${sessionId}/end`),

  getSession: (sessionId) =>
    api("GET", `/interview/sessions/${sessionId}`),

  history: (role, limit = 20) => {
    const params = new URLSearchParams();

    if (role) params.set("role", role);
    params.set("limit", limit);

    return api("GET", `/interview/history?${params}`);
  },
};

/* =============================
   CODE EXECUTION APIs
============================= */

export const codeApi = {
  execute: (code) =>
    api("POST", "/code/execute", { code }),
};

export default axiosApi;
