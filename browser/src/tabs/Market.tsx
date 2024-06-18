import React from 'react';
import { Table, TableHead, TableBody, TableRow, TableCell, TextField, Button } from '@mui/material';
import { KingdomReloader } from '../App';

type MarketData = {
    id: string;
    kingdomName: string;
    resource: string;
    price: number;
    count: number;
};

type OfferBuyer = {
    buyer: string;
    count: number;
}

const Market: React.FC<KingdomReloader> = ({ reloadKingdom }) => {
    const [marketData, setMarketData] = React.useState<MarketData[]>([]);
    const [buyInputs, setBuyInputs] = React.useState<{ [id: string]: number }>({});

    const handleInputChange = (id: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
        const value = parseInt(event.target.value);
        setBuyInputs((prevAmounts) => ({
            ...prevAmounts,
            [id]: value,
        }));
    };

    const reloadMarket = () => {
        fetch('http://localhost:8080/market')
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
            buyer: 'uprzejmy',
            count: count
        };
        if (count <= 0) return;
        fetch(`http://localhost:8080/market/${id}/buy`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(offerBuyer)
        })
            .then(response => response.json())
            .then(data => {
                console.log(`Request successful, data: ${JSON.stringify(data)}`);
                reloadMarket();
                clearForm(id);
                reloadKingdom();
            })
            .catch(error => {
                console.error(`Failed to buy ${count} items of id: ${id}, due to ${error || 'unknown error'}`);
            });
    };

    const handleBuyPrice = (id: string, toSpend: number, price: number) => {
        const count = Math.floor(toSpend / price);
        handleBuyAmount(id, count);
    }

    return (
        <div>
            <h1>Market</h1>
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
                            <TableCell>{data.kingdomName}</TableCell>
                            <TableCell>{data.resource}</TableCell>
                            <TableCell>{data.price}</TableCell>
                            <TableCell>{data.count}</TableCell>
                            <TableCell>
                                <TextField
                                    type="number"
                                    id="buyAmountInput"
                                    inputProps={{ min: 0 }}
                                    value={buyInputs[data.id] || 0}
                                    onChange={handleInputChange(data.id)}
                                />
                                <Button
                                    variant="contained"
                                    onClick={() => handleBuyAmount(data.id, buyInputs[data.id] || 0)}
                                >
                                    Buy Amount
                                </Button>
                                <Button
                                    variant="contained"
                                    onClick={() => handleBuyPrice(data.id, buyInputs[data.id] || 0, data.price)}
                                >
                                    Buy Price
                                </Button>
                                <Button variant="contained">Max</Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default Market;
