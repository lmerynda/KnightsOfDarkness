import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button, ButtonGroup } from '@mui/material';
import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';

const SpecialBuilding: React.FC = () => {
    const [buildInputs, setBuildInputs] = React.useState<{ [id: string]: number }>({});


    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const specialBuildings = kingdomContext.kingdom.specialBuildings;

    function handleMaxInput(id: string, pointsToPut: number): void {
        setBuildInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: pointsToPut,
        }));
    }

    const handleInputChange = (id: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(event.target.value);
        setBuildInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: value,
        }));
    };

    return (
        <div>
            <h2>Special Buildings</h2>

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
                                    <Button>Build</Button>
                                    <Button onClick={() => handleMaxInput(specialBuilding.id, specialBuilding.buildingPointsRequired - specialBuilding.buildingPointsPut)}>Max</Button>
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
