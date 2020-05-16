import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';
import {Switch, Route, Redirect, HashRouter, useLocation} from "react-router-dom";

import classNames from 'classnames';

import {artifactClient} from '../util/flingclient';

import FlingArtifacts from './FlingArtifacts';
import Upload from './Upload';
import Settings from './Settings';

import './FlingContent.scss';

export default function FlingContent(props) {
    let location = useLocation();

    return(
        <div className="fling-content p-2">
          <ul className="tab tab-block mt-0">
            <li className={`tab-item ${location.hash !== "#/upload" && location.hash !== "#/settings" ? "active": ""}`}>
              <a href="#/files">Files</a>
            </li>
            <li className={`tab-item ${location.hash === "#/upload"? "active": ""}`}>
              <a href="#/upload">Upload</a>
            </li>
            <li className={`tab-item ${location.hash === "#/settings"? "active": ""}`}>
              <a href="#/settings">Settings</a>
            </li>
          </ul>

          <div className="mt-2">
            <HashRouter>
              <Switch>
                <Route exact path={["/files","/"]}><FlingArtifacts activeFling={props.activeFling} /></Route>
                <Route path="/upload"><Upload activeFling={props.activeFling} /></Route>
                <Route path="/settings"><div><Settings activeFling={props.activeFling}/></div></Route>
              </Switch>
            </HashRouter>
          </div>
        </div>
    );
}
