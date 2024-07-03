import React from 'react';
import './css/App.css';
import { CssBaseline, Box } from '@mui/material';
import Sidebar from './Sidebar';
import KingdomTabs from './KingdomTabs';
import KingdomToolbar from './KingdomToolbar';
import { KingdomData } from './GameTypes';
import { GAME_API } from './Consts';

export type KingdomContextType = {
    kingdom: KingdomData;
    reloadKingdom: () => void;
}

export const KingdomContext = React.createContext<KingdomContextType | undefined>(undefined);

const kingdomName = "uprzejmy";

const Kingdom: React.FC = () => {
    const [kingdom, setKingdom] = React.useState<KingdomData>();

    const reloadKingdom = () => {
        fetch(`${GAME_API}/kingdom/${kingdomName}`)
            .then(response => response.json())
            .then(kingdom => {
                console.log(`Request successful, data: ${JSON.stringify(kingdom)}`);
                setKingdom(kingdom);
            })
            .catch(error => console.error('Fetching kingdom data for reload has failed:', error))
    };

    React.useEffect(() => {
        reloadKingdom();
    }, []);

    return (
        <Box sx={{ display: 'flex' }}>
            <CssBaseline />

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