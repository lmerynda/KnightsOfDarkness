import React from "react";
import { BuildResponse } from "../game-api-client/KingdomApi";

const BuildReport: React.FC<BuildResponse> = buildResponse => {
  return (
    <div>
      {Object.keys(buildResponse).length > 0 && (
        <div>
          <h2>Build Report</h2>
          <ul>
            {Object.entries(buildResponse).map(([building, count]) => {
              if (count > 0) {
                return (
                  <li key={building}>
                    <strong>{building}:</strong> {count}
                  </li>
                );
              }
              return null;
            })}
          </ul>
        </div>
      )}
    </div>
  );
};

export default BuildReport;
