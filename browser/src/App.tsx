import React from "react";
import "./css/App.css";
import { CssBaseline, Box, createTheme, ThemeProvider } from "@mui/material";
import Login from "./Login";
import Kingdom from "./Kingdom";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import { GAME_API } from "./Consts";

const darkTheme = createTheme({
  palette: {
    mode: "dark",
  },
});

const App: React.FC = () => {
  const [authenticated, setAuthenticated] = React.useState<boolean | undefined>(undefined);

  const validateToken = async () => {
    const token = localStorage.getItem("authToken");
    if (token) {
      try {
        console.log("Validating token");
        const response = await fetch(`${GAME_API}/auth/validate-token`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        if (response.ok) {
          console.log("Token is valid");
          setAuthenticated(true);
          return;
        } else {
          console.log("Token is not valid");
        }
      } catch (error) {
        console.error("Error validating token: ", error);
      }
    }
    setAuthenticated(false);
  };

  React.useEffect(() => {
    validateToken();
  }, []);

  if (authenticated === undefined) {
    return <div>Loading...</div>;
  }

  return (
    <ThemeProvider theme={darkTheme}>
      <Box sx={{ display: "flex" }}>
        <CssBaseline />
        <Router>
          <Routes>
            <Route path="/login" element={<Login setAuthenticated={setAuthenticated} />} />
            <Route path="/*" element={authenticated ? <Kingdom /> : <Navigate to="/login" />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Router>
      </Box>
    </ThemeProvider>
  );
};

export default App;
