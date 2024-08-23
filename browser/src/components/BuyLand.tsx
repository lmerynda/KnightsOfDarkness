import React, { useContext } from "react";
import { KingdomContext } from "../Kingdom";
import Button from "@mui/material/Button";
import { Input } from "@mui/material";
import { buyLandRequest } from "../game-api-client/KingdomApi";

const BuyLand: React.FC = () => {
  const [buyAmount, setBuyAmount] = React.useState<number>(0);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleSubmit = async () => {
    const data = await buyLandRequest(buyAmount);
    setBuyAmount(0);
    kingdomContext.reloadKingdom();
  };

  return (
    <div>
      <Input type="number" value={buyAmount} onChange={event => setBuyAmount(parseInt(event.target.value))} />
      <Button variant="contained" onClick={handleSubmit}>
        Buy Land
      </Button>
    </div>
  );
};

export default BuyLand;
