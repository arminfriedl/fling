import log from 'loglevel';
import React, {useState, useEffect} from 'react';

import {useParams, BrowserRouter} from 'react-router-dom';

import {flingClient} from '../../util/flingclient';

import DirectDownload from './DirectDownload';

export default function FlingUserUpload(props) {
    let { shareId } = useParams();
    let [fling, setFling] = useState({});

    useEffect(() => {
        flingClient.getFlingByShareId(shareId)
            .then(f => setFling(f));
    }, [shareId]);

    return(
        <div>
          {fling.sharing && fling.sharing.directDownload ? <DirectDownload fling={fling} />: ""}
        </div>
    );
}

