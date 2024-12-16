import { GAME_API } from "../Consts";
import type { MarketOfferCreateResult, MarketOfferData, MarketResource, OfferBuyer } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export type CreateMarketOfferData = {
  resource: MarketResource;
  price: number;
  count: number;
};

export type MarketOfferBuyResponse = {
  resource: MarketResource;
  count: number;
  pricePerUnit: number;
  totalCost: number;
};

export async function fetchMarketDataRequest(resource: MarketResource): Promise<MarketOfferData[]> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/market/${resource}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`fetchMarketData Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Fetching market data error", error);
    throw error;
  }
}

export async function buyMarketOfferRequest(id: string, offerBuyer: OfferBuyer): Promise<MarketOfferBuyResponse> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/market/${id}/buy`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(offerBuyer),
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`buyMarketOffer Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Buying market offer error", error);
    throw error;
  }
}

export async function withdrawMarketOfferRequest(id: string): Promise<MarketOfferData[]> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/market/${id}/withdraw`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify({}),
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`withdrawMarketOffer Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Withdrawing market offer error", error);
    throw error;
  }
}

export async function createMarketOfferRequest(offer: CreateMarketOfferData): Promise<MarketOfferCreateResult> {
  try {
    const response = await handleResponse(
      fetchData(`${GAME_API}/market/create`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("authToken") ?? ""}`,
        },
        body: JSON.stringify(offer),
      }),
    );

    if (response.ok) {
      const data = response.json();
      console.log(`createMarketOffer Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
      return data;
    }

    throw new Error(`request failed, status: ${response.status}`);
  } catch (error) {
    console.error("Creating market offer error", error);
    throw error;
  }
}
