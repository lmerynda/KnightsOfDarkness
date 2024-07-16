import React, { useContext } from 'react';
import { KingdomContext } from '../Kingdom';
import Button from '@mui/material/Button';
import { GAME_API } from '../Consts';
import { Input } from '@mui/material';

const BuyLand: React.FC = () => {
    const [buyAmount, setBuyAmount] = React.useState<number>(0);
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleSubmit = () => {
        fetch(`${GAME_API}/kingdom/buy-land`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify(buyAmount)
        })
            .then((response) => {
                console.log(`Request successful, data: ${JSON.stringify(response.json)}`);
                if (response.ok) {
                    setBuyAmount(0);
                    kingdomContext.reloadKingdom();
                }
            })
            .catch((error) => {
                console.error('Error when buying land: ', error);
            });
    };

    return (
        <div>
            <Input type="number" value={buyAmount} onChange={(event) => setBuyAmount(parseInt(event.target.value))} />
            <Button variant="contained" onClick={handleSubmit}>Buy Land</Button>
        </div>
    );
};

export default BuyLand;
