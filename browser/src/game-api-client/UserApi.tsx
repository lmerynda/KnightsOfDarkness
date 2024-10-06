import { GAME_API } from "../Consts";
import { fetchData, handleResponse } from "./Common";

export async function validateTokenRequest(token: string): Promise<boolean> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/auth/validate-token`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      }),
    );

    if (response.ok) {
      console.log(`validateToken Request successful, status: ${response.status}`);
      return true;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Validating token error", error);
    return false;
  }
}

export async function authenticateRequest(email: string, password: string): Promise<string | undefined> {
  const authRequest = {
    email: email,
    password: password,
  };
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/auth/authenticate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(authRequest),
      }),
    );

    if (response.ok) {
      const data = await response.json();
      console.log(`authenticate Request successful, status: ${response.status}`);
      return data.token;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Authentication error", error);
    return undefined;
  }
}

export async function registerRequest(email: string, kingdomName: string, password: string): Promise<boolean> {
  const authRequest = {
    email: email,
    kingdomName: kingdomName,
    password: password,
  };
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(authRequest),
      }),
    );

    if (response.ok) {
      console.log(`register Request successful, status: ${response.status}`);
      return true;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Register error", error);
    return false;
  }
}
