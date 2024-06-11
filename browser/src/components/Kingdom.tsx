import { KingdomData } from "../GameTypes";
import KingdomBuildingsView from "./KingdomBuildings";
import KingdomResourcesView from "./KingdomResources";
import KingdomUnitsView from "./KingdomUnits";
import Market from "./Market";

export type KingdomProps = {
    kingdom: KingdomData;
}

const KingdomView: React.FC<KingdomProps> = ({ kingdom }) => {
    return (
        <div className="kingdom-container">
            <h1>Kingdom {kingdom.name}</h1>
            <div><KingdomResourcesView {...kingdom.resources} /></div>
            <div className="kingdom-main-container">
                <div>
                    <KingdomBuildingsView {...kingdom.buildings} />
                    <KingdomUnitsView {...kingdom.units} />
                </div>
                <div className="kingdom-activity-container">
                    <Market />
                </div>
            </div>

        </div>
    );
};
export default KingdomView;