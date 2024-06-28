import { Button, ButtonGroup, Grid, Input, InputLabel } from "@mui/material";
import React, { useContext } from "react";
import { KingdomContext } from "../App";

const MarketPost: React.FC = () => {
    const [sellAmount, setSellAmount] = React.useState<number>(0);
    const [price, setPrice] = React.useState<number>(0);
    const [resource, setResource] = React.useState<string>('food');
    const kingdomContext = useContext(KingdomContext);

    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const ResourcesTypes = ['food', 'iron', 'tools', 'weapons'];

    function handleCreateOffer(): void {
        throw new Error("Function not implemented.");
    }

    return (
        <div>
            <h2>Create Offer</h2>
            <Grid container spacing={2} alignItems="center">
                <Grid item>
                    <ButtonGroup variant="contained" >
                        {ResourcesTypes.map((type) => (
                            <Button
                                key={type}
                                variant={resource === type ? 'contained' : 'outlined'}
                                onClick={() => setResource(type)}
                            >
                                {type}
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