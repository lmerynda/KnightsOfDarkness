import { Button, ButtonGroup, Grid, Input, InputLabel, Typography } from "@mui/material";
import React, { useContext } from "react";
import type { SendAttackData, SendAttackResult } from "../game-api-client/AttacksApi";
import { sendAttack } from "../game-api-client/AttacksApi";
import { attackTypes, type AttackType } from "../GameTypes";
import { KingdomContext } from "../Kingdom";

const SendAttack: React.FC = () => {
  const defaultAttackType: AttackType = "economy";
  const [selectedAttackType, setSelectedAttackType] = React.useState<AttackType>(defaultAttackType);
  const [destinationKingdomName, setDestinationKingdomName] = React.useState<string>("");
  const [infantry, setInfantry] = React.useState<number>(0);
  const [bowmen, setBowmen] = React.useState<number>(0);
  const [cavalry, setCavalry] = React.useState<number>(0);
  const [lastSendAttackResult, setLastSendAttackResult] = React.useState<SendAttackResult | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);

  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSendAttack = async (): Promise<void> => {
    const data: SendAttackData = {
      attackType: selectedAttackType,
      destinationKingdomName: destinationKingdomName,
      units: { infantry, bowman: bowmen, cavalry },
    };

    const result = await sendAttack(data);
    setLastSendAttackResult(result);

    setSelectedAttackType(defaultAttackType);
    setDestinationKingdomName("");
    setInfantry(0);
    setBowmen(0);
    setCavalry(0);

    kingdomContext.reloadKingdom();
  };

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
          <ButtonGroup variant="contained">
            {attackTypes.map(attackType => (
              <Button
                key={attackType}
                variant={attackType === selectedAttackType ? "contained" : "outlined"}
                onClick={() => {
                  setSelectedAttackType(attackType);
                }}
              >
                {attackType}
              </Button>
            ))}
          </ButtonGroup>
        </Grid>
        <Grid item>
          <InputLabel>Infantry</InputLabel>
          <Input type="number" value={infantry} onChange={event => setInfantry(parseInt(event.target.value))} />
        </Grid>
        <Grid item>
          <InputLabel>Bowmen</InputLabel>
          <Input type="number" value={bowmen} onChange={event => setBowmen(parseInt(event.target.value))} />
        </Grid>
        <Grid item>
          <InputLabel>Cavalry</InputLabel>
          <Input type="number" value={cavalry} onChange={event => setCavalry(parseInt(event.target.value))} />
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
