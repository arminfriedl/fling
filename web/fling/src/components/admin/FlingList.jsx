import React from 'react';
import { useSelector } from "react-redux";

import FlingTile from './FlingTile';

export default function FlingList() {
  const flings = useSelector((store) => store.flings.flings);

  return (
    <div className="panel">
      <div className="panel-header p-2">
        <h5>My Flings</h5>
      </div>
      <div className="panel-body p-0">
        {flings.map(fling => <FlingTile fling={fling} key={fling.id} />)}
      </div>
    </div>
  );
}
