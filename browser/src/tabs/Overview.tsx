import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import Button from "@mui/material/Button";
import TurnReport from "../components/TurnReport";
import BuyLand from "../components/BuyLand";
import { passTurnRequest } from "../game-api-client/KingdomApi";

const Overview: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSubmit = async () => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const data = await passTurnRequest();
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <h1>Overview</h1>
      <Button variant="contained" onClick={handleSubmit}>
        Pass Turn
      </Button>
      <BuyLand />
      <TurnReport />
    </div>
  );
};

export default Overview;
