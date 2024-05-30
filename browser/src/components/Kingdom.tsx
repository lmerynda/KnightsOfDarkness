import KingdomBuildings from "./KingdomBuildings";
import KingdomResources from "./KingdomResources";
import Market from "./Market";


export type KingdomData = {
    name: string;
};

export type KingdomProps = {
    kingdom: KingdomData;
}

const Kingdom: React.FC<KingdomProps> = ({ kingdom }) => {
    return (
        <div className="kingdom-container">
            <h1>Kingdom {kingdom.name}</h1>
            <div>
                <KingdomResources kingdom={kingdom} />
                <KingdomBuildings kingdom={kingdom} />
                <Market />
            </div>
        </div>
    );
};
export default Kingdom;