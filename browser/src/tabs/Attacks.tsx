import { Button, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React, { useContext } from "react";
import type { OngoingAttack } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import { withdrawAttack } from "../game-api-client/AttacksApi";
import SendAttack from "../components/SendAttack";

const Attacks: React.FC = () => {
  const [ongoingAttacks, setOngoingAttacks] = React.useState<OngoingAttack[]>([]);
  const kingdomContext = useContext(KingdomContext);

  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleWithdraw = async (id: string): Promise<void> => {
    await withdrawAttack(id);
    kingdomContext.reloadKingdom();
  };

  React.useEffect(() => {
    setOngoingAttacks(kingdomContext.kingdom.ongoingAttacks);
  }, [kingdomContext.kingdom.ongoingAttacks]);

  return (
    <div>
      <Typography variant="h4">Attacks</Typography>
      <SendAttack />
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Target</TableCell>
            <TableCell>Turns Left</TableCell>
            <TableCell>Attack Type</TableCell>
            <TableCell>Infantry</TableCell>
            <TableCell>Bowmen</TableCell>
            <TableCell>Cavarly</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {ongoingAttacks.map(ongoingAttack => (
            <TableRow key={ongoingAttack.id}>
              <TableCell>{ongoingAttack.to}</TableCell>
              <TableCell>{ongoingAttack.turnsLeft}</TableCell>
              <TableCell>{ongoingAttack.attackType}</TableCell>
              <TableCell>{ongoingAttack.units.infantry}</TableCell>
              <TableCell>{ongoingAttack.units.bowman}</TableCell>
              <TableCell>{ongoingAttack.units.cavalry}</TableCell>
              <TableCell>
                <Button variant="contained" onClick={() => handleWithdraw(ongoingAttack.id)}>
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

export default Attacks;
