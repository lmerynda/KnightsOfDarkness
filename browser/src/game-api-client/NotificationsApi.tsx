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

export async function markNotificationAsRead(notificationId: string): Promise<void> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/notifications/${notificationId}/mark-as-read`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      console.log(`markNotificationAsRead Request successful, status: ${response.status}`);
      return;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Marking notification as read error", error);
    throw error;
  }
}

export async function fetchNotificationsCount(): Promise<number> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/notifications/count`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`fetchNotificationsCount Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Fetching notifications count error", error);
    throw error;
  }
}