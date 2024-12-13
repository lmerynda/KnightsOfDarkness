import { Button, Grid, Input, InputLabel, Typography } from "@mui/material";
import React, { useContext } from "react";
import { sendAttack, SendAttackData, SendAttackResult } from "../game-api-client/AttacksApi";
import { AttackType } from "../GameTypes";
import { KingdomContext } from "../Kingdom";

const SendAttack: React.FC = () => {
  const defaultAttackType: AttackType = "economy";
  const [selectedAttackType, setSelectedAttackType] = React.useState<AttackType>(defaultAttackType);
  const [destinationKingdomName, setDestinationKingdomName] = React.useState<string>("");
  //   const [amount, setAmount] = React.useState<number>(0);
  const [lastSendAttackResult, setLastSendAttackResult] = React.useState<SendAttackResult | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);

  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSendAttack = async () => {
    const data: SendAttackData = {
      attackType: selectedAttackType,
      destinationKingdomName: destinationKingdomName,
      units: { infantry: 0, bowman: 0, cavalry: 0 },
    };

    const result = await sendAttack(data);
    setLastSendAttackResult(result);

    setSelectedAttackType(defaultAttackType);
    setDestinationKingdomName("");

    kingdomContext.reloadKingdom();
  };

  //   function handleMaxClick(): void {
  //     setAmount(kingdomContext?.kingdom.resources[selectedResource] ?? 0);
  //   }

  return (
    <div>
      <h2>Send Attack</h2>
      {lastSendAttackResult && (
        <Typography variant="body1" component="p" color={lastSendAttackResult.success ? "success" : "error"}>
          {lastSendAttackResult.message}
        </Typography>
      )}
      <Grid container spacing={2} alignItems="center">
        <Grid item>
          <InputLabel>Destination Kingdom</InputLabel>
          <Input type="text" value={destinationKingdomName} onChange={event => setDestinationKingdomName(event.target.value)} />
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={() => handleSendAttack()}>
            Send Attack
          </Button>
        </Grid>
      </Grid>
    </div>
  );
};

export default SendAttack;
