import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from '@mui/material';
import React, { useContext, useState } from 'react';
import { buildingList } from '../GameTypes';
import { KingdomContext } from '../Kingdom';
import { buildRequest } from '../game-api-client/KingdomApi';
import BuildReport from '../components/BuildReport';

const SpecialBuilding: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const specialBuildings = kingdomContext.kingdom.specialBuildings;

    return (
        <div>
            <h2>Special Buildings</h2>

            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Special Building</TableCell>
                        <TableCell>Level</TableCell>
                        <TableCell>Progress</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {specialBuildings.map((specialBuilding) => (
                        <TableRow key={specialBuilding.id}>
                            <TableCell>{specialBuilding.buildingType}</TableCell>
                            <TableCell>{specialBuilding.level}</TableCell>
                            <TableCell>{specialBuilding.buildingPointsPut} / {specialBuilding.buildingPointsRequired}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default SpecialBuilding;
