import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import { Typography } from "@mui/material";

const TurnReport: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }
  const lastTurnReport = kingdomContext.kingdom.lastTurnReport;

  return (
    <div>
      <Typography variant="h5">Last turn report</Typography>
      <div style={{ marginLeft: "1rem" }}>
        <Typography variant="body1">Food consumed: {lastTurnReport.foodConsumed}</Typography>
        <Typography variant="body1">Arriving people: {lastTurnReport.arrivingPeople}</Typography>
        <Typography variant="body1">Exiled People: {lastTurnReport.exiledPeople}</Typography>
        <Typography variant="body1">Weapons production percentage: {lastTurnReport.weaponsProductionPercentage}</Typography>
        <Typography variant="h6">Professionals Left</Typography>
        <div style={{ marginLeft: "1rem" }}>
          {Object.entries(lastTurnReport.professionalsLeaving)
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            .filter(([unit, quantity]) => quantity > 0)
            .map(([unit, quantity]) => (
              <Typography variant="body1" key={unit}>
                {unit}: {quantity}
              </Typography>
            ))}
        </div>
        <Typography variant="body1">Kingdom size production bonus: {lastTurnReport.kingdomSizeProductionBonus}</Typography>
        <Typography variant="body1">Nourishment production factor: {lastTurnReport.nourishmentProductionFactor}</Typography>
        <Typography variant="h6">Resources Produced</Typography>
        <div style={{ marginLeft: "1rem" }}>
          {Object.entries(lastTurnReport.resourcesProduced).map(([resource, quantity]) => (
            <Typography variant="body1" key={resource}>
              {resource}: {quantity}
            </Typography>
          ))}
        </div>
        <Typography variant="h6">Special Building Bonus</Typography>
        <div style={{ marginLeft: "1rem" }}>
          {Object.entries(lastTurnReport.specialBuildingBonus).map(([resource, bonus]) => (
            <Typography variant="body1" key={resource}>
              {resource}: {bonus}
            </Typography>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TurnReport;
