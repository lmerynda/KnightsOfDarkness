import { GAME_API } from "../Consts";
import type { MarketResource } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export type SendCarriersData = {
  destinationKingdomName: string;
  resource: MarketResource;
  amount: number;
};

export type SendCarriersResult = {
  message: string;
  success: boolean;
  data?: SendCarriersData;
};

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

export async function sendCarriers(sendCarriersData: SendCarriersData): Promise<SendCarriersResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/send-carriers`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(sendCarriersData),
      }),
    );

    if (response.ok) {
      console.log(`sendCarriers request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Sending carriers failed", error);
    throw error;
  }
}
