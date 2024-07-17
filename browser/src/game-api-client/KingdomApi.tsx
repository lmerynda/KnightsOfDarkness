import { GAME_API } from "../Consts";
import { KingdomData } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export async function fetchKingdomData(): Promise<KingdomData> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/kingdom`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
        }));

        const kingdom = await response.json();
        console.log(`fetchKingdomData Request successful, status: ${response.status} data: ${JSON.stringify(kingdom)}`);
        return kingdom;
    } catch (error) {
        console.error('Fetching kingdom data for reload has failed:', error);
        throw error;
    }
}