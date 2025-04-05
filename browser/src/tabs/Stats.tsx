import React from "react";
import { Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import { fetchKingdomStats } from "../game-api-client/KingdomApi";
import type { KingdomStatsDto } from "../GameTypes";

const Stats: React.FC = () => {
  const [stats, setStats] = React.useState<KingdomStatsDto[]>([]);

  const fetchStats = async (): Promise<void> => {
    const data = await fetchKingdomStats();
    setStats(data);
  };

  React.useEffect(() => {
    fetchStats();
  }, []);

  return (
    <div>
      <Typography variant="h4">Kingdom Stats</Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Kingdom Name</TableCell>
            <TableCell>Land</TableCell>
            <TableCell>Army</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {stats.map(stat => (
            <TableRow key={stat.kingdomName}>
              <TableCell>{stat.kingdomName}</TableCell>
              <TableCell>{stat.land}</TableCell>
              <TableCell>{stat.army}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default Stats;
