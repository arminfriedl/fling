import React, { useState, useEffect } from 'react';

import { useParams } from 'react-router-dom';

import { FlingClient } from '../../util/fc';

import DirectDownload from './DirectDownload';
import FlingUserList from './FlingUserList';

export default function FlingUser() {
  let { shareId } = useParams();
  let [fling, setFling] = useState({});
  let [loading, setLoading] = useState(true);

  useEffect(() => {
    let flingClient = new FlingClient();
    flingClient.getFlingByShareId(shareId)
      .then(f => {
        setFling(f);
        setLoading(false);
      });
  }, [shareId]);

  return (
    <>
    {loading
     ? <div></div>
     : <div>
        {fling.shared && fling.directDownload
          ? <DirectDownload fling={fling} />
          : <FlingUserList fling={fling} />}
       </div>
    }
    </>
  );
}
