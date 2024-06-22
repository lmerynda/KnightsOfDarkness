import React from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import { Link, Navigate, Route, Routes } from 'react-router-dom';
import MarketBuy from './MarketBuy';
import MarketSell from './MarketSell';

const Market: React.FC = () => {
    const [path, setPath] = React.useState('/market/buy');

    return (
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={path} onChange={(e, newPath) => setPath(newPath)}>
                <Tab label="Buy" component={Link} value="/market/buy" to="/market/buy" />
                <Tab label="Sell" component={Link} value="/market/sell" to="/market/sell" />
            </Tabs>
            <Routes>
                <Route path="buy" element={<MarketBuy />} />
                <Route path="sell" element={<MarketSell />} />
                <Route path="*" element={<Navigate to="/market/buy" />} />
            </Routes>
        </Box>
    );
};

export default Market;
