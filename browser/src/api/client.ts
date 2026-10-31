const apiBase: string = import.meta.env.VITE_GAME_API;
const tokenKey = "authToken";
const refreshEveryMillis = 10 * 60 * 1000;

export class ApiError extends Error {
  public readonly status: number;

  public constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export const getToken = (): string | null => {
  try {
    return localStorage.getItem(tokenKey);
  } catch {
    return null;
  }
};

export const setToken = (token: string | null): void => {
  try {
    if (token) {
      localStorage.setItem(tokenKey, token);
    } else {
      localStorage.removeItem(tokenKey);
    }
  } catch {
    // storage unavailable, the session lasts until the page closes
  }
  window.dispatchEvent(new Event("auth-changed"));
};

const readMessage = async (response: Response): Promise<string> => {
  try {
    const body: unknown = await response.json();
    if (body && typeof body === "object" && "message" in body && typeof body.message === "string") {
      return body.message;
    }
  } catch {
    // not json
  }
  return `The server answered ${response.status}`;
};

export const request = async <T>(path: string, init: { method?: string; body?: unknown } = {}): Promise<T> => {
  const token = getToken();
  const response = await fetch(`${apiBase}${path}`, {
    method: init.method ?? "GET",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: init.body === undefined ? undefined : JSON.stringify(init.body),
  });

  if (response.status === 401 && token) {
    setToken(null);
    throw new ApiError(401, "Your session has ended, sign in again");
  }
  if (!response.ok) {
    throw new ApiError(response.status, await readMessage(response));
  }

  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
};

export const get = <T>(path: string): Promise<T> => request<T>(path);
export const post = <T>(path: string, body?: unknown): Promise<T> => request<T>(path, { method: "POST", body: body ?? {} });

let refreshTimer: number | undefined;

/**
 * tokens last an hour, while the game is open we trade the token for a fresh one regularly
 */
export const keepSessionFresh = (): (() => void) => {
  window.clearInterval(refreshTimer);
  refreshTimer = window.setInterval(async () => {
    if (!getToken()) {
      return;
    }
    try {
      const { token } = await post<{ token: string }>("/auth/refresh");
      setToken(token);
    } catch {
      // the next request will notice an expired session
    }
  }, refreshEveryMillis);
  return () => window.clearInterval(refreshTimer);
};

export const errorText = (error: unknown): string => (error instanceof Error ? error.message : "Something went wrong");
