import React from 'react';
import './css/App.css';
import { BrowserRouter as Router } from 'react-router-dom';
import { CssBaseline, Box, createTheme, ThemeProvider } from '@mui/material';
import Sidebar from './Sidebar';
import KingdomTabs from './KingdomTabs';
import KingdomToolbar from './KingdomToolbar';
import { KingdomData } from './GameTypes';

export interface KingdomReloader {
  reloadKingdom: () => void;
}

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});

const kingdomName = "uprzejmy";

const App: React.FC = () => {
  const [kingdom, setKingdom] = React.useState<KingdomData>();

  const reloadKingdom = () => {
    fetch(`http://localhost:8080/kingdom/${kingdomName}`)
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
    <ThemeProvider theme={darkTheme}>
      <Router>
        <Box sx={{ display: 'flex' }}>
          <CssBaseline />
          {kingdom ? (
            <>
              <Sidebar {...kingdom} />
              <Box
                component="main"
                sx={{ flexGrow: 1, bgcolor: 'background.default', p: 3 }}
              >
                <KingdomToolbar kingdomName={kingdom.name} kingdomResources={kingdom.resources} />
                <KingdomTabs reloadKingdom={reloadKingdom} />
              </Box>
            </>
          ) : <div>Loading...</div>
          }
        </Box>
      </Router>
    </ThemeProvider>
  );
}

export default App;