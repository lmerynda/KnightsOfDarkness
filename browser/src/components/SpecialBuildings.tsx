import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button, ButtonGroup } from '@mui/material';
import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import { buildSpecialBuildingRequest, demolishSpecialBuildingRequest } from '../game-api-client/KingdomApi';
import SpecialBuildingStart from './SpecialBuildingStart';

const SpecialBuilding: React.FC = () => {
    const [buildInputs, setBuildInputs] = React.useState<{ [id: string]: number }>({});

    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const specialBuildings = kingdomContext.kingdom.specialBuildings;

    function handleMaxInput(id: string, pointsToPut: number): void {
        if (kingdomContext === undefined) {
            return;
        }
        const availableBuildingPoints = kingdomContext.kingdom.resources.buildingPoints;
        const maxPoints = Math.min(availableBuildingPoints, pointsToPut);
        setBuildInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: maxPoints,
        }));
    }

    const handleInputChange = (id: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(event.target.value);
        setBuildInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: value,
        }));
    };

    const clearForm = (id: string) => {
        setBuildInputs((prevInputs) => ({
            ...prevInputs,
            [id]: 0,
        }));
    };

    const handleBuild = async (id: string, buildingPoints: number) => {
        if (kingdomContext === undefined || buildingPoints <= 0) {
            return;
        }

        const data = await buildSpecialBuildingRequest(id, buildingPoints);

        clearForm(id);
        kingdomContext.reloadKingdom();
    };
    const handleDemolish = async (id: string) => {
        const data = await demolishSpecialBuildingRequest(id);
        kingdomContext.reloadKingdom();
    }

    return (
        <div>
            <h2>Special Buildings</h2>
            <SpecialBuildingStart />
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Special Building</TableCell>
                        <TableCell>Level</TableCell>
                        <TableCell>Progress</TableCell>
                        <TableCell>Actions</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {specialBuildings.map((specialBuilding) => (
                        <TableRow key={specialBuilding.id}>
                            <TableCell>{specialBuilding.buildingType}</TableCell>
                            <TableCell>{specialBuilding.level}</TableCell>
                            <TableCell>{specialBuilding.buildingPointsPut} / {specialBuilding.buildingPointsRequired}</TableCell>
                            <TableCell>
                                <Input
                                    type="number"
                                    inputProps={{ min: 0 }}
                                    value={buildInputs[specialBuilding.id] || 0}
                                    onChange={handleInputChange(specialBuilding.id)} />
                                <ButtonGroup>
                                    <Button onClick={() => handleBuild(specialBuilding.id, buildInputs[specialBuilding.id] || 0)}>Build</Button>
                                    <Button onClick={() => handleMaxInput(specialBuilding.id, specialBuilding.buildingPointsRequired - specialBuilding.buildingPointsPut)}>Max</Button>
                                    <Button variant="contained" onClick={() => handleDemolish(specialBuilding.id)} color="error">Demolish</Button>
                                </ButtonGroup>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default SpecialBuilding;
