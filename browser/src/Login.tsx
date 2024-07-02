import React, { useState } from 'react';
import { Button, TextField } from '@mui/material';

interface LoginProps {
    setUser: (user: string | undefined) => void;
}

const Login: React.FC<LoginProps> = ({ setUser }) => {

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (event: React.FormEvent) => {
        setUser(username);
    };

    return (
        <form onSubmit={handleSubmit}>
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
            <Button type="submit" variant="contained" color="primary">
                Login
            </Button>
        </form>
    );
};

export default Login;