import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from '@mui/material';
import React, { useState } from 'react';
import { KingdomReloader } from '../App';
import { buildingList } from '../GameTypes';

const Build: React.FC<KingdomReloader> = ({ reloadKingdom }) => {
    const [buildingCounts, setBuildingCounts] = useState<{ [building: string]: number }>({});

    const handleCountChange = (building: string, count: number) => {
        if (count >= 0) {
            setBuildingCounts((prevCounts) => ({
                ...prevCounts,
                [building]: count
            }));
        }
    };

    const handleSubmit = () => {
        fetch('http://localhost:8080/kingdom/uprzejmy/build', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(buildingCounts)
        })
            .then((response) => {
                console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
                if (response.ok) {
                    reloadKingdom();
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
            <Button variant="contained" onClick={handleSubmit}>Submit</Button>
        </div>
    );
};

export default Build;
