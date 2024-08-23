import { Button, Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import MarketPost from "../components/MarketPost";
import { withdrawMarketOfferRequest } from "../game-api-client/MarketApi";

const MarketSell: React.FC = () => {
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleWithdraw = async (id: string) => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const data = await withdrawMarketOfferRequest(id);
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <h1>Market Sell</h1>
      <MarketPost />
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
          {kingdomContext.kingdom.marketOffers.map(data => (
            <TableRow key={data.id}>
              <TableCell>{data.sellerName}</TableCell>
              <TableCell>{data.resource}</TableCell>
              <TableCell>{data.price}</TableCell>
              <TableCell>{data.count}</TableCell>
              <TableCell>
                <Button variant="contained" onClick={() => handleWithdraw(data.id)}>
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
