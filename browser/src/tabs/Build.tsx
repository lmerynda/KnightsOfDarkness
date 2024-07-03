import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from '@mui/material';
import React, { useContext, useState } from 'react';
import { buildingList } from '../GameTypes';
import { GAME_API } from '../Consts';
import { KingdomContext } from '../Kingdom';

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

    const handleSubmit = () => {
        fetch(`${GAME_API}/kingdom/uprzejmy/build`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(buildingCounts)
        })
            .then((response) => {
                console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
                if (response.ok) {
                    kingdomContext.reloadKingdom();
                    setBuildingCounts({});
                }
            })
            .catch((error) => {
                console.error('Error requesting kingdom to build: ', error);
            });
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
