import { GAME_API } from "../Consts";
import { fetchData, handleResponse } from "./Common";

export type CreateAllianceData = {
  name: string;
};

export async function createAllianceRequest(data: CreateAllianceData): Promise<void> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/create`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(data),
      }),
    );

    if (response.ok) {
      console.log(`create alliance request successful`);
      return;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Creating alliance failed", error);
    throw error;
  }
}
