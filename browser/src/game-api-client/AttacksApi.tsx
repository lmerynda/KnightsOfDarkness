import { GAME_API } from "../Consts";
import type { AttackType, UnitsMap } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export type SendAttackData = {
  destinationKingdomName: string;
  attackType: AttackType;
  units: Partial<UnitsMap>;
};

export type SendAttackResult = {
  message: string;
  success: boolean;
  data?: SendAttackData;
};

export async function withdrawAttack(attackId: string): Promise<void> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/withdraw-attack`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(attackId),
      }),
    );

    if (response.ok) {
      console.log(`withdrawAttack request successful`);
      return;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Withdrawing attack failed", error);
    throw error;
  }
}

export async function sendAttack(sendAttackData: SendAttackData): Promise<SendAttackResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/kingdom/send-attack`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(sendAttackData),
      }),
    );

    if (response.ok) {
      console.log(`sendAttack request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Sending attack failed", error);
    throw error;
  }
}
