import React from "react";
import { AppBar, Tabs, Tab, Box } from "@mui/material";
import { Route, Routes, Link, Navigate } from "react-router-dom";
import Build from "./tabs/Build";
import Train from "./tabs/Train";
import Overview from "./tabs/Overview";
import Market from "./tabs/Market";

const KingdomTabs: React.FC = () => {
  const [path, setPath] = React.useState("/overview");

  return (
    <Box sx={{ width: "100%" }}>
      <AppBar position="static">
        <Tabs value={path} onChange={(e, newPath) => setPath(newPath)}>
          <Tab label="Overview" value="/overview" to="/overview" component={Link} />
          <Tab label="Build" value="/build" to="/build" component={Link} />
          <Tab label="Train" value="/train" to="/train" component={Link} />
          <Tab label="Market" value="/market/*" to="/market/*" component={Link} />
        </Tabs>
      </AppBar>
      <Routes>
        <Route path="/overview" element={<Overview />} />
        <Route path="/build" element={<Build />} />
        <Route path="/train" element={<Train />} />
        <Route path="/market/*" element={<Market />} />
        <Route path="*" element={<Navigate to="/overview" />} />
      </Routes>
    </Box>
  );
};

export default KingdomTabs;
