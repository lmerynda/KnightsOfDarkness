import React, { useContext, useState } from "react";
import { KingdomContext } from "../Kingdom";
import Button from "@mui/material/Button";
import TurnReport from "../components/TurnReport";
import BuyLand from "../components/BuyLand";
import { passTurnRequest } from "../game-api-client/KingdomApi";
import type { PassTurnReport } from "../GameTypes";
import { Box, Grid, Input, InputLabel } from "@mui/material";

const Overview: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  const [lastTurnReport, setLastTurnReport] = useState<PassTurnReport | undefined>(undefined);
  const [weaponsProductionPercentage, setWeaponsProductionPercentage] = useState<number>(0);

  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSubmit = async (): Promise<void> => {
    const response = await passTurnRequest(weaponsProductionPercentage);
    setLastTurnReport(response);
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <h1>Overview</h1>
      <Grid container spacing={2} alignItems="center">
        <Grid item>
          <BuyLand />
        </Grid>

        <Grid item>
          <InputLabel>Weapons Production %</InputLabel>
          <Input
            type="number"
            value={weaponsProductionPercentage}
            onChange={event => setWeaponsProductionPercentage(parseInt(event.target.value))}
            inputProps={{ min: 0, max: 100 }}
          />
        </Grid>
      </Grid>

      <Grid item>
        <Button variant="contained" onClick={handleSubmit}>
          Pass Turn
        </Button>
      </Grid>

      {lastTurnReport && !lastTurnReport.success && (
        <Grid item>
          <Box component="div" sx={{ display: "inline", color: "red" }}>
            {lastTurnReport.message}
          </Box>
        </Grid>
      )}

      <Grid item>
        <TurnReport />
      </Grid>
    </div>
  );
};

export default Overview;
