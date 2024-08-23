import React, { useState } from "react";
import { Box, Button, Container, TextField, Typography } from "@mui/material";
import { GAME_API } from "./Consts";
import { useNavigate } from "react-router-dom";

interface LoginProps {
  setAuthenticated: (isAuthenticated: boolean) => void;
}

const Login: React.FC<LoginProps> = ({ setAuthenticated }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (username === "" || password === "") {
      console.log("Username or password is empty");
      return;
    }
    console.log(`Logging in with username: ${username} and password: ${password}`);
    const authRequest = {
      username: username,
      password: password,
    };

    try {
      const response = await fetch(`${GAME_API}/auth/authenticate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(authRequest),
      });
      if (response.ok) {
        const data = await response.json();
        console.log(`Response ok, data: ${JSON.stringify(data)}`);
        localStorage.setItem("authToken", data.token);
        setAuthenticated(true);
        navigate("/");
      } else if (response.status === 401) {
        console.log("Unauthorized");
      } else if (response.status === 500) {
        console.log("Internal Server Error");
      } else {
        console.log("Unknown error");
      }
    } catch (error) {
      console.error("Error during authentication: ", error);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <Typography component="h1" variant="h5">
          Login
        </Typography>
        <Box component={"form"} onSubmit={handleSubmit} sx={{ mt: 1 }}>
          <TextField label="User" type="text" value={username} onChange={event => setUsername(event.target.value)} required />
          <TextField label="Password" type="password" value={password} onChange={event => setPassword(event.target.value)} required />
          <Button type="submit" variant="contained" color="primary">
            Login
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default Login;
