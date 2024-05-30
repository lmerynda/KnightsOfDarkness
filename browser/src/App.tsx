import React from 'react';
import './css/App.css';
import Kingdom, { KingdomData } from './components/Kingdom';


const kingdomName = "uprzejmy";

const App: React.FC = () => {
  const [kingdom, setKingdom] = React.useState<KingdomData>();

  React.useEffect(() => {
    fetch(`http://localhost:8080/kingdom/${kingdomName}`)
      .then(response => response.json())
      .then(kingdom => {
        setKingdom(kingdom);
      });
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <div>
          Kingdom {kingdom ? <Kingdom kingdom={kingdom} /> : 'Loading...'}
        </div>
      </header>
    </div>
  );
}

export default App;