import React, { useContext } from "react";
import { Drawer, Toolbar, Typography, Box } from "@mui/material";
import type { KingdomData } from "./GameTypes";
import { KingdomContext } from "./Kingdom";

const Sidebar: React.FC<KingdomData> = kingdom => {
  const kingdomContext = useContext(KingdomContext);

  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const totalUnits = kingdomContext.totalUnits;

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 240,
        flexShrink: 0,
        [`& .MuiDrawer-paper`]: { width: 240, boxSizing: "border-box" },
      }}
    >
      <Toolbar />
      <Box sx={{ padding: 2 }}>
        <Typography variant="h6">buildings</Typography>
        <Typography variant="body1">houses: {kingdom.buildings.house}</Typography>
        <Typography variant="body1">goldMines: {kingdom.buildings.goldMine}</Typography>
        <Typography variant="body1">ironMines: {kingdom.buildings.ironMine}</Typography>
        <Typography variant="body1">workshops: {kingdom.buildings.workshop}</Typography>
        <Typography variant="body1">farms: {kingdom.buildings.farm}</Typography>
        <Typography variant="body1">markets: {kingdom.buildings.market}</Typography>
        <Typography variant="body1">barracks: {kingdom.buildings.barracks}</Typography>
        <Typography variant="body1">guardHouses: {kingdom.buildings.guardHouse}</Typography>
        <Typography variant="body1">spyGuilds: {kingdom.buildings.spyGuild}</Typography>
        <Typography variant="body1">towers: {kingdom.buildings.tower}</Typography>
        <Typography variant="body1">castles: {kingdom.buildings.castle}</Typography>
        <Typography variant="h6">units</Typography>
        <Typography variant="body1">
          goldMiners: {kingdom.units.availableUnits.goldMiner} ({totalUnits.goldMiner})
        </Typography>
        <Typography variant="body1">
          ironMiners: {kingdom.units.availableUnits.ironMiner} ({totalUnits.ironMiner})
        </Typography>
        <Typography variant="body1">
          farmers: {kingdom.units.availableUnits.farmer} ({totalUnits.farmer})
        </Typography>
        <Typography variant="body1">
          blacksmiths: {kingdom.units.availableUnits.blacksmith} ({totalUnits.blacksmith})
        </Typography>
        <Typography variant="body1">
          builders: {kingdom.units.availableUnits.builder} ({totalUnits.builder})
        </Typography>
        <Typography variant="body1">
          carriers: {kingdom.units.availableUnits.carrier} ({totalUnits.carrier})
        </Typography>
        <Typography variant="body1">
          guards: {kingdom.units.availableUnits.guard} ({totalUnits.guard})
        </Typography>
        <Typography variant="body1">
          spies: {kingdom.units.availableUnits.spy} ({totalUnits.spy})
        </Typography>
        <Typography variant="body1">
          infantry: {kingdom.units.availableUnits.infantry} ({totalUnits.infantry})
        </Typography>
        <Typography variant="body1">
          bowmen: {kingdom.units.availableUnits.bowman} ({totalUnits.bowman})
        </Typography>
        <Typography variant="body1">
          cavalry: {kingdom.units.availableUnits.cavalry} ({totalUnits.cavalry})
        </Typography>
      </Box>
    </Drawer>
  );
};

export default Sidebar;
