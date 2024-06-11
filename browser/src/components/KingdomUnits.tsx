import React from 'react';
import { KingdomUnits } from '../GameTypes';

const KingdomUnitsView: React.FC<KingdomUnits> = (kingdomUnits) => {
    return (
        <div className='kingdom-content-container'>
            <div><h6>Units</h6></div>
            <div>
                <div>goldMiners: {kingdomUnits.goldMiners}</div>
                <div>ironMiners: {kingdomUnits.ironMiners}</div>
                <div>farmers: {kingdomUnits.farmers}</div>
                <div>blacksmiths: {kingdomUnits.blacksmiths}</div>
                <div>builders: {kingdomUnits.builders}</div>
                <div>carriers: {kingdomUnits.carriers}</div>
                <div>guards: {kingdomUnits.guards}</div>
                <div>spies: {kingdomUnits.spies}</div>
                <div>infantry: {kingdomUnits.infantry}</div>
                <div>bowmen: {kingdomUnits.bowmen}</div>
                <div>cavalry: {kingdomUnits.cavalry}</div>
            </div>
        </div>
    );
};
export default KingdomUnitsView;