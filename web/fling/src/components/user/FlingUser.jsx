import log from 'loglevel';
import React, {useState} from 'react';

import {useParams, BrowserRouter} from 'react-router-dom';

export default function FlingAdmin() {
    let { fling } = useParams();

    return(
        <div>
          Hello
        </div>
    );
}
