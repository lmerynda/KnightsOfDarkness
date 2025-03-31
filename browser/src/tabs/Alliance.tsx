import React, { useContext } from "react";
import AllianceList from "../components/AllianceList";
import CreateAlliance from "../components/CreateAlliance";
import { KingdomContext } from "../Kingdom";
import KingdomAlliance from "../components/KingdomAlliance";
import type { AllianceData } from "../game-api-client/AllianceApi";
import { fetchAllianceRequest } from "../game-api-client/AllianceApi";

const Alliance: React.FC = () => {
  const [alliance, setAlliance] = React.useState<AllianceData | undefined>(undefined);
  const kingdomContext = useContext(KingdomContext);
  // ask someone how to better solve it, null object pattern?
  if (kingdomContext === undefined) {
    throw new Error("Kingdom context is undefined");
  }

  const reloadAlliance = React.useCallback(async () => {
    if (!kingdomContext.kingdom.allianceName) {
      setAlliance(undefined);
      return;
    }

    const data = await fetchAllianceRequest();
    if (data) {
      setAlliance(data);
    }
  }, [kingdomContext.kingdom.allianceName]);

  React.useEffect(() => {
    reloadAlliance();
  }, [reloadAlliance]);

  const leaveAlliance = React.useCallback(() => {
    setAlliance(undefined);
  }, []);

  return (
    <div>
      {alliance ? (
        <KingdomAlliance
          alliance={alliance}
          isEmperor={kingdomContext.kingdom.name === alliance.emperor}
          maxAllianceMembers={kingdomContext.gameConfig.common.allianceMaxMembers}
          leaveAlliance={leaveAlliance}
          reloadAlliance={reloadAlliance}
        />
      ) : (
        <>
          <CreateAlliance />
          <AllianceList />
        </>
      )}
    </div>
  );
};

export default Alliance;
