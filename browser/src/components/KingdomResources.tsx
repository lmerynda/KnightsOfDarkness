import React from 'react';
import { KingdomResources } from './Kingdom';

const KingdomResourcesView: React.FC<KingdomResources> = (kingdomResources) => {
    return (
        <div className="resources-container">
            <div><h6>Resources</h6></div>
            <div>
                <span>gold: {kingdomResources.gold}</span>
                <span>food: {kingdomResources.food}</span>
                <span>iron: {kingdomResources.iron}</span>
                <span>tools: {kingdomResources.tools}</span>
                <span>weapons: {kingdomResources.weapons}</span>
            </div>
        </div>
    );
};
export default KingdomResourcesView;