import log from 'loglevel';
import React from 'react';
import {Switch, Route, useLocation, Link} from "react-router-dom";

import FlingArtifacts from './FlingArtifacts';
import Upload from './Upload';
import Settings from './Settings';

export default function FlingContent(props) {
    let location = useLocation();

    function path(tail) {
        return `/admin/${props.activeFling}/${tail}`;
    }

    return(
        <div className="fling-content p-2">
          {log.info("FlingContent location ", location)}
          {log.info("FlingContent active fling ", props.activeFling)}
          <ul className="tab tab-block mt-0">
            <li className={`tab-item ${location.pathname !== path("upload") && location.pathname !== path("settings") ? "active": ""}`}>
              <Link to={path("files")}>Files</Link>
            </li>
            <li className={`tab-item ${location.pathname === path("upload") ? "active": ""}`}>
              <Link to={path("upload")}>Upload</Link>
            </li>
            <li className={`tab-item ${location.pathname === path("settings") ? "active": ""}`}>
              <Link to={path("settings")}>Settings</Link>
            </li>
          </ul>

          <div className="mt-2">
            <Switch>
              <Route exact path="/admin/:fling"><FlingArtifacts activeFling={props.activeFling} /></Route>
              <Route path="/admin/:fling/files"><FlingArtifacts activeFling={props.activeFling} /></Route>
              <Route path="/admin/:fling/upload"><Upload activeFling={props.activeFling} /></Route>
              <Route path="/admin/:fling/settings"><Settings activeFling={props.activeFling} /></Route>
            </Switch>
          </div>
        </div>
    );
}
