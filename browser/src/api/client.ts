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

const trialKey = "trialAccount";

/** a kingdom started with only its name: the browser keeps the placeholder address and password to sign back in */
export type TrialAccount = { email: string; password: string };

export const getTrial = (): TrialAccount | null => {
  try {
    const kept = localStorage.getItem(trialKey);
    return kept ? (JSON.parse(kept) as TrialAccount) : null;
  } catch {
    return null;
  }
};

export const setTrial = (account: TrialAccount | null): void => {
  try {
    if (account) {
      localStorage.setItem(trialKey, JSON.stringify(account));
    } else {
      localStorage.removeItem(trialKey);
    }
  } catch {
    // storage unavailable, the trial lasts until the page closes
  }
};

// a trial ruler has no password to type, so an expired session signs back in with the kept one
const renewTrialSession = async (): Promise<boolean> => {
  const trial = getTrial();
  if (!trial) return false;
  const response = await fetch(`${apiBase}/auth/authenticate`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(trial),
  });
  if (!response.ok) return false;
  const { token } = (await response.json()) as { token: string };
  setToken(token);
  return true;
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
  const send = (): Promise<Response> => {
    const current = getToken();
    return fetch(`${apiBase}${path}`, {
      method: init.method ?? "GET",
      headers: {
        "Content-Type": "application/json",
        ...(current ? { Authorization: `Bearer ${current}` } : {}),
      },
      body: init.body === undefined ? undefined : JSON.stringify(init.body),
    });
  };
  let response = await send();
  // the kingdom changed at the same moment (the turn tick, an attack), the server applied nothing, so try once more
  if (response.status === 409) {
    response = await send();
  }

  if (response.status === 401 && token && (await renewTrialSession())) {
    response = await send();
  }
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
