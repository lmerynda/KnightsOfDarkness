import React, { useContext, useState } from "react";
import { KingdomContext } from "../Kingdom";
import Button from "@mui/material/Button";
import TurnReport from "../components/TurnReport";
import BuyLand from "../components/BuyLand";
import { passTurnRequest } from "../game-api-client/KingdomApi";
import type { PassTurnReport } from "../GameTypes";
import { Box } from "@mui/material";

const Overview: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  const [lastTurnReport, setLastTurnReport] = useState<PassTurnReport | undefined>(undefined);

  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSubmit = async (): Promise<void> => {
    const response = await passTurnRequest();
    setLastTurnReport(response);
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <h1>Overview</h1>
      <Button variant="contained" onClick={handleSubmit}>
        Pass Turn
      </Button>
      {lastTurnReport && !lastTurnReport.success && (
        <Box component="div" sx={{ display: "inline", color: "red" }}>
          {lastTurnReport.message}
        </Box>
      )}
      <BuyLand />
      <TurnReport />
    </div>
  );
};

export default Overview;
