import React from 'react';
import { Table, TableHead, TableBody, TableRow, TableCell } from '@mui/material';

type MarketData = {
    id: string;
    kingdomName: string;
    resource: string;
    price: number;
    count: number;
};

// const useStyles = makeStyles({
//     root: {
//         margin: '20px',
//     },
//     table: {
//         minWidth: 650,
//     },
// });

const Market: React.FC = () => {

    const [marketData, setMarketData] = React.useState<MarketData[]>([]);

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
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>Kingdom</TableCell>
                        <TableCell>Resource</TableCell>
                        <TableCell>Price</TableCell>
                        <TableCell>Count</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {marketData.map((data) => (
                        <TableRow key={data.id}>
                            <TableCell>{data.kingdomName}</TableCell>
                            <TableCell>{data.resource}</TableCell>
                            <TableCell>{data.price}</TableCell>
                            <TableCell>{data.count}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
};

export default Market;
