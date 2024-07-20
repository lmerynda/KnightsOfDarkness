import { GAME_API } from "../Consts";
import { MarketData, OfferBuyer } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export async function fetchMarketDataRequest(): Promise<MarketData[]> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/market`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
        }));

        if (response.ok) {
            const data = response.json()
            console.log(`fetchMarketData Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
            return data;
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('Fetching market data error', error);
        throw error;
    }
}

export async function buyMarketOfferRequest(id: string, offerBuyer: OfferBuyer): Promise<MarketData[]> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/market/${id}/buy`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify(offerBuyer)
        }));

        if (response.ok) {
            const data = response.json()
            console.log(`buyMarketOffer Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
            return data;
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('Buying market offer error', error);
        throw error;
    }
}


export async function withdrawMarketOfferRequest(id: string): Promise<MarketData[]> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/market/${id}/withdraw`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify({})
        }));

        if (response.ok) {
            const data = response.json()
            console.log(`withdrawMarketOffer Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
            return data;
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('Withdrawing market offer error', error);
        throw error;
    }
}