import React from 'react';
import { KingdomBuildings } from '../GameTypes';

const KingdomBuildingsView: React.FC<KingdomBuildings> = (kingdomBuildings) => {
    return (
        <div className='kingdom-content-container'>
            <div><h6>Buildings</h6></div>
            <div>
                <div>houses: {kingdomBuildings.houses}</div>
                <div>goldMines: {kingdomBuildings.goldMines}</div>
                <div>ironMines: {kingdomBuildings.ironMines}</div>
                <div>workshops: {kingdomBuildings.workshops}</div>
                <div>farms: {kingdomBuildings.farms}</div>
                <div>markets: {kingdomBuildings.markets}</div>
                <div>barracks: {kingdomBuildings.barracks}</div>
                <div>guardHouses: {kingdomBuildings.guardHouses}</div>
                <div>spyGuilds: {kingdomBuildings.spyGuilds}</div>
                <div>towers: {kingdomBuildings.towers}</div>
                <div>castles: {kingdomBuildings.castles}</div>
            </div>
        </div>
    );
};
export default KingdomBuildingsView;