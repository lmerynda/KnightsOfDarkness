import React from "react";
import { Box, Tabs, Tab } from "@mui/material";
import { Link, Navigate, Route, Routes } from "react-router-dom";
import MarketBuyResource from "../components/MarketBuyResource";
import { marketResources } from "../GameTypes";

const MarketBuy: React.FC = () => {
  const [path, setPath] = React.useState("/market/buy/food");

  return (
    <div>
      <h1>Market Buy</h1>
      <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
        <Tabs value={path} onChange={(e, newPath) => setPath(newPath)}>
          {marketResources.map(resource => (
            <Tab key={resource} label={resource} component={Link} value={`/market/buy/${resource}`} to={`/market/buy/${resource}`} />
          ))}
        </Tabs>
        <Routes>
          {marketResources.map(resource => (
            <Route key={resource} path={resource} element={<MarketBuyResource resource={resource} />} />
          ))}
          <Route path="*" element={<Navigate to="/market/buy/food" />} />
        </Routes>
      </Box>
    </div>
  );
};

export default MarketBuy;
