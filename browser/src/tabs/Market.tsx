import React from 'react';
import { Table, TableHead, TableBody, TableRow, TableCell, TextField, Button } from '@mui/material';

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

const Market: React.FC = () => {
    const [marketData, setMarketData] = React.useState<MarketData[]>([]);
    const [buyAmounts, setBuyAmounts] = React.useState<{ [id: string]: number }>({});

    React.useEffect(() => {
        fetch('http://localhost:8080/market')
            .then(response => response.json())
            .then(data => {
                setMarketData(data);
            });
    }, []);

    React.useEffect(() => {
        if (marketData) {
            console.log(`Market data: ${JSON.stringify(marketData)}`);
        }
    }, [marketData]);

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
                // Handle the response data
            })
            .catch(error => {
                // Handle the error
            });
    };

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
                                    inputProps={{ min: 0 }}
                                    onChange={(event) => {
                                        const value = parseInt(event.target.value);
                                        setBuyAmounts((prevAmounts) => ({
                                            ...prevAmounts,
                                            [data.id]: value,
                                        }));
                                    }}
                                />
                                <Button
                                    variant="contained"
                                    onClick={() => handleBuyAmount(data.id, buyAmounts[data.id] || 0)}
                                >
                                    Buy Amount
                                </Button>
                                <Button variant="contained">Buy Price</Button>
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
