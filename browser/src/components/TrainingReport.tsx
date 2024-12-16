import React from "react";
import type { TrainingActionReport } from "../GameTypes";

const TrainingReport: React.FC<TrainingActionReport> = trainingResponse => {
  return (
    <div>
      {Object.keys(trainingResponse.units).length > 0 && (
        <div>
          <h2>Training Report</h2>
          <span>{trainingResponse.message}</span>
          <ul>
            {Object.entries(trainingResponse.units).map(([unit, count]) => {
              if (count > 0) {
                return (
                  <li key={unit}>
                    <strong>{unit}:</strong> {count}
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

export default TrainingReport;
