import React from 'react';
import { KingdomResources } from '../GameTypes';

const KingdomResourcesView: React.FC<KingdomResources> = (kingdomResources) => {
    return (
        <div className="resources-container">
            <span>gold: {kingdomResources.gold}</span>
            <span>food: {kingdomResources.food}</span>
            <span>iron: {kingdomResources.iron}</span>
            <span>tools: {kingdomResources.tools}</span>
            <span>weapons: {kingdomResources.weapons}</span>
        </div>
    );
};
export default KingdomResourcesView;