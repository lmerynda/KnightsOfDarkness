import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from '@mui/material';
import React, { useContext, useState } from 'react';
import { buildingList } from '../GameTypes';
import { KingdomContext } from '../Kingdom';
import { buildRequest } from '../game-api-client/KingdomApi';

const Build: React.FC = () => {
    const [buildingCounts, setBuildingCounts] = useState<{ [building: string]: number }>({});
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
        kingdomContext.reloadKingdom();
        setBuildingCounts({});
    };

    return (
        <div>
            <h1>Build</h1>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Building</TableCell>
                        <TableCell>Count</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {buildingList.map((building) => (
                        <TableRow key={building}>
                            <TableCell>{building}</TableCell>
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
        </div>
    );
};

export default Build;
