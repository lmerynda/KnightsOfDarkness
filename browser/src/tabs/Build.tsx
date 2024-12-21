import { Table, TableBody, TableCell, TableHead, TableRow, Input, Button } from "@mui/material";
import React, { useContext, useState } from "react";
import type { Building, BuildingActionReport } from "../GameTypes";
import { buildings } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { buildRequest, demolishRequest } from "../game-api-client/KingdomApi";
import BuildReport from "../components/BuildReport";
import SpecialBuilding from "../components/SpecialBuildings";
import { getBuildingOccupants, getTotalCapacity } from "../GameUtils";

const Build: React.FC = () => {
  const [buildingCounts, setBuildingCounts] = useState<{ [building: string]: number }>({});
  const [lastBuildReport, setLastBuildReport] = useState<BuildingActionReport | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleCountChange = (building: string, value: string): void => {
    const newValue = parseInt(value) || 0;
    setBuildingCounts(prevCounts => ({
      ...prevCounts,
      [building]: newValue,
    }));
  };

  const handleBuild = async (): Promise<void> => {
    const response = await buildRequest(buildingCounts);
    setLastBuildReport(response);
    kingdomContext.reloadKingdom();
    setBuildingCounts({});
  };

  const handleDemolish = async (): Promise<void> => {
    const response = await demolishRequest(buildingCounts);
    setLastBuildReport(response); // TODO it should be demolish report, think how to generalize
    kingdomContext.reloadKingdom();
    setBuildingCounts({});
  };

  const howManyBuildingsCanAfford = (building: Building): number => {
    const singleBuildingCost = kingdomContext.gameConfig.buildingPointCosts[building];
    const buildingPoints = kingdomContext.kingdom.resources.buildingPoints;
    const maxToAfford = Math.floor(buildingPoints / singleBuildingCost);
    return maxToAfford;
  };

  const handleMaxInput = (building: Building): void => {
    const availableLand = kingdomContext.kingdom.resources.land - kingdomContext.kingdom.details.usedLand;
    const maxBuildingsToAfford = howManyBuildingsCanAfford(building);
    const maxBuildings = Math.min(maxBuildingsToAfford, availableLand);
    setBuildingCounts(prevCounts => ({
      ...prevCounts,
      [building]: maxBuildings,
    }));
  };

  return (
    <div>
      <h1>Build</h1>
      {lastBuildReport && <BuildReport {...lastBuildReport} />}

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Building</TableCell>
            <TableCell>Count</TableCell>
            <TableCell>Occupancy</TableCell>
            <TableCell>Cost</TableCell>
            <TableCell>Build Plan</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {buildings.map(building => (
            <TableRow key={building}>
              <TableCell>{building}</TableCell>
              <TableCell>{kingdomContext.kingdom.buildings[building]}</TableCell>
              <TableCell>
                {getBuildingOccupants(building, kingdomContext.kingdom)}/
                {getTotalCapacity(building, kingdomContext.kingdom, kingdomContext.gameConfig)}
              </TableCell>
              <TableCell>{kingdomContext.gameConfig.buildingPointCosts[building]}</TableCell>
              <TableCell>
                <Input
                  type="number"
                  value={buildingCounts[building] || 0}
                  onChange={e => handleCountChange(building, e.target.value)}
                  inputProps={{ min: 0 }}
                />
              </TableCell>
              <TableCell>
                <Button variant="contained" onClick={() => handleMaxInput(building)}>
                  Max
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Button variant="contained" onClick={handleBuild}>
        Build
      </Button>
      <Button color="error" variant="contained" onClick={handleDemolish}>
        Demolish
      </Button>
      <SpecialBuilding />
    </div>
  );
};

export default Build;
