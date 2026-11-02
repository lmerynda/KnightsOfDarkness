import React, { useEffect, useState } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { getToken, keepSessionFresh } from "./api/client";
import { GameProvider } from "./game/GameState";
import { Landing } from "./pages/Landing";
import { Register, SignIn } from "./pages/Auth";
import { Overview } from "./pages/Overview";
import { Build } from "./pages/Build";
import { Train } from "./pages/Train";
import { War } from "./pages/War";
import { Spies } from "./pages/Spies";
import { Market } from "./pages/Market";
import { Carriers } from "./pages/Carriers";
import { AlliancePage } from "./pages/Alliance";
import { Letters } from "./pages/Letters";
import { Chronicle } from "./pages/Chronicle";
import { RankingsPage } from "./pages/Rankings";
import { Keep } from "./pages/Keep";
import { GameShell } from "./shell/GameShell";

const useSignedIn = (): boolean => {
  const [signedIn, setSignedIn] = useState(() => Boolean(getToken()));
  useEffect(() => {
    const update = (): void => setSignedIn(Boolean(getToken()));
    window.addEventListener("auth-changed", update);
    window.addEventListener("storage", update);
    return () => {
      window.removeEventListener("auth-changed", update);
      window.removeEventListener("storage", update);
    };
  }, []);
  useEffect(() => (signedIn ? keepSessionFresh() : undefined), [signedIn]);
  return signedIn;
};

const Loading: React.FC<{ error: string | null }> = ({ error }) => (
  <div className="loading-screen">
    <p className="loading-title">Knights of Darkness</p>
    <p className={error ? "warning" : "muted"}>{error ?? "Opening the gates of your kingdom"}</p>
  </div>
);

const App: React.FC = () => {
  const signedIn = useSignedIn();

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={signedIn ? <Navigate to="/kingdom/overview" replace /> : <Landing />} />
        <Route path="/sign-in" element={signedIn ? <Navigate to="/kingdom/overview" replace /> : <SignIn />} />
        <Route path="/register" element={signedIn ? <Navigate to="/kingdom/overview" replace /> : <Register />} />
        <Route
          path="/kingdom"
          element={
            signedIn ? (
              <GameProvider fallback={error => <Loading error={error} />}>
                <GameShell />
              </GameProvider>
            ) : (
              <Navigate to="/sign-in" replace />
            )
          }
        >
          <Route index element={<Navigate to="overview" replace />} />
          <Route path="overview" element={<Overview />} />
          <Route path="build" element={<Build />} />
          <Route path="train" element={<Train />} />
          <Route path="war" element={<War />} />
          <Route path="spies" element={<Spies />} />
          <Route path="market" element={<Market />} />
          <Route path="carriers" element={<Carriers />} />
          <Route path="alliance" element={<AlliancePage />} />
          <Route path="letters" element={<Letters />} />
          <Route path="chronicle" element={<Chronicle />} />
          <Route path="rankings" element={<RankingsPage />} />
          <Route path="keep" element={<Keep />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
