import React from 'react';
import './css/App.css';
import { KingdomData } from './components/Kingdom';
import { BrowserRouter as Router } from 'react-router-dom';
import { CssBaseline, Box, createTheme, ThemeProvider } from '@mui/material';
import Sidebar from './Sidebar';
import KingdomTabs from './KingdomTabs';
import KingdomToolbar from './KingdomToolbar';

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
  },
});


const kingdomName = "uprzejmy";

const App: React.FC = () => {
  const [kingdom, setKingdom] = React.useState<KingdomData>();

  React.useEffect(() => {
    fetch(`http://localhost:8080/kingdom/${kingdomName}`)
      .then(response => response.json())
      .then(kingdom => {
        setKingdom(kingdom);
      });
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
                <KingdomTabs />
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