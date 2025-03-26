import React, { useContext } from "react";
import AllianceList from "../components/AllianceList";
import CreateAlliance from "../components/CreateAlliance";
import { KingdomContext } from "../Kingdom";

const Alliance: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  return <div>{kingdomContext.kingdom.allianceName ? <AllianceList /> : <CreateAlliance />}</div>;
};

export default Alliance;
