import log from 'loglevel';
import React, {useState, useEffect} from 'react';

import {FlingClient} from '../../util/fc';

import FlingTile from './FlingTile';

export default function FlingList(props) {
  const [flings, setFlings] = useState([]);
  useEffect(() => {
    let flingClient = new FlingClient();
    flingClient.getFlings()
      .then(flings => {
        let newFlings = [];
        for (let fling of flings) {
          let flingTile = <FlingTile fling={fling} key={fling.id} />;
          newFlings.push(flingTile);
        }
        setFlings(newFlings);
      }).catch(log.error);
  }, []);

  return(
    <div className="panel">
      {log.info(`Got active fling: ${props.activeFling}`)}
      <div className="panel-header p-2">
        <h5>My Flings</h5>
      </div>
      <div className="panel-body p-0">
        {flings}
      </div>
    </div>
  );
}
