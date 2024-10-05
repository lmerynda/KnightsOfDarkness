import React, { useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { authenticateRequest } from "./game-api-client/UserApi";

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
    console.log(`Logging in with email: ${email}`);

    const token = await authenticateRequest(email, password);
    if (token) {
      localStorage.setItem("authToken", token);
      setAuthenticated(true);
      navigate("/");
      return;
    }

    // TODO take login fail info back to user
    console.log("Login error");
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
