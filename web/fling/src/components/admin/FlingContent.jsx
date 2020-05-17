import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';
import {Switch, Route, Redirect, BrowserRouter, useLocation, useParams, Link} from "react-router-dom";

import classNames from 'classnames';

import {artifactClient} from '../../util/flingclient';

import FlingArtifacts from './FlingArtifacts';
import Upload from './Upload';
import Settings from './Settings';

export default function FlingContent(props) {
    let location = useLocation();
    let { fling } = useParams();

    function path(tail) {
        return `/admin/${props.activeFling}/${tail}`;
    }

    return(
        <div className="fling-content p-2">
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
              <Route exact path={["/admin/:fling/files","/admin"]}><FlingArtifacts activeFling={props.activeFling} /></Route>
              <Route path="/admin/:fling/upload"><Upload activeFling={props.activeFling} /></Route>
              <Route path="/admin/:fling/settings"><Settings activeFling={props.activeFling}/></Route>
            </Switch>
          </div>
        </div>
    );
}
