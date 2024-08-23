import React from "react";
import { TrainingResponse } from "../game-api-client/KingdomApi";

const TrainingReport: React.FC<TrainingResponse> = trainingResponse => {
  return (
    <div>
      {Object.keys(trainingResponse).length > 0 && (
        <div>
          <h2>Training Report</h2>
          <ul>
            {Object.entries(trainingResponse).map(([unit, count]) => {
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
