import React, { useState, useEffect } from 'react';

import { useParams } from 'react-router-dom';

import { FlingClient } from '../../util/fc';

import DirectDownload from './DirectDownload';
import FlingUserList from './FlingUserList';

export default function FlingUser() {
  let { shareId } = useParams();
  let [fling, setFling] = useState({});

  useEffect(() => {
    let flingClient = new FlingClient();
    flingClient.getFlingByShareId(shareId)
      .then(f => setFling(f));
  }, [shareId]);

  return (
    <div>
      {fling.sharing && fling.sharing.directDownload
        ? <DirectDownload fling={fling} />
        : <FlingUserList fling={fling} />}
    </div>
  );
}
