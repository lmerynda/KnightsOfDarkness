import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import Button from '@mui/material/Button';
import { GAME_API } from '../Consts';
import { Typography } from '@mui/material';

const Overview: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }
    const lastTurnReport = kingdomContext.kingdom.lastTurnReport;

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
            <Typography variant="h5">Last turn report</Typography>
            <div style={{ marginLeft: '1rem' }}>
                <Typography variant="body1">Food consumed: {lastTurnReport.foodConsumed}</Typography>
                <Typography variant="h6">Resources Produced</Typography>
                <div style={{ marginLeft: '1rem' }}>
                    {Object.entries(lastTurnReport.resourcesProduced).map(([resource, quantity]) => (
                        <Typography variant="body1" key={resource}>{resource}: {quantity}</Typography>
                    ))}
                </div>
                <Typography variant="body1">Arriving people: {lastTurnReport.arrivingPeople}</Typography>
                <Typography variant="body1">Exiled People: {lastTurnReport.exiledPeople}</Typography>
                <Typography variant="body1">Kingdom size production bonus: {lastTurnReport.kingdomSizeProductionBonus}</Typography>
                <Typography variant="body1">Nourishment prduction factor: {lastTurnReport.nourishmentProductionFactor}</Typography>
            </div>
        </div>
    );
};

export default Overview;
