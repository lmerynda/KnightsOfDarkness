import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import { createAllianceRequest } from "../game-api-client/AllianceApi";
import { Button, Input } from "@mui/material";

const CreateAlliance: React.FC = () => {
  const [name, setName] = React.useState<string>("");
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleCreateAlliance = async (): Promise<void> => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const data = await createAllianceRequest({ name });
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <Input type="text" value={name} onChange={event => setName(event.target.value)} />
      <Button variant="contained" onClick={handleCreateAlliance}>
        Create Alliance
      </Button>
    </div>
  );
};

export default CreateAlliance;
