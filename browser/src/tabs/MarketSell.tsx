import { Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import React, { useContext } from "react";
import { KingdomContext } from "../App";

const MarketSell: React.FC = () => {
    const kingdomContext = useContext(KingdomContext);
    // ask someone how to better solve it, null object pattern?
    if (kingdomContext === undefined) {
        throw new Error('Kingdom context is undefined');
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
                    </TableRow>
                </TableHead>
                <TableBody>
                    {kingdomContext.kingdom.marketOffers.map((data) => (
                        <TableRow key={data.id}>
                            <TableCell>{data.sellerName}</TableCell>
                            <TableCell>{data.resource}</TableCell>
                            <TableCell>{data.price}</TableCell>
                            <TableCell>{data.count}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default MarketSell;