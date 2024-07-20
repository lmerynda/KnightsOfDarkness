import { GAME_API } from "../Consts";
import { KingdomData } from "../GameTypes";
import { fetchData, handleResponse } from "./Common";

export async function fetchKingdomDataRequest(): Promise<KingdomData> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/kingdom`, {
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

// TODO return pass-turn result which we get as a response
export async function passTurnRequest(): Promise<Response> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/kingdom/pass-turn`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
        }));

        if (response.ok) {
            console.log(`pass-turn request successful`);
            return response.json();
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('pass-turn error:', error);
        throw error;
    }
}

export async function buildRequest(data: { [building: string]: number }): Promise<Response> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/kingdom/build`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify(data)
        }));

        if (response.ok) {
            console.log(`build request successful`);
            return response.json();
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('build error:', error);
        throw error;
    }
}

export async function trainRequest(data: { [unit: string]: number }): Promise<Response> {
    try {
        const response = await handleResponse(fetchData(`${GAME_API}/kingdom/train`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify(data)
        }));

        if (response.ok) {
            console.log(`train request successful`);
            return response.json();
        }

        throw new Error(`request failed, status: ${response.status}`);
    } catch (error) {
        console.error('train error:', error);
        throw error;
    }
}
