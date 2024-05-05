import React from 'react';

type MarketData = {
    id: string;
    kingdom: string;
    resource: string;
    price: number;
    count: number;
};

const Market: React.FC = () => {
    // react state to hold market data from the server
    const [marketData, setMarketData] = React.useState<MarketData[]>();

    React.useEffect(() => {
        fetch('http://localhost:8080/market')
            .then(response => response.json())
            .then(data => {
                setMarketData(data);
            });
    }, []);

    React.useEffect(() => {
        if (marketData) {
            console.log(`Market data: ${JSON.stringify(marketData)}`);
        }
    }, [marketData]);

    return (
        <div>
            <h1>Market</h1>
            <table>
                <thead>
                    <tr>
                        <th>Kingdom</th>
                        <th>Resource</th>
                        <th>Price</th>
                        <th>Count</th>
                    </tr>
                </thead>
                <tbody>
                    {marketData?.map((data) => (
                        <tr key={data.id}>
                            <td>{data.kingdom}</td>
                            <td>{data.resource}</td>
                            <td>{data.price}</td>
                            <td>{data.count}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default Market;
