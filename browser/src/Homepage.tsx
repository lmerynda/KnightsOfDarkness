import { Container, Grid, Box, Typography } from "@mui/material";
import React from "react";
import Login from "./Login";
import Register from "./Register";
import { fetchGameHealthStatusRequest } from "./game-api-client/GameApi";

interface LoginProps {
  setAuthenticated: (isAuthenticated: boolean) => void;
}

const Homepage: React.FC<LoginProps> = ({ setAuthenticated }) => {
      const [isGameRunning, setIsGameRunning] = React.useState<boolean>(false);
    
      const reloadIsGameRunning = async (): Promise<void> => {
        const data = await fetchGameHealthStatusRequest();
        setIsGameRunning(data);
      };
    
      React.useEffect(() => {
        reloadIsGameRunning();
    
        const interval = setInterval(() => {
            reloadIsGameRunning();
        }, 5000);
    
        return () => {
          clearInterval(interval);
        };
      }, []);

  return (
    
    <Container component="main" maxWidth="md">
      <Box my={4}>
        <Typography variant="h4" component="h1" gutterBottom>
          Knights of Darkness
        </Typography>
        <Typography variant="h4" component="h1" gutterBottom sx={{
            color: isGameRunning ? 'green' : 'red',
            fontWeight: 'bold',
        }}>
            {isGameRunning ? "Game is online" : "Game is offline"}
        </Typography>
      </Box>
      <Grid container spacing={4}>
        <Grid item xs={12} md={6}>
          <Box
            p={2}
            border={1}
            borderColor="grey.300"
            borderRadius={4}
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            height="100%"
          >
            <Login setAuthenticated={setAuthenticated} />
          </Box>
        </Grid>
        <Grid item xs={12} md={6}>
          <Box
            p={2}
            border={1}
            borderColor="grey.300"
            borderRadius={4}
            display="flex"
            flexDirection="column"
            justifyContent="center"
            alignItems="center"
            height="100%"
          >
            <Register />
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Homepage;
