import React, { useContext } from "react";
import { Table, TableHead, TableBody, TableRow, TableCell, Button, Input, ButtonGroup } from "@mui/material";
import { KingdomContext } from "../Kingdom";
import { MarketOfferData, MarketResource, OfferBuyer } from "../GameTypes";
import { buyMarketOfferRequest, fetchMarketDataRequest, MarketOfferBuyResponse } from "../game-api-client/MarketApi";
import MarketOfferBuyReport from "./MarketOfferBuyReport";

export type MarketBuyResourceProps = {
  resource: MarketResource;
};

const MarketBuyResource: React.FC<MarketBuyResourceProps> = (props: MarketBuyResourceProps) => {
  const [marketData, setMarketData] = React.useState<MarketOfferData[]>([]);
  const [buyInputs, setBuyInputs] = React.useState<{ [id: string]: number }>({});
  const [lastBuyReport, setLastBuyReport] = React.useState<MarketOfferBuyResponse | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const handleInputChange = (id: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(event.target.value);
    setBuyInputs(prevAmounts => ({
      ...prevAmounts,
      [id]: value,
    }));
  };

  const reloadMarket = async () => {
    const data = await fetchMarketDataRequest(props.resource);
    setMarketData(data);
  };

  React.useEffect(() => {
    reloadMarket();
  }, [props.resource]);

  const clearForm = (id: string) => {
    setBuyInputs(prevInputs => ({
      ...prevInputs,
      [id]: 0,
    }));
  };

  const handleBuyAmount = async (id: string, count: number) => {
    const offerBuyer: OfferBuyer = {
      count: count,
    };
    if (count <= 0) return;

    const data = await buyMarketOfferRequest(id, offerBuyer);
    setLastBuyReport(data);
    reloadMarket();
    clearForm(id);
    kingdomContext.reloadKingdom();
  };

  const handleBuyPrice = (id: string, toSpend: number, price: number) => {
    const count = Math.floor(toSpend / price);
    handleBuyAmount(id, count);
  };

  function handleMaxInput(id: string, price: number, count: number): void {
    if (kingdomContext === undefined) {
      return;
    }
    const gold = kingdomContext.kingdom.resources.gold;
    const maxToAfford = Math.floor(gold / price);
    const maxToBuy = Math.min(maxToAfford, count);

    setBuyInputs(prevAmounts => ({
      ...prevAmounts,
      [id]: maxToBuy,
    }));
  }

  return (
    <div>
      <h1>Market Buy</h1>
      {lastBuyReport && <MarketOfferBuyReport {...lastBuyReport} />}
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
          {marketData.map(data => (
            <TableRow key={data.id}>
              <TableCell>{data.sellerName}</TableCell>
              <TableCell>{data.resource}</TableCell>
              <TableCell>{data.price}</TableCell>
              <TableCell>{data.count}</TableCell>
              <TableCell>
                <Input type="number" inputProps={{ min: 0 }} value={buyInputs[data.id] || 0} onChange={handleInputChange(data.id)} />
                <ButtonGroup variant="contained">
                  <Button onClick={() => handleBuyAmount(data.id, buyInputs[data.id] || 0)}>Buy Amount</Button>
                  <Button onClick={() => handleBuyPrice(data.id, buyInputs[data.id] || 0, data.price)}>Buy Price</Button>
                  <Button onClick={() => handleMaxInput(data.id, data.price, data.count)}>Max</Button>
                </ButtonGroup>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default MarketBuyResource;
