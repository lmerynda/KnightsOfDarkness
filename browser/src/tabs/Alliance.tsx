import React from "react";
import AllianceList from "../components/AllianceList";
import CreateAlliance from "../components/CreateAlliance";

const Alliance: React.FC = () => {
  // TODO reloadAlliances after creating alliance
  return (
    <div>
      <CreateAlliance />
      <AllianceList />
    </div>
  );
};

export default Alliance;
