import { Button, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React, { useContext } from "react";
import { CarriersOnTheMove } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { withdrawCarriers } from "../game-api-client/TransfersApi";

const Transfers: React.FC = () => {
  const [carriersOnTheMove, setCarriersOnTheMove] = React.useState<CarriersOnTheMove[]>([]);
  const kingdomContext = useContext(KingdomContext);

  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleWithdraw = async (id: string) => {
    await withdrawCarriers(id);
    kingdomContext.reloadKingdom();
  };

  React.useEffect(() => {
    setCarriersOnTheMove(kingdomContext.kingdom.carriersOnTheMove);
  }, [kingdomContext.kingdom.carriersOnTheMove]);

  return (
    <div>
      <Typography variant="h4">Transfers</Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Resource</TableCell>
            <TableCell>To</TableCell>
            <TableCell>Carriers Count</TableCell>
            <TableCell>Amount</TableCell>
            <TableCell>Turns Left</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {carriersOnTheMove.map(transfer => (
            <TableRow key={transfer.id}>
              <TableCell>{transfer.resource}</TableCell>
              <TableCell>{transfer.to}</TableCell>
              <TableCell>{transfer.carriersCount}</TableCell>
              <TableCell>{transfer.amount}</TableCell>
              <TableCell>{transfer.turnsLeft}</TableCell>
              <TableCell>
                <Button variant="contained" onClick={() => handleWithdraw(transfer.id)}>
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

export default Transfers;
