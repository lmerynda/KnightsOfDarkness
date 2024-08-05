import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import { Typography } from '@mui/material';

const TurnReport: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }
    const lastTurnReport = kingdomContext.kingdom.lastTurnReport;

    return (
        <div>
            <Typography variant="h5">Last turn report</Typography>
            <div style={{ marginLeft: '1rem' }}>
                <Typography variant="body1">Food consumed: {lastTurnReport.foodConsumed}</Typography>
                <Typography variant="body1">Arriving people: {lastTurnReport.arrivingPeople}</Typography>
                <Typography variant="body1">Exiled People: {lastTurnReport.exiledPeople}</Typography>
                <Typography variant="body1">Kingdom size production bonus: {lastTurnReport.kingdomSizeProductionBonus}</Typography>
                <Typography variant="body1">Nourishment production factor: {lastTurnReport.nourishmentProductionFactor}</Typography>
                <Typography variant="body1">Special Building Bonus: {lastTurnReport.specialBuildingBonus}</Typography>
                <Typography variant="h6">Resources Produced</Typography>
                <div style={{ marginLeft: '1rem' }}>
                    {Object.entries(lastTurnReport.resourcesProduced).map(([resource, quantity]) => (
                        <Typography variant="body1" key={resource}>{resource}: {quantity}</Typography>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default TurnReport;