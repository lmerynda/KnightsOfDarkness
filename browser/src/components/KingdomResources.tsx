import React from 'react';
import { KingdomResources } from './Kingdom';

const KingdomResourcesView: React.FC<KingdomResources> = (kingdomResources) => {
    return (
        <div className='kingdom-content-container'>
            <div><h6>Resources</h6></div>
            <div>
                <div>gold: {kingdomResources.gold}</div>
                <div>food: {kingdomResources.food}</div>
                <div>iron: {kingdomResources.iron}</div>
                <div>tools: {kingdomResources.tools}</div>
                <div>weapons: {kingdomResources.weapons}</div>
            </div>
        </div>
    );
};
export default KingdomResourcesView;