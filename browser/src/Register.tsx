import React, { useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { registerRequest } from "./game-api-client/UserApi";

type Result = {
  message: string;
  error: boolean;
};

const Register: React.FC = () => {
  const [email, setEmail] = useState("");
  const [kingdomName, setKingdomName] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState("");
  const [result, setResult] = useState<Result | undefined>(undefined);

  const setError = (message: string) => {
    setResult({ message: message, error: true });
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (email === "" || kingdomName === "" || password === "" || repeatPassword === "") {
      setError("email, kingdomName, password, or repeat password cannot be empty");
      return;
    }

    if (password !== repeatPassword) {
      setError("Passwords do not match");
      return;
    }

    console.log(`Registering new player with email: ${email} and kingdomName: ${kingdomName}`);

    const registerSuccess = await registerRequest(email, kingdomName, password);
    if (registerSuccess) {
      setResult({ message: "Successfully registered account, you can now log in", error: false });
      return;
    }

    setError("Error during registration");
  };

  return (
    <>
      <Typography component="h1" variant="h5">
        Register
      </Typography>
      <Box component={"form"} onSubmit={handleSubmit} sx={{ mt: 1 }}>
        <TextField label="Email" type="email" value={email} onChange={event => setEmail(event.target.value)} required />
        <TextField label="Kingdom Name" type="text" value={kingdomName} onChange={event => setKingdomName(event.target.value)} required />
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
