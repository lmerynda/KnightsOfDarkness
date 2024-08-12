import { Table, TableBody, TableCell, TableHead, TableRow, Button, Input } from '@mui/material';
import React, { useContext, useState } from 'react';
import { units } from '../GameTypes';
import { KingdomContext } from '../Kingdom';
import { trainRequest } from '../game-api-client/KingdomApi';
import TrainingReport from '../components/TrainingReport';
import { getOpenPositions } from '../GameUtils';

const Train: React.FC = () => {
    const [unitCounts, setUnitsCounts] = useState<{ [unit: string]: number }>({});
    const [lastTrainingReport, setLastTrainingReport] = useState<{ [unit: string]: number }>({});
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleCountChange = (unit: string, count: number) => {
        if (count >= 0) {
            setUnitsCounts((prevCounts) => ({
                ...prevCounts,
                [unit]: count
            }));
        }
    };

    const handleSubmit = async () => {
        const data = await trainRequest(unitCounts);
        setLastTrainingReport(data);
        kingdomContext.reloadKingdom();
        setUnitsCounts({});
    };

    return (
        <div>
            <h1>Train</h1>
            <TrainingReport {...lastTrainingReport} />
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Unit</TableCell>
                        <TableCell>Gold</TableCell>
                        <TableCell>Tools</TableCell>
                        <TableCell>Weapons</TableCell>
                        <TableCell>Open Positions</TableCell>
                        <TableCell>Train Plan</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {units.map((unit) => (
                        <TableRow key={unit}>
                            <TableCell>{unit}</TableCell>
                            <TableCell>{kingdomContext.gameConfig.trainingCost[unit].gold}</TableCell>
                            <TableCell>{kingdomContext.gameConfig.trainingCost[unit].tools}</TableCell>
                            <TableCell>{kingdomContext.gameConfig.trainingCost[unit].weapons}</TableCell>
                            <TableCell>{getOpenPositions(unit, kingdomContext.kingdom, kingdomContext.gameConfig) || "N/A"}</TableCell>
                            <TableCell>
                                <Input
                                    type="number"
                                    value={unitCounts[unit] || 0}
                                    onChange={(e) => handleCountChange(unit, parseInt(e.target.value))}
                                    inputProps={{ min: 0 }}
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
            <Button variant="contained" onClick={handleSubmit}>Train</Button>
        </div>
    );
};

export default Train;
