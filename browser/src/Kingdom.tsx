import React from "react";
import "./css/App.css";
import { Box } from "@mui/material";
import Sidebar from "./Sidebar";
import KingdomTabs from "./KingdomTabs";
import KingdomToolbar from "./KingdomToolbar";
import { GameConfig, KingdomData } from "./GameTypes";
import { fetchKingdomDataRequest } from "./game-api-client/KingdomApi";
import { kingdomRefreshInterval } from "./Consts";
import { fetchGameConfigRequest } from "./game-api-client/GameApi";

export type KingdomContextType = {
  kingdom: KingdomData;
  gameConfig: GameConfig;
  reloadKingdom: () => Promise<void>;
};

export const KingdomContext = React.createContext<KingdomContextType | undefined>(undefined);

const Kingdom: React.FC = () => {
  const [kingdom, setKingdom] = React.useState<KingdomData>();
  const [gameConfig, setGameConfig] = React.useState<GameConfig>();

  const reloadKingdom = async () => {
    const data = await fetchKingdomDataRequest();
    setKingdom(data);
  };

  const reloadGameConfig = async () => {
    const data = await fetchGameConfigRequest();
    setGameConfig(data);
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
        <KingdomContext.Provider value={{ kingdom, gameConfig, reloadKingdom }}>
          <Sidebar {...kingdom} />
          <Box component="main" sx={{ flexGrow: 1, bgcolor: "background.default", p: 3 }}>
            <KingdomToolbar kingdomName={kingdom.name} kingdomResources={kingdom.resources} />
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
