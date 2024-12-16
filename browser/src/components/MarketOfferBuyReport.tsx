import React from "react";
import type { MarketOfferBuyResponse } from "../game-api-client/MarketApi";

const MarketOfferBuyReport: React.FC<MarketOfferBuyResponse> = buyResponse => {
  return (
    <div>
      <h2>Buy Report</h2>
      <ul>
        <li key="resource">Resource: {buyResponse.resource}</li>
        <li key="count">Amount bought: {buyResponse.count}</li>
        <li key="pricePerUnit">Price per unit: {buyResponse.pricePerUnit}</li>
        <li key="totalCost">Total cost: {buyResponse.totalCost}</li>
      </ul>
    </div>
  );
};

export default MarketOfferBuyReport;
