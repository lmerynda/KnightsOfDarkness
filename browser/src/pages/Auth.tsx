import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api } from "../api/endpoints";
import { errorText } from "../api/client";
import { art } from "../game/lore";

const AuthFrame: React.FC<{ title: string; children: React.ReactNode; footer: React.ReactNode }> = ({ title, children, footer }) => (
  <div className="auth" style={{ backgroundImage: `url(${art("login")})` }}>
    <div className="auth-card">
      <Link to="/" className="auth-home">
        Knights of Darkness
      </Link>
      <h1>{title}</h1>
      {children}
      <p className="muted auth-footer">{footer}</p>
    </div>
  </div>
);

export const SignIn: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.signIn(email, password);
      navigate("/kingdom/overview");
    } catch (failure) {
      setError(errorText(failure));
    }
    setBusy(false);
  };

  return (
    <AuthFrame
      title="Return to your kingdom"
      footer={
        <>
          No kingdom yet? <Link to="/register">Found one</Link>
        </>
      }
    >
      <form className="stack" onSubmit={submit}>
        <label className="field">
          <span>Email</span>
          <input
            className="input"
            type="text"
            autoComplete="username"
            value={email}
            onChange={event => setEmail(event.target.value)}
            required
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            className="input"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={event => setPassword(event.target.value)}
            required
          />
        </label>
        {error ? <p className="warning">{error}</p> : null}
        <button className="button" type="submit" disabled={busy}>
          Enter
        </button>
      </form>
    </AuthFrame>
  );
};

export const Register: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [kingdomName, setKingdomName] = useState("");
  const [password, setPassword] = useState("");
  const [repeat, setRepeat] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault();
    if (password !== repeat) {
      setError("The two passwords are different");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await api.register(email, kingdomName, password);
      await api.signIn(email, password);
      navigate("/kingdom/overview");
    } catch (failure) {
      setError(errorText(failure));
    }
    setBusy(false);
  };

  return (
    <AuthFrame
      title="Found a kingdom"
      footer={
        <>
          Already rule one? <Link to="/sign-in">Return to it</Link>
        </>
      }
    >
      <form className="stack" onSubmit={submit}>
        <label className="field">
          <span>Kingdom name</span>
          <input
            className="input"
            value={kingdomName}
            onChange={event => setKingdomName(event.target.value)}
            minLength={3}
            maxLength={24}
            required
          />
        </label>
        <label className="field">
          <span>Email</span>
          <input
            className="input"
            type="email"
            autoComplete="email"
            value={email}
            onChange={event => setEmail(event.target.value)}
            required
          />
        </label>
        <label className="field">
          <span>Password, at least 8 characters</span>
          <input
            className="input"
            type="password"
            autoComplete="new-password"
            value={password}
            onChange={event => setPassword(event.target.value)}
            minLength={8}
            required
          />
        </label>
        <label className="field">
          <span>Password again</span>
          <input
            className="input"
            type="password"
            autoComplete="new-password"
            value={repeat}
            onChange={event => setRepeat(event.target.value)}
            required
          />
        </label>
        {error ? <p className="warning">{error}</p> : null}
        <button className="button" type="submit" disabled={busy}>
          Found the kingdom
        </button>
      </form>
    </AuthFrame>
  );
};
