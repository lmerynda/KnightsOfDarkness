import React from 'react';
import { KingdomReloader } from '../App';

const Overview: React.FC<KingdomReloader> = ({ reloadKingdom }) => {

    const handleSubmit = () => {
        fetch('http://localhost:8080/kingdom/uprzejmy/pass-turn', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({})
        })
            .then((response) => {
                if (response.ok) {
                    reloadKingdom();
                }
            })
            .catch((error) => {
                // TODO handle error
                console.error('Error:', error);
            });
    };

    return (
        <div>
            <h1>Overview</h1>
            <button onClick={handleSubmit}>Pass Turn</button>
        </div>
    );
};

export default Overview;
