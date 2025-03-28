import { Button, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React from "react";
import { type AllianceData, leaveAllianceRequest } from "../game-api-client/AllianceApi";

type ActionResult = {
  success: boolean;
  message: string;
};

interface KingdomAllianceProps {
  alliance: AllianceData;
  leaveAlliance: () => void;
}

const KingdomAlliance: React.FC<KingdomAllianceProps> = ({ alliance, leaveAlliance }) => {
  const [lastActionResult, setLastActionResult] = React.useState<ActionResult | undefined>(undefined);

  const handleLeaveAlliance = async (): Promise<void> => {
    const result = await leaveAllianceRequest();
    console.log(`leave result: ${JSON.stringify(result)}`);
    setLastActionResult(result);
    if (result.success) {
      leaveAlliance();
    }
  };

  return (
    <div>
      <h1>Alliance {alliance.name}</h1>
      {lastActionResult && (
        <Typography variant="body1" component="p" color={lastActionResult.success ? "success" : "error"}>
          {lastActionResult.message}
        </Typography>
      )}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Emperor</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow key={alliance.name}>
            <TableCell>{alliance.name}</TableCell>
            <TableCell>{alliance.emperor}</TableCell>
          </TableRow>
        </TableBody>
      </Table>
      <Button variant="contained" onClick={handleLeaveAlliance}>
        leave
      </Button>
    </div>
  );
};

export default KingdomAlliance;
