import React from "react";
import type { BuildingActionReport } from "../GameTypes";

const BuildReport: React.FC<BuildingActionReport> = buildActionReport => {
  return (
    <div>
      {Object.keys(buildActionReport.buildings).length > 0 && (
        <div>
          <h2>Build Report</h2>
          <span>{buildActionReport.message}</span>
          <ul>
            {Object.entries(buildActionReport.buildings).map(([building, count]) => {
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
