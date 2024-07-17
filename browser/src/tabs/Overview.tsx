import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import Button from '@mui/material/Button';
import { GAME_API } from '../Consts';
import TurnReport from '../components/TurnReport';
import BuyLand from '../components/BuyLand';

const Overview: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleSubmit = () => {
        fetch(`${GAME_API}/kingdom/pass-turn`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify({})
        })
            .then((response) => {
                if (response.status === 401) {
                    // redirect to localhost:3000/login
                    window.location.href = '/login';
                }

                if (response.ok) {
                    console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
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
            <BuyLand />
            <TurnReport />
        </div>
    );
};

export default Overview;
