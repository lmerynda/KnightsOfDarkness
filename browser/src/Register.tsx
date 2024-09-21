import React, { useState } from "react";
import { Box, Button, TextField, Typography } from "@mui/material";
import { GAME_API } from "./Consts";
import { useNavigate } from "react-router-dom";

const Register: React.FC = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [repeatPassword, setRepeatPassword] = useState(""); // Add state for repeat password
  const navigate = useNavigate();

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (username === "" || password === "" || repeatPassword === "") {
      console.log("Username, password, or repeat password is empty");
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

        navigate("/");
      } else if (response.status === 401) {
        console.log("Unauthorized");
      } else if (response.status === 500) {
        console.log("Internal Server Error");
      } else {
        console.log("Unknown error");
      }
    } catch (error) {
      console.error("Error during player registration: ", error);
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
        />{" "}
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
