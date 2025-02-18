import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import type { CreateAllianceResult } from "../game-api-client/AllianceApi";
import { createAllianceRequest } from "../game-api-client/AllianceApi";
import { Button, Input, Typography } from "@mui/material";

const CreateAlliance: React.FC = () => {
  const [name, setName] = React.useState<string>("");
  const [lastCreateResult, setLastCreateResult] = React.useState<CreateAllianceResult | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleCreateAlliance = async (): Promise<void> => {
    const result = await createAllianceRequest({ name });
    if (result.success) {
      kingdomContext.reloadKingdom();
      return;
    }
    setLastCreateResult(result);
  };

  return (
    <div>
      {lastCreateResult && (
        <Typography variant="body1" component="p" color={lastCreateResult.success ? "success" : "error"}>
          {lastCreateResult.message}
        </Typography>
      )}
      <Input type="text" value={name} onChange={event => setName(event.target.value)} />
      <Button variant="contained" onClick={handleCreateAlliance}>
        Create Alliance
      </Button>
    </div>
  );
};

export default CreateAlliance;
