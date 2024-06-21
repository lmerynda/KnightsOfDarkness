import { Table, TableBody, TableCell, TableHead, TableRow, Button, Input } from '@mui/material';
import React, { useState } from 'react';
import { KingdomReloader } from '../App';
import { unitList } from '../GameTypes';

const Train: React.FC<KingdomReloader> = ({ reloadKingdom }) => {
    const [unitCounts, setUnitsCounts] = useState<{ [unit: string]: number }>({});

    const handleCountChange = (unit: string, count: number) => {
        if (count >= 0) {
            setUnitsCounts((prevCounts) => ({
                ...prevCounts,
                [unit]: count
            }));
        }
    };

    const handleSubmit = () => {
        fetch('http://localhost:8080/kingdom/uprzejmy/train', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(unitCounts)
        })
            .then((response) => {
                console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
                if (response.ok) {
                    reloadKingdom();
                    setUnitsCounts({});
                }
            })
            .catch((error) => {
                console.error('Error requesting kingdom to train: ', error);
            });
    };

    return (
        <div>
            <h1>Train</h1>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Unit</TableCell>
                        <TableCell>Count</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {unitList.map((unit) => (
                        <TableRow key={unit}>
                            <TableCell>{unit}</TableCell>
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
            <Button variant="contained" onClick={handleSubmit}>Submit</Button>
        </div>
    );
};

export default Train;
