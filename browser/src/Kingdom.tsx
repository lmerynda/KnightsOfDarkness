import React from 'react';
import './css/App.css';
import { Box } from '@mui/material';
import Sidebar from './Sidebar';
import KingdomTabs from './KingdomTabs';
import KingdomToolbar from './KingdomToolbar';
import { KingdomData } from './GameTypes';
import { fetchKingdomDataRequest } from './game-api-client/KingdomApi';
import { kingdomRefreshInterval } from './Consts';

export type KingdomContextType = {
    kingdom: KingdomData;
    reloadKingdom: () => void;
}

export const KingdomContext = React.createContext<KingdomContextType | undefined>(undefined);

const Kingdom: React.FC = () => {
    const [kingdom, setKingdom] = React.useState<KingdomData>();

    const reloadKingdom = async () => {
        const data = await fetchKingdomDataRequest();
        setKingdom(data);
    };

    React.useEffect(() => {
        reloadKingdom();

        const interval = setInterval(() => {
            reloadKingdom();
        }, kingdomRefreshInterval);

        return () => {
            clearInterval(interval);
        };
    }, []);

    return (
        <Box sx={{ display: 'flex' }}>
            {kingdom ? (
                <KingdomContext.Provider value={{ kingdom, reloadKingdom }}>
                    <Sidebar {...kingdom} />
                    <Box component="main" sx={{ flexGrow: 1, bgcolor: 'background.default', p: 3 }}>
                        <KingdomToolbar kingdomName={kingdom.name} kingdomResources={kingdom.resources} />
                        <KingdomTabs />
                    </Box>
                </KingdomContext.Provider>
            ) : (
                <div>Loading...</div>
            )};
        </Box>
    );
}

export default Kingdom;
