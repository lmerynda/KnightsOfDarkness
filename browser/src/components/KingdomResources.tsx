import React from 'react';
import { KingdomProps } from './Kingdom';

const KingdomResources: React.FC<KingdomProps> = ({ kingdom }) => {
    return (
        <div className='kingdom-content-container'>
            <div><h6>Resources</h6></div>
            <div>
                <div>Raz</div>
                <div>Dwa</div>
                <div>Trzy</div>
            </div>
        </div>
    );
};
export default KingdomResources;