import React, { useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { GAME_API } from "./Consts";
import { useNavigate } from "react-router-dom";

interface LoginProps {
  setAuthenticated: (isAuthenticated: boolean) => void;
}

const Login: React.FC<LoginProps> = ({ setAuthenticated }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (email === "" || password === "") {
      console.log("Email or password is empty");
      return;
    }
    console.log(`Logging in with email: ${email} and password: ${password}`);
    const authRequest = {
      email: email,
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
    <>
      <Typography component="h1" variant="h5">
        Login
      </Typography>
      <Box component={"form"} onSubmit={handleSubmit} sx={{ mt: 1 }}>
        <TextField label="Email" type="text" value={email} onChange={event => setEmail(event.target.value)} required />
        <TextField label="Password" type="password" value={password} onChange={event => setPassword(event.target.value)} required />
        <Box sx={{ mt: 2 }}>
          <Button type="submit" variant="contained" color="primary">
            Login
          </Button>
        </Box>
      </Box>
    </>
  );
};

export default Login;
