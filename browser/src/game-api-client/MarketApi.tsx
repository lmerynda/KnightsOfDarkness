import { GAME_API } from "../Consts";
import { MarketData } from "../GameTypes";
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
            console.log(`fetchKingdomData Request successful, status: ${response.status} data: ${JSON.stringify(data)}`);
            return data;
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('Fetching kingdom data error', error);
        throw error;
    }
}