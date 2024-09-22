import React from "react";
import KingdomResourcesView from "./components/KingdomResources";
import { KingdomDetails, KingdomResources } from "./GameTypes";
import { Button } from "@mui/material";
import { useNavigate } from "react-router-dom";

export type KingdomToolbarProps = {
  kingdomName: string;
  kingdomResources: KingdomResources;
  kingdomDetails: KingdomDetails;
};
const KingdomToolbar: React.FC<KingdomToolbarProps> = ({ kingdomName, kingdomResources, kingdomDetails }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("authToken");
    navigate("/homepage");
  };

  return (
    <div>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <h2>Kingdom {kingdomName}</h2>
        <div>
          <Button variant="contained" onClick={() => handleLogout()}>
            Logout
          </Button>
        </div>
      </div>
      <KingdomResourcesView kingdomResources={kingdomResources} kingdomDetails={kingdomDetails} />
    </div>
  );
};

export default KingdomToolbar;
