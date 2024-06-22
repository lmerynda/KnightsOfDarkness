import React, { useContext } from 'react';
import { KingdomContext } from '../App';
import Button from '@mui/material/Button';

const Overview: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleSubmit = () => {
        fetch('http://localhost:8080/kingdom/uprzejmy/pass-turn', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({})
        })
            .then((response) => {
                console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
                if (response.ok) {
                    kingdomContext.reloadKingdom();
                }
            })
            .catch((error) => {
                console.error('Error when requesting turn pass: ', error);
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
