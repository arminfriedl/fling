import log from 'loglevel';
import React, {useState, useEffect} from 'react';

import {useParams, BrowserRouter} from 'react-router-dom';

import {flingClient} from '../../util/flingclient';

export default function FlingUser() {
    let { shareId } = useParams();
    let [fling, setFling] = useState({});

    useEffect(() => {
        flingClient.getFlingByShareId(shareId)
            .then(f => setFling(f));
    }, [shareId]);

    return(
        <div>
          Hello
        </div>
    );
}
