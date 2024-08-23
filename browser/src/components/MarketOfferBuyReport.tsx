import React from "react";
import { MarketOfferBuyResponse } from "../game-api-client/MarketApi";

const MarketOfferBuyReport: React.FC<MarketOfferBuyResponse> = buyResponse => {
  return (
    <div>
      <h2>Buy Report</h2>
      <ul>
        <li key={buyResponse.resource}>Resource: {buyResponse.resource}</li>
        <li key={buyResponse.count}>Amount bought: {buyResponse.count}</li>
        <li key={buyResponse.pricePerUnit}>Price per unit: {buyResponse.pricePerUnit}</li>
        <li key={buyResponse.totalCost}>Total cost: {buyResponse.totalCost}</li>
      </ul>
    </div>
  );
};

export default MarketOfferBuyReport;
