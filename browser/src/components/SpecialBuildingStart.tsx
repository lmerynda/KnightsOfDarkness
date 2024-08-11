import { Select, MenuItem, Grid, Button } from '@mui/material';
import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import { specialBuildingTypes } from '../GameTypes';
import { startSpecialBuildingRequest } from '../game-api-client/KingdomApi';

type BuildingType = typeof specialBuildingTypes[number];

const SpecialBuildingStart: React.FC = () => {
    const [newBuilding, setNewBuilding] = React.useState<BuildingType>("goldShaft");

    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const specialBuildings = kingdomContext.kingdom.specialBuildings;

    // this 5 const should come from kingdom?
    if (specialBuildings.length >= 5) {
        return (
            <div>
                <span>Max Special buildings reached</span>
            </div>
        );
    }

    const handleStartSpecialBuilding = async () => {
        const data = await startSpecialBuildingRequest(newBuilding);
        setNewBuilding("goldShaft");
        kingdomContext.reloadKingdom();
    }

    return (
        <div>
            <Grid container spacing={2} alignItems="center">
                <Grid item>
                    <Select value={newBuilding}>
                        {specialBuildingTypes.map((building) => (
                            <MenuItem key={building} value={building} onClick={() => setNewBuilding(building)}>{building}</MenuItem>
                        ))}
                    </Select>
                </Grid>
                <Grid item>
                    <Button variant="contained" onClick={handleStartSpecialBuilding}>Start building</Button>
                </Grid>
            </Grid>

        </div>
    );
};

export default SpecialBuildingStart;
