import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from '@mui/material';
import React, { useContext, useState } from 'react';
import { buildings } from '../GameTypes';
import { KingdomContext } from '../Kingdom';
import { buildRequest } from '../game-api-client/KingdomApi';
import BuildReport from '../components/BuildReport';
import SpecialBuilding from '../components/SpecialBuildings';
import { getBuildingOccupants, getTotalCapacity } from '../GameUtils';

const Build: React.FC = () => {
    const [buildingCounts, setBuildingCounts] = useState<{ [building: string]: number }>({});
    const [lastBuildReport, setLastBuildReport] = useState<{ [building: string]: number }>({});
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }


    const handleCountChange = (building: string, count: number) => {
        if (count >= 0) {
            setBuildingCounts((prevCounts) => ({
                ...prevCounts,
                [building]: count
            }));
        }
    };

    const handleSubmit = async () => {
        const data = await buildRequest(buildingCounts);
        setLastBuildReport(data);
        kingdomContext.reloadKingdom();
        setBuildingCounts({});
    };

    return (
        <div>
            <h1>Build</h1>
            <BuildReport {...lastBuildReport} />

            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Building</TableCell>
                        <TableCell>Cost</TableCell>
                        <TableCell>Occupancy</TableCell>
                        <TableCell>Count</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {buildings.map((building) => (
                        <TableRow key={building}>
                            <TableCell>{building}</TableCell>
                            <TableCell>{kingdomContext.gameConfig.buildingPointCosts[building]}</TableCell>
                            <TableCell>{getBuildingOccupants(building, kingdomContext.kingdom)}/{getTotalCapacity(building, kingdomContext.kingdom, kingdomContext.gameConfig)}</TableCell>
                            <TableCell>
                                <Input
                                    type="number"
                                    value={buildingCounts[building] || 0}
                                    onChange={(e) => handleCountChange(building, parseInt(e.target.value))}
                                    inputProps={{ min: 0 }}
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
            <Button variant="contained" onClick={handleSubmit}>Build</Button>
            <SpecialBuilding />
        </div>
    );
};

export default Build;
