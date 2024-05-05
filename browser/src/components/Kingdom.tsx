import React from 'react';

type KingdomData = {
    id: string;
    name: string;
};

const Kingdom: React.FC = () => {
    // react state to hold market data from the server
    const [kingdom, setKingdom] = React.useState<KingdomData>();

    React.useEffect(() => {
        fetch('http://localhost:8080/kingdom')
            .then(response => response.json())
            .then(kingdom => {
                setKingdom(kingdom);
            });
    }, []);

    React.useEffect(() => {
        if (kingdom) {
            console.log(`Kingdom data: ${JSON.stringify(kingdom)}`);
        }
    }, [kingdom]);

    return (
        <div>
            <h1>Kingdom</h1>
            <table>
                <thead>
                    <tr>
                        <th>Kingdom name</th>
                    </tr>
                </thead>
                <tbody>
                    {kingdom ?
                        <tr key={kingdom.id}>
                            <td>{kingdom.name}</td>
                        </tr>
                        : ''
                    }
                </tbody>
            </table>
        </div>
    );
};

export default Kingdom;