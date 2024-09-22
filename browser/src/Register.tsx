import React, { useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { GAME_API } from "./Consts";

type Result = {
  message: string;
  error: boolean;
};

const Register: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState("");
  const [result, setResult] = useState<Result | undefined>(undefined);

  const setError = (message: string) => {
    setResult({ message: message, error: true });
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (username === "" || password === "" || repeatPassword === "") {
      setError("Username, password, or repeat password cannot be empty");
      return;
    }

    if (password !== repeatPassword) {
      setError("Passwords do not match");
      return;
    }

    console.log(`Registering new player with username: ${username} and kingdomName: ${username}`);

    const authRequest = {
      username: username,
      password: password,
    };

    try {
      const response = await fetch(`${GAME_API}/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(authRequest),
      });
      if (response.ok) {
        console.log(`Successfully registered new player with username: ${username}`);
        setResult({ message: "Successfully registered account, you can now log in", error: false });
      } else if (response.status === 401) {
        setError("Unauthorized");
      } else if (response.status === 500) {
        setError("Internal Server Error");
      } else {
        setError("Unknown error");
      }
    } catch (error) {
      console.error("Error during player registration: ", error);
      setError("Error during registration");
    }
  };

  return (
    <>
      <Typography component="h1" variant="h5">
        Register
      </Typography>
      <Box component={"form"} onSubmit={handleSubmit} sx={{ mt: 1 }}>
        <TextField label="Kingdom Name" type="text" value={username} onChange={event => setUsername(event.target.value)} required />
        <TextField label="Password" type="password" value={password} onChange={event => setPassword(event.target.value)} required />
        <TextField
          label="Repeat-Password"
          type="password"
          value={repeatPassword}
          onChange={event => setRepeatPassword(event.target.value)}
          required
        />
        {result && (
          <Typography color={result.error ? "error" : "success"} variant="body2" sx={{ mt: 2 }}>
            {result.message}
          </Typography>
        )}
        <Box sx={{ mt: 2 }}>
          <Button type="submit" variant="contained" color="primary">
            Register
          </Button>
        </Box>
      </Box>
    </>
  );
};

export default Register;
