import log from 'loglevel';
import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';

import { ArtifactClient, FlingClient } from '../../util/fc';
import { prettifyTimestamp } from '../../util/fn';

function FlingArtifactControl(props) {
  let iframeContainer = useRef(null);
  const artifactClient = new ArtifactClient();

  function handleDelete(ev) {
    artifactClient.deleteArtifact(props.artifact.id)
      .then(() => props.reloadArtifactsFn());
  }


  function handleDownload(ev) {
    artifactClient.downloadArtifactWithHttpInfo(props.artifact.id)
      .then(response => {
          log.info(response.headers);
          var blob = new Blob([response.data], {type: response.type});
          if(window.navigator.msSaveOrOpenBlob) {
              window.navigator.msSaveBlob(blob, response.name);
          }
          else{
              var elem = window.document.createElement('a');
              elem.href = window.URL.createObjectURL(blob);
              elem.download = response.name;
              document.body.appendChild(elem);
              elem.click();
              document.body.removeChild(elem);
          }
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
          artifacts.push(<FlingArtifactRow key={artifact.id} artifact={artifact} reloadArtifactsFn={getArtifacts} />);
        }

        setArtifacts(artifacts);
      });
  }
}
