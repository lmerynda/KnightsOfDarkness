import React from 'react';
import KingdomResourcesView from './components/KingdomResources';
import { KingdomResources } from './components/Kingdom';

export type KingdomToolbarProps = {
    kingdomName: string;
    kingdomResources: KingdomResources;
};

const KingdomToolbar: React.FC<KingdomToolbarProps> = ({ kingdomName, kingdomResources }) => {
    return (
        <div>
            <h2>Kingdom {kingdomName}</h2>
            <KingdomResourcesView {...kingdomResources} />
        </div>
    );
};

export default KingdomToolbar;