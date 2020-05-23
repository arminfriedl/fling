import log from 'loglevel';
import React, {useState} from 'react';

import Navbar from './Navbar';
import FlingList from './FlingList';
import FlingContent from './FlingContent';

import {useParams, BrowserRouter} from 'react-router-dom';

export default function Fling() {
    let { fling } = useParams();

    return(
        <div>
          <Navbar />

          <div className="container">
            <div className="columns mt-2">
              <div className="column col-sm-12 col-lg-3 col-2"> <FlingList activeFling={fling} /> </div>
              <div className="column col-sm-12"><FlingContent activeFling={fling} /></div>
            </div>
          </div>
        </div>
    );
}
