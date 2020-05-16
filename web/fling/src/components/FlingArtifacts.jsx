import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';

import classNames from 'classnames';

import {artifactClient} from '../util/flingclient';

import './FlingArtifacts.scss';

function FlingArtifactControl(props) {
    return(
        <div className={`btn-group ${props.hidden ? "d-invisible": "d-visible"}`}>
          <button className="btn btn-sm"><i className="icon icon-delete"/></button>
          <button className="btn btn-sm"><i className="icon icon-edit"/></button>
          <button className="btn btn-sm"><i className="icon icon-download"/></button>
        </div>
    );
}

function FlingArtifactRow(props) {
    let [hovered, setHovered] = useState(false);

    return(
        <tr key={props.artifact.id} className="artifact-row" onMouseOver={() => setHovered(true)} onMouseOut={() => setHovered(false)}>
          <td>{props.artifact.name}</td>
          <td>{props.artifact.version}</td>
          <td/>
          <td><FlingArtifactControl hidden={!hovered} /></td>
        </tr>
    );
}

export default function FlingArtifacts(props) {
    const [artifacts, setArtifacts] = useState([]);
    useEffect(getArtifacts, [props.activeFling]);

    return (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Date</th>
                <th>Size</th>
                <th/>
              </tr>
            </thead>
            <tbody>
              {artifacts}
            </tbody>
          </table>
    );

    function getArtifacts() {
        if (!props.activeFling) {
            log.debug("No fling active. Not getting artifacts.");
            return;
        }

        log.debug(`Fling ${props.activeFling} active. Getting artifacts.`);
        let artifacts = [];

        artifactClient.getArtifacts(props.activeFling)
            .then(result => {
                log.debug(`Got ${result.length} artifacts`);
                for(let artifact of result) {
                    artifacts.push(<FlingArtifactRow key={artifact.id} artifact={artifact} />);
                }

                setArtifacts(artifacts);
            });
    }
}
