import React from 'react';
import './css/App.css';
import { Box } from '@mui/material';
import Sidebar from './Sidebar';
import KingdomTabs from './KingdomTabs';
import KingdomToolbar from './KingdomToolbar';
import { KingdomData } from './GameTypes';
import { fetchKingdomData } from './game-api-client/KingdomApi';

export type KingdomContextType = {
    kingdom: KingdomData;
    reloadKingdom: () => void;
}

export const KingdomContext = React.createContext<KingdomContextType | undefined>(undefined);

const Kingdom: React.FC = () => {
    const [kingdom, setKingdom] = React.useState<KingdomData>();

    const reloadKingdom = async () => {
        const data = await fetchKingdomData();
        setKingdom(data);
    };

    React.useEffect(() => {
        reloadKingdom();
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
