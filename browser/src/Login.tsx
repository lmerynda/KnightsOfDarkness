import React, { useState } from 'react';
import { Box, Button, Container, TextField, Typography } from '@mui/material';

interface LoginProps {
    setUser: (user: string | undefined) => void;
}

const Login: React.FC<LoginProps> = ({ setUser }) => {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        setUser(username);
    };

    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography component="h1" variant="h5">
                    Login
                </Typography>
                <Box component="form" sx={{ mt: 1 }}>
                    <form style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <TextField
                            label="User"
                            type="text"
                            value={username}
                            onChange={(event) => setUsername(event.target.value)}
                            required
                        />
                        <TextField
                            label="Password"
                            type="password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            required
                        />
                        <Button type="submit" variant="contained" color="primary" onClick={handleSubmit}>
                            Login
                        </Button>
                    </form>
                </Box>
            </Box>
        </Container>
    );
};

export default Login;