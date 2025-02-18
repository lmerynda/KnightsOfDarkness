import { GAME_API } from "../Consts";
import { fetchData, handleResponse } from "./Common";

export type CreateAllianceData = {
  name: string;
};

export type CreateAllianceResult = {
  message: string;
  success: boolean;
};

export type AllianceData = {
  name: string;
  emperor: string;
};

export async function createAllianceRequest(data: CreateAllianceData): Promise<CreateAllianceResult> {
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
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Creating alliance failed", error);
    throw error;
  }
}

export async function fetchAllAlliancesRequest(): Promise<AllianceData[]> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      console.log(`get alliances request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Getting alliances failed", error);
    throw error;
  }
}
