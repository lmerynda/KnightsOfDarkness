import React from 'react';

type KingdomData = {
    name: string;
};

export type KingdomProps = {
    kingdomName: string;
};

const Kingdom: React.FC<KingdomProps> = (kingdomProps: KingdomProps) => {
    // react state to hold market data from the server
    const [kingdom, setKingdom] = React.useState<KingdomData>();

    React.useEffect(() => {
        fetch(`http://localhost:8080/kingdom/${kingdomProps.kingdomName}`)
            .then(response => response.json())
            .then(kingdom => {
                setKingdom(kingdom);
            });
    }, [kingdomProps.kingdomName]);

    React.useEffect(() => {
        if (kingdom) {
            console.log(`Kingdom data: ${JSON.stringify(kingdom)}`);
        }
    }, [kingdom]);

    return (
        <div>
            <h1>Kingdom {kingdom ? kingdom.name : 'Loading...'}</h1>
        </div>
    );
};

export default Kingdom;