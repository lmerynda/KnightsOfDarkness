import { Table, TableBody, TableCell, TableHead, TableRow, Button, Input } from "@mui/material";
import React, { useContext, useState } from "react";
import type { TrainingActionReport, Unit } from "../GameTypes";
import { units } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { fireUnitsRequest, trainRequest } from "../game-api-client/KingdomApi";
import TrainingReport from "../components/TrainingReport";
import { getOpenPositions } from "../GameUtils";

const Train: React.FC = () => {
  const [unitCounts, setUnitCounts] = useState<{ [unit: string]: number }>({});
  const [lastTrainingReport, setLastTrainingReport] = useState<TrainingActionReport | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleCountChange = (unit: string, value: string): void => {
    const newValue = parseInt(value) || 0;
    setUnitCounts(prevCounts => ({
      ...prevCounts,
      [unit]: newValue,
    }));
  };

  const handleTrain = async (): Promise<void> => {
    const response = await trainRequest(unitCounts);
    setLastTrainingReport(response);
    kingdomContext.reloadKingdom();
    setUnitCounts({});
  };

  const handleFireUnits = async (): Promise<void> => {
    const response = await fireUnitsRequest(unitCounts);
    setLastTrainingReport(response);
    kingdomContext.reloadKingdom();
    setUnitCounts({});
  };

  const howManyUnitsCanAfford = (unit: Unit): number => {
    const singleUnitCost = kingdomContext.gameConfig.trainingCost[unit];
    const resources = kingdomContext.kingdom.resources;
    const canAffordInGold = resources.gold / singleUnitCost.gold;
    const canAffordInMaterials =
      singleUnitCost.tools !== 0 ? resources.tools / singleUnitCost.tools : resources.weapons / singleUnitCost.weapons;
    return Math.floor(Math.min(canAffordInGold, canAffordInMaterials));
  };

  const handleMaxInput = (unit: Unit): void => {
    const openPositions = getOpenPositions(unit, kingdomContext.kingdom, kingdomContext.gameConfig);
    // some units don't have their buildings so it's not a limiting factor
    const maxUnitsToAfford = openPositions ? Math.min(openPositions, howManyUnitsCanAfford(unit)) : howManyUnitsCanAfford(unit);

    setUnitCounts(prevCounts => ({
      ...prevCounts,
      [unit]: maxUnitsToAfford,
    }));
  };

  return (
    <div>
      <h1>Train</h1>
      {lastTrainingReport && <TrainingReport {...lastTrainingReport} />}

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Unit</TableCell>
            <TableCell>Gold</TableCell>
            <TableCell>Tools</TableCell>
            <TableCell>Weapons</TableCell>
            <TableCell>Open Positions</TableCell>
            <TableCell>Train Plan</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {units.map(unit => (
            <TableRow key={unit}>
              <TableCell>{unit}</TableCell>
              <TableCell>{kingdomContext.gameConfig.trainingCost[unit].gold}</TableCell>
              <TableCell>{kingdomContext.gameConfig.trainingCost[unit].tools}</TableCell>
              <TableCell>{kingdomContext.gameConfig.trainingCost[unit].weapons}</TableCell>
              <TableCell>{getOpenPositions(unit, kingdomContext.kingdom, kingdomContext.gameConfig) ?? "N/A"}</TableCell>
              <TableCell>
                <Input
                  type="number"
                  value={unitCounts[unit] || 0}
                  onChange={e => handleCountChange(unit, e.target.value)}
                  inputProps={{ min: 0 }}
                />
              </TableCell>
              <TableCell>
                <Button variant="contained" onClick={() => handleMaxInput(unit)}>
                  Max
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Button variant="contained" onClick={handleTrain}>
        Train
      </Button>
      <Button color="error" variant="contained" onClick={handleFireUnits}>
        Fire
      </Button>
    </div>
  );
};

export default Train;
