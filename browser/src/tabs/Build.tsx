import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import React, { useState } from 'react';
import { KingdomReloader } from '../App';

const buildingList = [
    "houses",
    "goldMines",
    "ironMines",
    "workshops",
    "farms",
    "markets",
    "barracks",
    "guardHouses",
    "spyGuilds",
    "towers",
    "castles"
];



const Build: React.FC<KingdomReloader> = ({ reloadKingdom }) => {
    const [buildingCounts, setBuildingCounts] = useState<{ [building: string]: number }>({});

    const handleCountChange = (building: string, count: number) => {
        setBuildingCounts((prevCounts) => ({
            ...prevCounts,
            [building]: count
        }));
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
                // Handle response
                if (response.ok) {
                    reloadKingdom(); // Invoke the callback to reload the kingdom
                }
            })
            .catch((error) => {
                // Handle error
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
                                <input
                                    type="number"
                                    value={buildingCounts[building] || 0}
                                    onChange={(e) => handleCountChange(building, parseInt(e.target.value))}
                                />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
            <button onClick={handleSubmit}>Submit</button>
        </div>
    );
};

export default Build;
