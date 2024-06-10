// src/TabsComponent.tsx
import React from 'react';
import { AppBar, Tabs, Tab, Box } from '@mui/material';
import { Route, Routes, Link, useLocation } from 'react-router-dom';
import Build from './tabs/Build';
import Overview from './tabs/Overview';
import Market from './tabs/Market';
import { KingdomReloader } from './App';


function useRouteMatch(patterns: string[]): string | null {
    const { pathname } = useLocation();
    for (let i = 0; i < patterns.length; i += 1) {
        const pattern = patterns[i];
        if (pathname === pattern) {
            return pattern;
        }
    }
    return null;
}

const KingdomTabs: React.FC<KingdomReloader> = ({ reloadKingdom }) => {
    const routeMatch = useRouteMatch(['/overview', '/build', '/market']);
    const currentTab = routeMatch ? routeMatch : false;

    return (
        <Box sx={{ width: '100%' }}>
            <AppBar position="static">
                <Tabs value={currentTab}>
                    <Tab label="Overview" value="/overview" to="/overview" component={Link} />
                    <Tab label="Build" value="/build" to="/build" component={Link} />
                    <Tab label="Market" value="/market" to="/market" component={Link} />
                </Tabs>
            </AppBar>
            <Routes>
                <Route path="/Overview" element={<Overview />} />
                <Route path="/Build" element={<Build reloadKingdom={reloadKingdom} />} />
                <Route path="/Market" element={<Market />} />
                <Route path="/" element={<Overview />} />
            </Routes>
        </Box>
    );
};

export default KingdomTabs;
