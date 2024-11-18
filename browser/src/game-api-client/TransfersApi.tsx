import { GAME_API } from "../Consts";
import { fetchData, handleResponse } from "./Common";

export async function withdrawCarriers(carriersOnTheMoveId: string): Promise<void> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/withdraw-carriers`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(carriersOnTheMoveId),
      }),
    );

    if (response.ok) {
      console.log(`withdrawCarriers request successful`);
      return;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Withdrawing carriers failed", error);
    throw error;
  }
}
