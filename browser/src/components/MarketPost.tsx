import { Button, ButtonGroup, Grid, IconButton, Input, InputAdornment, InputLabel, Typography } from "@mui/material";
import React, { useContext } from "react";
import type { MarketOfferCreateResult, MarketResource } from "../GameTypes";
import { marketResources } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import type { CreateMarketOfferData } from "../game-api-client/MarketApi";
import { createMarketOfferRequest } from "../game-api-client/MarketApi";

const MarketPost: React.FC = () => {
  const [sellAmount, setSellAmount] = React.useState<number>(0);
  const [price, setPrice] = React.useState<number>(0);
  const defaultResource: MarketResource = "food";
  const [selectedResource, setSelectedResource] = React.useState<MarketResource>(defaultResource);
  const [lastCreateOfferResult, setLastCreateOfferResult] = React.useState<MarketOfferCreateResult | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);

  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleCreateOffer = async (): Promise<void> => {
    const offer: CreateMarketOfferData = {
      resource: selectedResource,
      price: price,
      count: sellAmount,
    };

    const result = await createMarketOfferRequest(offer);
    setLastCreateOfferResult(result);

    setSellAmount(0);
    setPrice(0);
    setSelectedResource(defaultResource);
    kingdomContext.reloadKingdom();
  };

  function handleMaxClick(): void {
    setSellAmount(kingdomContext?.kingdom.resources[selectedResource] ?? 0);
  }

  return (
    <div>
      <h2>Create Offer</h2>
      {lastCreateOfferResult && (
        <Typography variant="body1" component="p" color={lastCreateOfferResult.success ? "success" : "error"}>
          {lastCreateOfferResult.message}
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
                  setSellAmount(0);
                }}
              >
                {resource}
              </Button>
            ))}
          </ButtonGroup>
        </Grid>
        <Grid item>
          <InputLabel>Amount</InputLabel>
          <Input
            type="number"
            inputProps={{ min: 0 }}
            value={sellAmount}
            onChange={event => setSellAmount(parseInt(event.target.value))}
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
          <InputLabel>Price</InputLabel>
          <Input type="number" inputProps={{ min: 0 }} value={price} onChange={event => setPrice(parseInt(event.target.value))} />
        </Grid>
        <Grid item>
          <Button variant="contained" onClick={() => handleCreateOffer()}>
            Create Offer
          </Button>
        </Grid>
      </Grid>
    </div>
  );
};

export default MarketPost;
