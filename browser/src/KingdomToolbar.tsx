import React from "react";
import KingdomResourcesView from "./components/KingdomResources";
import { KingdomDetails, KingdomResources } from "./GameTypes";

export type KingdomToolbarProps = {
  kingdomName: string;
  kingdomResources: KingdomResources;
  kingdomDetails: KingdomDetails;
};

const KingdomToolbar: React.FC<KingdomToolbarProps> = ({ kingdomName, kingdomResources, kingdomDetails }) => {
  return (
    <div>
      <h2>Kingdom {kingdomName}</h2>
      <KingdomResourcesView kingdomResources={kingdomResources} kingdomDetails={kingdomDetails} />
    </div>
  );
};

export default KingdomToolbar;
