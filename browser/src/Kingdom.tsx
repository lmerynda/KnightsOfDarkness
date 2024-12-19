import React from "react";
import "./css/App.css";
import { Box } from "@mui/material";
import Sidebar from "./Sidebar";
import KingdomTabs from "./KingdomTabs";
import KingdomToolbar from "./KingdomToolbar";
import type { GameConfig, KingdomData, UnitsMap } from "./GameTypes";
import { fetchKingdomDataRequest } from "./game-api-client/KingdomApi";
import { kingdomRefreshInterval } from "./Consts";
import { fetchGameConfigRequest } from "./game-api-client/GameApi";

export type KingdomContextType = {
  kingdom: KingdomData;
  gameConfig: GameConfig;
  reloadKingdom: () => Promise<void>;
  totalUnits: UnitsMap;
};

// TODO fix this lint warning
// eslint-disable-next-line react-refresh/only-export-components
export const KingdomContext = React.createContext<KingdomContextType | undefined>(undefined);

const Kingdom: React.FC = () => {
  const [kingdom, setKingdom] = React.useState<KingdomData>();
  const [gameConfig, setGameConfig] = React.useState<GameConfig>();

  const reloadKingdom = async (): Promise<void> => {
    const data = await fetchKingdomDataRequest();
    setKingdom(data);
  };

  const reloadGameConfig = async (): Promise<void> => {
    const data = await fetchGameConfigRequest();
    setGameConfig(data);
  };

  const calculateTotalUnits = (availableUnits: UnitsMap, mobileUnits: UnitsMap): UnitsMap => {
    const totalUnits: UnitsMap = { ...availableUnits };
    for (const unit in mobileUnits) {
      totalUnits[unit] += mobileUnits[unit];
    }
    return totalUnits;
  };

  React.useEffect(() => {
    reloadKingdom();
    reloadGameConfig();

    const interval = setInterval(() => {
      reloadKingdom();
    }, kingdomRefreshInterval);

    return () => {
      clearInterval(interval);
    };
  }, []);

  return (
    <Box sx={{ display: "flex" }}>
      {kingdom && gameConfig ? (
        <KingdomContext.Provider value={{ kingdom, gameConfig, reloadKingdom, totalUnits: calculateTotalUnits(kingdom.units.availableUnits, kingdom.units.mobileUnits) }}>
          <Sidebar {...kingdom} />
          <Box component="main" sx={{ flexGrow: 1, bgcolor: "background.default", p: 3 }}>
            <KingdomToolbar kingdomName={kingdom.name} kingdomResources={kingdom.resources} kingdomDetails={kingdom.details} />
            <KingdomTabs />
          </Box>
        </KingdomContext.Provider>
      ) : (
        <div>Loading...</div>
      )}
    </Box>
  );
};

export default Kingdom;
