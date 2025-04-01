import { Button, Input, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React from "react";
import {
  addBotToAllianceRequest,
  type AllianceData,
  leaveAllianceRequest,
  removeFromAllianceRequest,
} from "../game-api-client/AllianceApi";

type ActionResult = {
  success: boolean;
  message: string;
};

interface KingdomAllianceProps {
  alliance: AllianceData;
  isEmperor: boolean;
  maxAllianceMembers: number;
  leaveAlliance: () => void;
  reloadAlliance: () => Promise<void>;
}

const KingdomAlliance: React.FC<KingdomAllianceProps> = ({ alliance, isEmperor, maxAllianceMembers, leaveAlliance, reloadAlliance }) => {
  const [lastActionResult, setLastActionResult] = React.useState<ActionResult | undefined>(undefined);
  const [botName, setBotName] = React.useState<string>("");
  const hasMaxMembers = alliance.members.length >= maxAllianceMembers;

  const handleLeaveAlliance = async (): Promise<void> => {
    const result = await leaveAllianceRequest();
    setLastActionResult(result);
    if (result.success) {
      leaveAlliance();
    }
  };

  const handleRemoveFromAlliance = async (kingdomName: string): Promise<void> => {
    const result = await removeFromAllianceRequest({ kingdomName });
    setLastActionResult(result);
    if (result.success) {
      reloadAlliance();
    }
  };

  const handleAddBotToAlliance = async (): Promise<void> => {
    const result = await addBotToAllianceRequest({ botName });
    setLastActionResult(result);
    if (result.success) {
      setBotName("");
      reloadAlliance();
    }
  };

  return (
    <div>
      <h1>
        Alliance {alliance.name} led by Emperor {alliance.emperor}
      </h1>
      {lastActionResult && (
        <Typography variant="body1" component="p" color={lastActionResult.success ? "success" : "error"}>
          {lastActionResult.message}
        </Typography>
      )}
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Kingdom</TableCell>
            <TableCell>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {alliance.members?.map(member => (
            <TableRow key={member}>
              <TableCell>{member}</TableCell>
              <TableCell>
                {isEmperor && member !== alliance.emperor && (
                  <Button variant="contained" onClick={() => handleRemoveFromAlliance(member)}>
                    Boot
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      {isEmperor && !hasMaxMembers && (
        <>
          <Input type="text" value={botName} onChange={event => setBotName(event.target.value)} />
          <Button variant="contained" onClick={() => handleAddBotToAlliance()}>
            Add Bot to Alliance
          </Button>
        </>
      )}
      <Button variant="contained" onClick={handleLeaveAlliance}>
        leave
      </Button>
    </div>
  );
};

export default KingdomAlliance;
