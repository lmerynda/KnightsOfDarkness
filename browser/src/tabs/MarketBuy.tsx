import React, { useContext } from 'react';
import { Table, TableHead, TableBody, TableRow, TableCell, Button, Input, ButtonGroup } from '@mui/material';
import { KingdomContext } from '../Kingdom';
import { MarketData, OfferBuyer } from "../GameTypes";
import { GAME_API } from '../Consts';

const MarketBuy: React.FC = () => {
    const [marketData, setMarketData] = React.useState<MarketData[]>([]);
    const [buyInputs, setBuyInputs] = React.useState<{ [id: string]: number }>({});
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    console.log(`Kingdom context: ${JSON.stringify(kingdomContext.kingdom.name)}`);

    const handleInputChange = (id: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(event.target.value);
        setBuyInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: value,
        }));
    };

    const reloadMarket = () => {
        fetch(`${GAME_API}/market`,
            {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
                },
            }
        )
            .then(response => response.json())
            .then(data => {
                console.log(`Request successful, data: ${JSON.stringify(data)}`);
                setMarketData(data);
            })
            .catch(error => {
                console.error(`Failed to fetch market data due to ${error ?? 'unknown error'}`);
            });
    }

    React.useEffect(() => {
        reloadMarket();
    }, []);

    const clearForm = (id: string) => {
        console.log(`Clearing form for id: ${id}`);
        setBuyInputs((prevInputs) => ({
            ...prevInputs,
            [id]: 0,
        }));
    };

    const handleBuyAmount = (id: string, count: number) => {
        const offerBuyer: OfferBuyer = {
            count: count
        };
        if (count <= 0) return;
        fetch(`${GAME_API}/market/${id}/buy`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('authToken') || ''}`,
            },
            body: JSON.stringify(offerBuyer)
        })
            .then(response => response.json())
            .then(data => {
                console.log(`Request successful, data: ${JSON.stringify(data)}`);
                reloadMarket();
                clearForm(id);
                kingdomContext.reloadKingdom();
            })
            .catch(error => {
                console.error(`Failed to buy ${count} items of id: ${id}, due to ${error || 'unknown error'}`);
            });
    };

    const handleBuyPrice = (id: string, toSpend: number, price: number) => {
        const count = Math.floor(toSpend / price);
        handleBuyAmount(id, count);
    }

    function handleMaxInput(id: string, price: number, count: number): void {
        if (kingdomContext === undefined) {
            return;
        }
        const gold = kingdomContext.kingdom.resources.gold;
        const maxToAfford = Math.floor(gold / price);
        const maxToBuy = Math.min(maxToAfford, count);

        setBuyInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: maxToBuy,
        }));
    }

    return (
        <div>
            <h1>Market Buy</h1>
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Kingdom</TableCell>
                        <TableCell>Resource</TableCell>
                        <TableCell>Price</TableCell>
                        <TableCell>Count</TableCell>
                        <TableCell>Actions</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {marketData.map((data) => (
                        <TableRow key={data.id}>
                            <TableCell>{data.sellerName}</TableCell>
                            <TableCell>{data.resource}</TableCell>
                            <TableCell>{data.price}</TableCell>
                            <TableCell>{data.count}</TableCell>
                            <TableCell>
                                <Input
                                    type="number"
                                    inputProps={{ min: 0 }}
                                    value={buyInputs[data.id] || 0}
                                    onChange={handleInputChange(data.id)}
                                />
                                <ButtonGroup variant="contained" >
                                    <Button
                                        onClick={() => handleBuyAmount(data.id, buyInputs[data.id] || 0)}
                                    >
                                        Buy Amount
                                    </Button>
                                    <Button
                                        onClick={() => handleBuyPrice(data.id, buyInputs[data.id] || 0, data.price)}
                                    >
                                        Buy Price
                                    </Button>
                                    <Button
                                        onClick={() => handleMaxInput(data.id, data.price, data.count)}
                                    >
                                        Max
                                    </Button>
                                </ButtonGroup>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default MarketBuy;