import React from 'react';
import './css/App.css';
import { CssBaseline, Box, createTheme, ThemeProvider } from '@mui/material';
import Login from './Login';
import Kingdom from './Kingdom';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';

const darkTheme = createTheme({
    palette: {
        mode: 'dark',
    },
});

const App: React.FC = () => {
    const [authenticated, setAuthenticated] = React.useState<boolean>(false);

    return (
        <ThemeProvider theme={darkTheme}>
            <Box sx={{ display: 'flex' }}>
                <CssBaseline />
                <Router>
                    <Routes>
                        <Route path="/login" element={<Login setAuthenticated={setAuthenticated} />} />
                        <Route
                            path="/*"
                            element={
                                authenticated ? <Kingdom /> : <Navigate to="/login" />
                            }
                        />
                        <Route path="*" element={<Navigate to="/" />} />
                    </Routes>
                </Router>
            </Box>
        </ThemeProvider>
    );
}

export default App;