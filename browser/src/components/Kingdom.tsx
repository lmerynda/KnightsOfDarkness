import KingdomBuildingsView from "./KingdomBuildings";
import KingdomResourcesView from "./KingdomResources";
import KingdomUnitsView from "./KingdomUnits";
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

export type KingdomBuildings = {
    houses: number,
    goldMines: number,
    ironMines: number,
    workshops: number,
    farms: number,
    markets: number,
    barracks: number,
    guardHouses: number,
    spyGuilds: number,
    towers: number,
    castles: number
}

export type KingdomUnits = {
    goldMiners: number,
    ironMiners: number,
    farmers: number,
    blacksmiths: number,
    builders: number,
    carriers: number,
    guards: number,
    spies: number,
    infantry: number,
    bowmen: number,
    cavalry: number
}

export type KingdomData = {
    name: string;
    resources: KingdomResources;
    buildings: KingdomBuildings;
    units: KingdomUnits;
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
                <KingdomBuildingsView {...kingdom.buildings} />
                <KingdomUnitsView {...kingdom.units} />
                <Market />
            </div>
        </div>
    );
};
export default KingdomView;