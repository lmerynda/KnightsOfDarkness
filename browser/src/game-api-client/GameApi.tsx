import { GAME_API } from "../Consts";
import { GameConfig } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export type BuildResponse = {
  [building: string]: number;
};

export type TrainingResponse = {
  [unit: string]: number;
};

export type GameConfigResponse = {
  [key: string]: any;
};

export async function fetchGameConfigRequest(): Promise<GameConfig> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/config`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") || ""}`,
        },
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`fetchGameConfigRequest Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Fetching game configuration data error", error);
    throw error;
  }
}
