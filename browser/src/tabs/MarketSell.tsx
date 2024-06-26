import { Button, Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import React, { useContext } from "react";
import { KingdomContext } from "../App";
import { GAME_API } from "../Consts";

const MarketSell: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
    }

    const handleWithdraw = (id: string): void => {
        fetch(`${GAME_API}/market/${id}/withdraw`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({})
        })
            .then(response => {
                if (response.ok) {
                    console.log(`Withdrawn offer ${id}`);
                    kingdomContext.reloadKingdom();
                } else {
                    console.error(`Failed to withdraw offer ${id}`);
                }
            })
            .catch(error => {
                console.error(`Failed to withdraw offer ${id} due to ${error ?? 'unknown error'}`);
            });
    }

    return (
        <div>
            <h1>Market Sell</h1>
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
                    {kingdomContext.kingdom.marketOffers.map((data) => (
                        <TableRow key={data.id}>
                            <TableCell>{data.sellerName}</TableCell>
                            <TableCell>{data.resource}</TableCell>
                            <TableCell>{data.price}</TableCell>
                            <TableCell>{data.count}</TableCell>
                            <TableCell>
                                <Button
                                    variant="contained"
                                    onClick={() => handleWithdraw(data.id)}
                                >
                                    Withdraw
                                </Button>
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default MarketSell;