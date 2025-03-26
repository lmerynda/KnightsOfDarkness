import { Button, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React, { useContext } from "react";
import { type AllianceData, fetchAllianceRequest, leaveAllianceRequest } from "../game-api-client/AllianceApi";
import { KingdomContext } from "../Kingdom";

type ActionResult = {
  success: boolean;
  message: string;
};

const KingdomAlliance: React.FC = () => {
  const [alliance, setAlliance] = React.useState<AllianceData | undefined>(undefined);
  const [lastActionResult, setLastActionResult] = React.useState<ActionResult | undefined>(undefined);

  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const allianceName = kingdomContext.kingdom.allianceName;

  if (allianceName === null) {
    // some log? user should never be here
    return <div>You're not in the alliance</div>;
  }

  const reloadAlliance = React.useCallback(async () => {
    const data = await fetchAllianceRequest();
    setAlliance(data);
  }, []);

  React.useEffect(() => {
    reloadAlliance();
  }, [reloadAlliance]);

  if (alliance === undefined) {
    return <div>Loading {allianceName} ...</div>;
  }

  const handleLeaveAlliance = async () => {
    const result = await leaveAllianceRequest();
    console.log(`leave result: ${JSON.stringify(result)}`);
    setLastActionResult(result);
    reloadAlliance();
  };

  return (
    <div>
      <h1>Alliance {allianceName}</h1>
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
