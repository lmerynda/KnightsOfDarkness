import { Button, ButtonGroup, Grid, IconButton, Input, InputAdornment, InputLabel, Typography } from "@mui/material";
import React, { useContext } from "react";
import { MarketResource } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { CreateMarketOfferData, createMarketOfferRequest } from "../game-api-client/MarketApi";

const MarketPost: React.FC = () => {
    const [sellAmount, setSellAmount] = React.useState<number>(0);
    const [price, setPrice] = React.useState<number>(0);
    const [selectedResource, setSelectedResource] = React.useState<MarketResource>(MarketResource.food);
    const kingdomContext = useContext(KingdomContext);

    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleCreateOffer = async () => {
        const offer: CreateMarketOfferData = {
            resource: selectedResource,
            price: price,
            count: sellAmount
        };

        const data = await createMarketOfferRequest(offer);
        setSellAmount(0);
        setPrice(0);
        setSelectedResource(MarketResource.food);
        kingdomContext.reloadKingdom();
    }

    function handleMaxClick(): void {
        setSellAmount(kingdomContext?.kingdom.resources[selectedResource] ?? 0);
    }

    return (
        <div>
            <h2>Create Offer</h2>
            <Grid container spacing={2} alignItems="center">
                <Grid item>
                    <ButtonGroup variant="contained" >
                        {Object.values(MarketResource).map((resource) => (
                            <Button
                                key={resource}
                                variant={resource === selectedResource ? 'contained' : 'outlined'}
                                onClick={() => { setSelectedResource(resource); setSellAmount(0) }}
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
                        onChange={(event) => setSellAmount(parseInt(event.target.value))}
                        endAdornment={
                            <InputAdornment position="end">
                                <IconButton onClick={handleMaxClick}>
                                    <Typography variant="body2" component="span" style={{ fontWeight: 'bold' }}>
                                        max
                                    </Typography>
                                </IconButton>
                            </InputAdornment>
                        }
                    />
                </Grid>
                <Grid item>
                    <InputLabel>Price</InputLabel>
                    <Input
                        type="number"
                        inputProps={{ min: 0 }}
                        value={price}
                        onChange={(event) => setPrice(parseInt(event.target.value))}
                    />
                </Grid>
                <Grid item>
                    <Button variant="contained" onClick={() => handleCreateOffer()}>Create Offer</Button>
                </Grid>
            </Grid>
        </div>
    );
}

export default MarketPost;