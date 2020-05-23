import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';
import {useHistory, useLocation} from 'react-router-dom';

import classNames from 'classnames';

import {artifactClient} from '../../util/flingclient';

function FlingArtifactControl(props) {
    let history = useHistory();
    let iframeContainer = useRef(null);

    function handleDelete(ev) {
        artifactClient.deleteArtifact(props.artifact.id)
            .then(() => props.reloadArtifactsFn());
    }


    function handleDownload(ev) {
        artifactClient.downloadArtifact(props.artifact.id)
            .then(url => {
                // We need this iframe hack because with a regular href, while
                // the browser downloads the file fine, it also reloads the page, hence
                // loosing all logs and state
                let frame = document.createElement("iframe");
                frame.src = url;
                iframeContainer.current.appendChild(frame);
            });
    }

    return(
        <div className={`btn-group ${props.hidden ? "d-invisible": "d-visible"}`}>
          <button className="btn btn-sm" onClick={handleDelete}><i className="icon icon-delete"/></button>
          <button className="btn btn-sm"><i className="icon icon-edit"/></button>
          <button className="btn btn-sm" onClick={handleDownload}><i className="icon icon-download"/></button>
          <div className="d-hide" ref={iframeContainer}/>
        </div>
    );
}

function FlingArtifactRow(props) {
    let [hovered, setHovered] = useState(false);
    function readableBytes(bytes) {
        if(bytes <= 0) return "0 KB";

        var i = Math.floor(Math.log(bytes) / Math.log(1024)),
            sizes = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

        return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
    }

    function localizedUploadDate() {
        let d = new Date(props.artifact.uploadTime);
        return d.toLocaleDateString();
    }

    return(
        <tr key={props.artifact.id} className="artifact-row" onMouseOver={() => setHovered(true)} onMouseOut={() => setHovered(false)}>
          <td>{props.artifact.name}</td>
          <td>{localizedUploadDate()}</td>
          <td>{readableBytes(props.artifact.size)}</td>
          <td><FlingArtifactControl artifact={props.artifact} reloadArtifactsFn={props.reloadArtifactsFn} hidden={!hovered} /></td>
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
                <th>Uploaded</th>
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
                    artifacts.push(<FlingArtifactRow key={artifact.id} artifact={artifact} reloadArtifactsFn={getArtifacts} />);
                }

                setArtifacts(artifacts);
            });
    }
}
