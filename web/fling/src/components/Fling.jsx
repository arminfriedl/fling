import log from 'loglevel';
import React, {useState} from 'react';

import Navbar from './Navbar';
import FlingList from './FlingList';
import FlingContent from './FlingContent';

import {HashRouter} from 'react-router-dom';

export default function Fling() {
    const [activeFling, setActiveFling] = useState(undefined);

    return(
        <div>
          <Navbar />
          <div className="container">
            <div className="columns mt-2">
              <div className="column col-sm-12 col-lg-3 col-2"> <FlingList setActiveFlingFn={setActiveFling} activeFling={activeFling} /> </div>
              <div className="column col-sm-12">
                  <FlingContent activeFling={activeFling} />
              </div>
            </div>
          </div>
        </div>
    );
}
