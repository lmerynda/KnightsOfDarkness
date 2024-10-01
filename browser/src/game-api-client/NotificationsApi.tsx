import { GAME_API } from "../Consts";
import { Notification } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export async function fetchNotificationsRequest(): Promise<Notification[]> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/notifications`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`fetchNotifications Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Fetching notifications error", error);
    throw error;
  }
}
