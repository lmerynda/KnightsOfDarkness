import React from 'react';
import './css/App.css';
import Market from './components/Market';
import Kingdom from './components/Kingdom';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <div>
          <Market />
        </div>
        <div>
          <Kingdom />
        </div>
      </header>
    </div>
  );
}

export default App;
