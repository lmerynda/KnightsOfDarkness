import React from 'react';
import './css/App.css';
import Market from './components/Market';
import Kingdom from './components/Kingdom';

const kingdomName = "uprzejmy";

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <div>
          <Kingdom kingdomName={kingdomName} />
        </div>
      </header>
    </div>
  );
}

export default App;