import KingdomBuildings from "./KingdomBuildings";
import KingdomResourcesView from "./KingdomResources";
import Market from "./Market";

export type KingdomResources = {
    food: number;
    gold: number;
    iron: number;
    land: number;
    tools: number;
    weapons: number;
    buildingPoints: number;
    unemployed: number;
    turns: number;
}

export type KingdomData = {
    name: string;
    resources: KingdomResources;
};

export type KingdomProps = {
    kingdom: KingdomData;
}

const KingdomView: React.FC<KingdomProps> = ({ kingdom }) => {
    return (
        <div className="kingdom-container">
            <h1>Kingdom {kingdom.name}</h1>
            <div>
                <KingdomResourcesView {...kingdom.resources} />
                <KingdomBuildings kingdom={kingdom} />
                <Market />
            </div>
        </div>
    );
};
export default KingdomView;