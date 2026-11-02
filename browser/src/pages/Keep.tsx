import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/endpoints";
import { useGame } from "../game/useGame";
import { art } from "../game/lore";
import { Banner, Panel } from "../ui/parts";

/** a ruler who started with only a name keeps the kingdom with an address and password of their own */
export const Keep: React.FC = () => {
  const { act } = useGame();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const keep = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault();
    const kept = await act(
      () => api.keepKingdom(email, password),
      outcome => ({ text: outcome.message, failure: !outcome.success }),
    );
    if (kept?.success) navigate("/kingdom/overview");
  };

  return (
    <>
      <Banner image={art("castle")} title="Keep this kingdom" />
      <Panel
        title="An account of your own"
        lede="Until then the kingdom lives only in this browser."
        help="Sign in with this address and password from any device. Clearing this browser's data or signing out before you keep it loses the kingdom."
      >
        <form className="stack" style={{ maxWidth: 420 }} onSubmit={keep}>
          <label className="field">
            <span>Email</span>
            <input
              className="input"
              type="email"
              autoComplete="username"
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
              required
            />
          </label>
          <button className="button" type="submit">
            Keep it
          </button>
        </form>
      </Panel>
    </>
  );
};
