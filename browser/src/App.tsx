import React from "react";
import "./css/App.css";
import { CssBaseline, Box, createTheme, ThemeProvider } from "@mui/material";
import Kingdom from "./Kingdom";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import Homepage from "./Homepage";
import { validateTokenRequest } from "./game-api-client/UserApi";

const darkTheme = createTheme({
  palette: {
    mode: "dark",
  },
});

const App: React.FC = () => {
  const [authenticated, setAuthenticated] = React.useState<boolean | undefined>(undefined);

  const validateToken = React.useCallback(async () => {
    console.log("App.tsx validating token");
    const token = localStorage.getItem("authToken");
    setAuthenticated(token ? await validateTokenRequest(token) : false);
  }, [setAuthenticated]);

  React.useEffect(() => {
    validateToken();
  }, [validateToken]);

  const sleep = (ms: number): Promise<void> => new Promise(resolve => setTimeout(resolve, ms));

  React.useEffect(() => {
    const sleepAndValidate = async (): Promise<void> => {
      await sleep(2000);
      await validateToken();
    };

    sleepAndValidate();
  }, [validateToken]);

  if (authenticated === undefined) {
    return <div>Authenticating...</div>;
  }

  return (
    <ThemeProvider theme={darkTheme}>
      <Box sx={{ display: "flex" }}>
        <CssBaseline />
        <Router>
          <Routes>
            <Route path="/homepage" element={<Homepage setAuthenticated={setAuthenticated} />} />
            <Route path="/*" element={authenticated ? <Kingdom /> : <Navigate to="/homepage" />} />
            <Route path="*" element={<Navigate to="/homepage" />} />
          </Routes>
        </Router>
      </Box>
    </ThemeProvider>
  );
};

export default App;
