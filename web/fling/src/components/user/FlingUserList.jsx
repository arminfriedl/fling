import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';

import {useParams, BrowserRouter} from 'react-router-dom';

import {flingClient, artifactClient} from '../../util/flingclient';

function Artifacts(props) {
    let [artifacts, setArtifacts] = useState([]);

    useEffect(() => {
        if(!props.fling) return;

        artifactClient.getArtifacts(props.fling.id)
            .then((artifacts) => setArtifacts(artifacts));
    }, [props.fling]);

    function renderArtifact(artifact) {
        function readableBytes(bytes) {
            if(bytes <= 0) return "0 KB";

            var i = Math.floor(Math.log(bytes) / Math.log(1024)),
                sizes = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

            return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
        }

        function localizedDate(t) {
            let d = new Date(t);
            return d.toLocaleDateString();
        }

        function getArtifactInfo() {
            return `${readableBytes(artifact.size)}, ${localizedDate(artifact.uploadTime)}`;
        }

        return(
            <div className="user-list-artifact">
              <div className="container">
                <div className="columns">
                  <div className="column col-8 col-sm-12">
                    {artifact.name}<br />
                  </div>
                  <div className="column col-2 col-sm-6">
                    <div className="text-gray">{readableBytes(artifact.size)}</div>
                  </div>
                  <div className="column col-2 col-sm-6">
                    <div className="text-gray float-right">{localizedDate(artifact.uploadTime)}</div>
                  </div>
                </div>
              </div>
            </div>
        );
    }

    return (
        <div>
            {artifacts.map(renderArtifact)}
        </div>
    );
}

export default function FlingUserList(props) {
    let iframeContainer = useRef(null);
    let [infoText, setInfoText] = useState("");
    let [inProgress, setInProgress] = useState(false);
    let [downloadUrl, setDownloadUrl] = useState("");

    useEffect(() => flingInfo(props.fling.id), [props.fling.id]);

    function flingInfo(flingId) {
        if(!flingId) return;

        function readableBytes(bytes) {
            if(bytes <= 0) return "0 KB";

            var i = Math.floor(Math.log(bytes) / Math.log(1024)),
                sizes = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

            return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
        }

        function localizedDate(t) {
            let d = new Date(t);
            return d.toLocaleDateString();
        }

        artifactClient.getArtifacts(flingId)
            .then((artifacts) => {
                let totalSize = 0;
                let countArtifacts = 0;

                for(let artifact of artifacts) {
                    totalSize += artifact.size;
                    countArtifacts++;
                }

                setInfoText(`${localizedDate(props.fling.creationTime)} - ${countArtifacts} files - ${readableBytes(totalSize)}`);
            });
    }

    function handleDownload(ev) {
        ev.preventDefault();

        setInProgress(true);

        flingClient.packageFling(props.fling.id)
            .then(downloadUrl => {
                // We need this iframe hack because with a regular href, while
                // the browser downloads the file fine, it also reloads the page, hence
                // loosing all logs and state
                let frame = document.createElement("iframe");
                frame.src = downloadUrl;
                iframeContainer.current.appendChild(frame);
                setDownloadUrl(downloadUrl);
                setInProgress(false);
            });
    }

    return(
        <>

          <div className="container-center">

            <div className="col-6 col-xl-8 col-lg-10 col-md-12">

            <h3 className="ml-2"> {props.fling.name} </h3>
            <div className="ml-2 text-gray">{infoText}</div>

            <div className="card">
              <ul className="tab mx-2">
                <li className="tab-item active">
                  <a>Files</a>
                </li>
                <li className="tab-item">
                  <a>Upload</a>
                </li>

                <li className="tab-item tab-action">
                  <div className="card-title">
                    {inProgress
                     ? <button className="m-2 btn btn-xs btn-secondary float-right user-list-loading" disabled="true"
                               onClick={(ev) => ev.preventDefault()}>
                         <div className="loading" /> Packaging
                       </button>
                     : <button className="m-2 btn btn-xs btn-secondary float-right" onClick={handleDownload}>Download</button>
                    }
                  </div>
                </li>
              </ul>

              <div className="card-body">
                <Artifacts fling={props.fling} />
              </div>
            </div>

            <div className="d-hide" ref={iframeContainer} />

            </div>
          </div>
        </>
    );
}
