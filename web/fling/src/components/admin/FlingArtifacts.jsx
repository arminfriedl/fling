import log from 'loglevel';
import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';

import { ArtifactClient, FlingClient, AuthClient } from '../../util/fc';
import { prettifyTimestamp } from '../../util/fn';

function FlingArtifactControl(props) {
  let iframeContainer = useRef(null);
  const artifactClient = new ArtifactClient();
  const authClient = new AuthClient();

  function handleDelete(ev) {
    artifactClient.deleteArtifact(props.artifact.id)
      .then(() => props.reloadArtifactsFn());
  }

  function handleDownload(ev) {
    authClient.deriveToken({ singleUse: true })
      .then(token => {
        // We need this iframe hack because with a regular href, while
        // the browser downloads the file fine, it also reloads the page, hence
        // loosing all logs and state
        let frame = document.createElement("iframe");
        let url = `${window['flingconfig'].API_BASE.replace(/\/+$/, '')}/api/artifacts/${props.artifact.id}/data?derivedToken=${token}`;
        log.trace(`Generated download url: ${url}`);
        frame.src = url;
        iframeContainer.current.appendChild(frame);
      });
  }

  return (
    <div className={`btn-group ${props.hidden ? "d-invisible" : "d-visible"}`}>
      <button className="btn btn-sm" onClick={handleDelete}><i className="icon icon-delete" /></button>
      <button className="btn btn-sm"><i className="icon icon-edit" /></button>
      <button className="btn btn-sm" onClick={handleDownload}><i className="icon icon-download" /></button>
      <div className="d-hide" ref={iframeContainer} />
    </div>
  );
}

function FlingArtifactRow(props) {
  let [hovered, setHovered] = useState(false);

  return (
    <tr key={props.artifact.id} className="artifact-row" onMouseOver={() => setHovered(true)} onMouseOut={() => setHovered(false)}>
      <td>{props.artifact.path}</td>
      <td>{prettifyTimestamp(props.artifact.creationTime, true)}</td>
      <td></td>
      <td><FlingArtifactControl artifact={props.artifact} reloadArtifactsFn={props.reloadArtifactsFn} hidden={!hovered} /></td>
    </tr>
  );
}

function FlingInfo(props) {
  return (
    <div className="m-2">
      { /* Add some infos about the fling */}
    </div>
  );
}

export default function FlingArtifacts() {
  const flingClient = new FlingClient();
  const activeFling = useSelector(state => state.flings.activeFling);
  const [artifacts, setArtifacts] = useState([]);

  useEffect(getArtifacts, [activeFling]);

  return (
    <div>
      <FlingInfo />

      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Uploaded</th>
            <th>Size</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {artifacts}
        </tbody>
      </table>
    </div>
  );

  function getArtifacts() {
    if (!activeFling) {
      log.debug("No fling active. Not getting artifacts.");
      return;
    }

    log.debug(`Fling ${activeFling} active. Getting artifacts.`);
    let artifacts = [];

    flingClient.getArtifacts(activeFling.id)
      .then(result => {
        log.debug(`Got ${result.length} artifacts`);
        for (let artifact of result) {
          if(artifact.archived) {
            artifacts.push(<FlingArtifactRow key={artifact.id} artifact={artifact} reloadArtifactsFn={getArtifacts} />);
          }
        }

        setArtifacts(artifacts);
      });
  }
}
