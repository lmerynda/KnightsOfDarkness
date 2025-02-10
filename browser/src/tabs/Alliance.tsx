import { Button, Table, TableBody, TableCell, TableHead, TableRow, Typography } from "@mui/material";
import React, { useContext } from "react";
import type { CarriersOnTheMove } from "../GameTypes";
import { KingdomContext } from "../Kingdom";
import SendCarriers from "../components/SendCarriers";
import CreateAlliance from "../components/CreateAlliance";

const Alliance: React.FC = () => {
  return (
    <div>
      <CreateAlliance />
    </div>
  );
};

export default Alliance;
