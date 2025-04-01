import { Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";
import React from "react";
import { type AllianceShortData, fetchAllAlliancesRequest } from "../game-api-client/AllianceApi";

const AllianceList: React.FC = () => {
  const [alliancesList, setAlliancesList] = React.useState<AllianceShortData[]>([]);

  const reloadAlliances = React.useCallback(async () => {
    const data = await fetchAllAlliancesRequest();
    setAlliancesList(data);
  }, []);

  React.useEffect(() => {
    reloadAlliances();
  }, [reloadAlliances]);

  return (
    <div>
      <h1>Alliances</h1>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>Emperor</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {alliancesList.map(alliance => (
            <TableRow key={alliance.name}>
              <TableCell>{alliance.name}</TableCell>
              <TableCell>{alliance.emperor}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default AllianceList;
