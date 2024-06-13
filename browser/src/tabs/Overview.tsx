import React from 'react';
import { KingdomReloader } from '../App';
import Button from '@mui/material/Button';

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
            <Button variant="contained" onClick={handleSubmit}>Pass Turn</Button>
        </div>
    );
};

export default Overview;
