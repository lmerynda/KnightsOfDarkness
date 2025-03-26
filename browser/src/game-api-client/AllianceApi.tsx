import { GAME_API } from "../Consts";
import { fetchData, handleResponse } from "./Common";

export type CreateAllianceData = {
  name: string;
};

export type AllianceData = {
  name: string;
  emperor: string;
};

export type AllianceInvitationData = {
  kingdomName: string;
  allianceName: string;
};

export type AcceptAllianceInvitationData = {
  invitationId: string;
};

export type RemoveFromAllianceData = {
  kingdomName: string;
};

export type AddBotToAllianceData = {
  botName: string;
};

export type CreateAllianceResult = {
  success: boolean;
  message: string;
};

export type LeaveAllianceResult = {
  success: boolean;
  message: string;
};

export type InviteAllianceResult = {
  success: boolean;
  message: string;
};

export type AcceptAllianceInvitationResult = {
  success: boolean;
  message: string;
};

export type RemoveFromAllianceResult = {
  success: boolean;
  message: string;
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

export async function leaveAllianceRequest(): Promise<LeaveAllianceResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/leave`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      console.log(`leave alliance request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Leaving alliance failed", error);
    throw error;
  }
}

export async function inviteToAllianceRequest(data: AllianceInvitationData): Promise<InviteAllianceResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/invite`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(data),
      }),
    );

    if (response.ok) {
      console.log(`invite to alliance request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Inviting to alliance failed", error);
    throw error;
  }
}

export async function acceptAllianceInvitationRequest(data: AcceptAllianceInvitationData): Promise<AcceptAllianceInvitationResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/accept`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(data),
      }),
    );

    if (response.ok) {
      console.log(`accept alliance invitation request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Accepting alliance invitation failed", error);
    throw error;
  }
}

export async function removeFromAllianceRequest(data: RemoveFromAllianceData): Promise<RemoveFromAllianceResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/remove`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(data),
      }),
    );

    if (response.ok) {
      console.log(`remove from alliance request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Removing from alliance failed", error);
    throw error;
  }
}

export async function addBotToAllianceRequest(data: AddBotToAllianceData): Promise<void> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/add-bot`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(data),
      }),
    );

    if (response.ok) {
      console.log(`add bot to alliance request successful`);
      return;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Adding bot to alliance failed", error);
    throw error;
  }
}

export async function fetchAllAlliancesRequest(): Promise<AllianceData[]> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/alliance/all`, {
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

export async function fetchAllianceRequest(): Promise<AllianceData> {
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
      console.log(`get alliance request successful`);
      return response.json();
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Getting alliance failed", error);
    throw error;
  }
}
