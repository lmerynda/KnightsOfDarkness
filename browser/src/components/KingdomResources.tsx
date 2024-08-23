import React from "react";
import { KingdomResources } from "../GameTypes";

const KingdomResourcesView: React.FC<KingdomResources> = kingdomResources => {
  return (
    <div className="resources-container">
      <span>gold: {kingdomResources.gold}</span>
      <span>food: {kingdomResources.food}</span>
      <span>iron: {kingdomResources.iron}</span>
      <span>tools: {kingdomResources.tools}</span>
      <span>weapons: {kingdomResources.weapons}</span>
      <span>
        land: {kingdomResources.usedLand} / {kingdomResources.land}
      </span>
      <span>unemployed: {kingdomResources.unemployed}</span>
      <span>buildingPoints: {kingdomResources.buildingPoints}</span>
      <span>turns: {kingdomResources.turns}</span>
    </div>
  );
};
export default KingdomResourcesView;
