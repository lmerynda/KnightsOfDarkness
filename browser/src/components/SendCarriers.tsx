import React, { useContext } from "react";
import type { MarketResource } from "../GameTypes";
import { marketResources } from "../GameTypes";
import type { SendCarriersData, SendCarriersResult } from "../game-api-client/TransfersApi";
import { sendCarriers } from "../game-api-client/TransfersApi";
import { KingdomContext } from "../Kingdom";
import { Button, ButtonGroup, Grid, IconButton, Input, InputAdornment, InputLabel, Typography } from "@mui/material";

const SendCarriers: React.FC = () => {
  const defaultResource: MarketResource = "food";
  const [selectedResource, setSelectedResource] = React.useState<MarketResource>(defaultResource);
  const [destinationKingdomName, setDestinationKingdomName] = React.useState<string>("");
  const [amount, setAmount] = React.useState<number>(0);
  const [lastSendCarriersResult, setLastSendCarriersResult] = React.useState<SendCarriersResult | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);

  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSendCarriers = async (): Promise<void> => {
    const data: SendCarriersData = {
      resource: selectedResource,
      destinationKingdomName: destinationKingdomName,
      amount: amount,
    };

    const result = await sendCarriers(data);
    setLastSendCarriersResult(result);

    setSelectedResource(defaultResource);
    setDestinationKingdomName("");
    setAmount(0);

    kingdomContext.reloadKingdom();
  };

  function handleMaxClick(): void {
    setAmount(kingdomContext?.kingdom.resources[selectedResource] ?? 0);
  }

  return (
    <div>
      <h2>Send Carriers</h2>
      {lastSendCarriersResult && (
        <Typography variant="body1" component="p" color={lastSendCarriersResult.success ? "success" : "error"}>
          {lastSendCarriersResult.message}
        </Typography>
      )}
      <Grid container spacing={2} alignItems="center">
        <Grid item>
          <ButtonGroup variant="contained">
            {marketResources.map(resource => (
              <Button
                key={resource}
                variant={resource === selectedResource ? "contained" : "outlined"}
                onClick={() => {
                  setSelectedResource(resource);
                  setAmount(0);
                }}
              >
                {resource}
              </Button>
            ))}
          </ButtonGroup>
        </Grid>
        <Grid item>
          <InputLabel>Destination Kingdom</InputLabel>
          <Input type="text" value={destinationKingdomName} onChange={event => setDestinationKingdomName(event.target.value)} />
        </Grid>
        <Grid item>
          <InputLabel>Amount</InputLabel>
          <Input
            type="number"
            inputProps={{ min: 0 }}
            value={amount}
            onChange={event => setAmount(parseInt(event.target.value))}
            endAdornment={
              <InputAdornment position="end">
                <IconButton onClick={handleMaxClick}>
                  <Typography variant="body2" component="span" style={{ fontWeight: "bold" }}>
                    max
                  </Typography>
                </IconButton>
              </InputAdornment>
            }
          />
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={() => handleSendCarriers()}>
            Send Carriers
          </Button>
        </Grid>
      </Grid>
    </div>
  );
};

export default SendCarriers;
