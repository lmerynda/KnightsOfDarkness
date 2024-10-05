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
