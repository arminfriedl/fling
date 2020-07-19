import log from 'loglevel';
import React from 'react';
import { Switch, Route, useLocation, Link } from "react-router-dom";

import { useSelector } from "react-redux";

import FlingArtifacts from './FlingArtifacts';
import Upload from './Upload';
import Settings from './Settings';

export default function FlingContent() {
  const location = useLocation();
  const activeFling = useSelector(state => state.flings.activeFling);

  function Empty() {
    return (
      <div className="empty">
        <div className="empty-icon">
          <i className="icon icon-search icon-2x"></i>
        </div>
        <p className="empty-title h5">No Fling selected</p>
        <p className="empty-subtitle">Select a fling from the list</p>
      </div>
    );
  }

  function Content() {
    function path(tail) {
      return `/admin/${activeFling.id}/${tail}`;
    }

    return (
      <div className="fling-content p-2">
        {log.info("FlingContent location ", location)}
        {log.info("FlingContent active fling ", activeFling)}
        <ul className="tab tab-block mt-0">
          <li className={`tab-item ${location.pathname !== path("upload") && location.pathname !== path("settings") ? "active" : ""}`}>
            <Link to={path("files")}>Files</Link>
          </li>
          <li className={`tab-item ${location.pathname === path("upload") ? "active" : ""}`}>
            <Link to={path("upload")}>Upload</Link>
          </li>
          <li className={`tab-item ${location.pathname === path("settings") ? "active" : ""}`}>
            <Link to={path("settings")}>Settings</Link>
          </li>
        </ul>

        <div className="mt-2">
          <Switch>
            <Route exact path="/admin/:fling"><FlingArtifacts /></Route>
            <Route path="/admin/:fling/files"><FlingArtifacts /></Route>
            <Route path="/admin/:fling/upload"><Upload /></Route>
            <Route path="/admin/:fling/settings"><Settings /></Route>
          </Switch>
        </div>
      </div>
    );
  }

  return (
    <>
      { activeFling ? Content() : Empty() }
    </>
  );
}
