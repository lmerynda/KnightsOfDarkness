import { Button, Input, MenuItem, Select } from "@mui/material";
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

    return (
        <div>
            <h2>Create Offer</h2>
            {ResourcesTypes.map((type) => (
                    <Button
                        key={type}
                        variant={resource === type ? 'contained' : 'outlined'}
                        onClick={() => setResource(type)}
                    >
                        {type}
                    </Button>
                ))}
            <Input
                type="number"
                inputProps={{ min: 0 }}
                value={sellAmount}
                onChange={(event) => setSellAmount(parseInt(event.target.value))}
            />
            <Input
                type="number"
                inputProps={{ min: 0 }}
                value={price}
                onChange={(event) => setPrice(parseInt(event.target.value))}
            />
        </div>
    );
}

export default MarketPost;