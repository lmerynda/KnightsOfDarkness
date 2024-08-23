// src/Sidebar.tsx
import React from "react";
import { Drawer, Toolbar, Typography, Box } from "@mui/material";
import { KingdomData } from "./GameTypes";

const Sidebar: React.FC<KingdomData> = kingdom => {
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
        <Typography variant="body1">houses: {kingdom.buildings.houses}</Typography>
        <Typography variant="body1">goldMines: {kingdom.buildings.goldMines}</Typography>
        <Typography variant="body1">ironMines: {kingdom.buildings.ironMines}</Typography>
        <Typography variant="body1">workshops: {kingdom.buildings.workshops}</Typography>
        <Typography variant="body1">farms: {kingdom.buildings.farms}</Typography>
        <Typography variant="body1">markets: {kingdom.buildings.markets}</Typography>
        <Typography variant="body1">barracks: {kingdom.buildings.barracks}</Typography>
        <Typography variant="body1">guardHouses: {kingdom.buildings.guardHouses}</Typography>
        <Typography variant="body1">spyGuilds: {kingdom.buildings.spyGuilds}</Typography>
        <Typography variant="body1">towers: {kingdom.buildings.towers}</Typography>
        <Typography variant="body1">castles: {kingdom.buildings.castles}</Typography>
        <Typography variant="h6">units</Typography>
        <Typography variant="body1">goldMiners: {kingdom.units.goldMiners}</Typography>
        <Typography variant="body1">ironMiners: {kingdom.units.ironMiners}</Typography>
        <Typography variant="body1">farmers: {kingdom.units.farmers}</Typography>
        <Typography variant="body1">blacksmiths: {kingdom.units.blacksmiths}</Typography>
        <Typography variant="body1">builders: {kingdom.units.builders}</Typography>
        <Typography variant="body1">carriers: {kingdom.units.carriers}</Typography>
        <Typography variant="body1">guards: {kingdom.units.guards}</Typography>
        <Typography variant="body1">spies: {kingdom.units.spies}</Typography>
        <Typography variant="body1">infantry: {kingdom.units.infantry}</Typography>
        <Typography variant="body1">bowmen: {kingdom.units.bowmen}</Typography>
        <Typography variant="body1">cavalry: {kingdom.units.cavalry}</Typography>
      </Box>
    </Drawer>
  );
};

export default Sidebar;
