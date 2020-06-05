import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';

import {Switch, Route, Redirect, BrowserRouter, useLocation, useParams, Link} from "react-router-dom";

import {flingClient, artifactClient} from '../../util/flingclient';

import upload from '../resources/upload.svg';
import drop from '../resources/drop.svg';

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

function Upload(props) {
    let fileInputRef = useRef(null);
    let [files, setFiles] = useState([]);
    let [dragging, setDragging] = useState(false);
    let [dragCount, setDragCount] = useState(0);

    useEffect(() => {
        // prevent browser from trying to open the file when drag event
        // not recognized properly
        window.addEventListener("dragover",function(e){
            e.preventDefault();
        },false);
        window.addEventListener("drop",function(e){
            e.preventDefault();
        },false);
    });

    function fileList() {
        function readableBytes(bytes) {
            if(bytes <= 0) return "0 KB";

            var i = Math.floor(Math.log(bytes) / Math.log(1024)),
                sizes = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

            return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
        }

        let fileList = [];
        files.forEach((file,idx) => {
            if(!file.uploaded) {
                fileList.push(
                    <div className="column col-6 col-md-12 mb-2">
                      <div className="card">
                        <div className="card-header">
                          <i className="icon icon-cross float-right c-hand" onClick={ev => deleteFile(idx)}/>
                          <div className="card-title h5">{file.name}</div>
                          <div className="card-subtitle text-gray">{(new Date(file.lastModified)).toLocaleString()+", "+readableBytes(file.size)}</div>
                        </div>
                      </div>
                    </div>
                );
            }
        });

        return fileList;
    }

    function deleteFile(idx) {
        let f = [...files];
        f.splice(idx, 1);
        setFiles(f);
    }

    function totalSize() {
        function readableBytes(bytes) {
            if(bytes <= 0) return "0 KB";

            var i = Math.floor(Math.log(bytes) / Math.log(1024)),
                sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

            return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
        }

        let totalSize = 0;
        for(let file of files) {
            totalSize += file.size;
        }

        return readableBytes(totalSize);
    }

    function handleClick(ev) {
        fileInputRef.current.click();
    }

    function handleFileInputChange(ev) {
        let fileInputFiles = fileInputRef.current.files;
        if (!fileInputFiles) {
            console.warn("No files selected");
            return;
        }

        setFiles([...files, ...fileInputFiles]);
    }

    function handleDrop(ev) {
        stopEvent(ev);
        ev.persist();

        let evFiles = ev.dataTransfer.files;

        if (!evFiles) {
            console.warn("Dropzone triggered without files");
            return;
        }

        setFiles([...files, ...fileListToArray(evFiles)]);
        setDragging(false);
        setDragCount(0);
    }

    function fileListToArray(fileList) {
        if(fileList === undefined || fileList === null) {
            return [];
        }

        let arr = [];
        for (let i=0; i<fileList.length; i++) { arr.push(fileList[i]); }

        return arr;
    }

    function handleOnDragEnter(ev) {
        stopEvent(ev);
        if(dragCount === 0) setDragging(true);

        setDragCount(dragCount+1);
    }

    function handleOnDragLeave(ev) {
        stopEvent(ev);
        let dc = dragCount;

        dc -= 1;
        setDragCount(dc);

        if(dc === 0) setDragging(false);
    }

    function stopEvent(ev) {
        ev.preventDefault();
        ev.stopPropagation();
    }

    function logFiles() {
        log.info("Files so far: ["+files.map((i) => i.name).join(',')+"]");
    }

    function setFileUploaded(idx) {
        let f = [...files];
        f[idx].uploaded = true;
        setFiles(f);
    }

    function handleUpload() {
        let f = [...files];

        files.forEach((file, idx) => {
            artifactClient.postArtifact(props.fling.id, file)
                .then(response => {
                    setFileUploaded(idx);
                });
        });
    }

    function zoneContent(dragging) {
        if(dragging){
            return(
                <>
                  <img className="dropzone-icon" alt="dropzone icon" src={drop} />
                  <h5 className="text-primary">Drop now!</h5>
                </>
            );
        }else {
            return(
                <>
                  <img className="dropzone-icon-upload" alt="dropzone icon" src={upload} />
                  <h5>Click or Drop</h5>
                </>
            );
        }
    }

    return(
        <div className="container">
          {logFiles()}
          <div className="columns">
            <div className="column col-4 col-sm-12">
              <div className="dropzone c-hand py-2"
                   onDrop={handleDrop}
                   onClick={handleClick}
                   onDragOver={stopEvent}
                   onDragEnter={handleOnDragEnter}
                   onDragLeave={handleOnDragLeave}>

                <input className="d-hide" ref={fileInputRef} type="file" multiple onChange={handleFileInputChange} />
                {zoneContent(dragging)}

              </div>
            </div>

            <div className="column col-8 col-sm-12" >
              <div className="file-list">
                <div className="row">
                  <div className="container">
                    <div className="columns">
                      {fileList()}
                    </div>
                  </div>
                </div>
                <div className="upload-command-line m-2">
                  <span className="total-upload">Total Size: {totalSize()}</span>
                  <button className="btn btn-primary btn-upload" onClick={handleUpload}>Upload</button>
                </div>
              </div>
            </div>
          </div>
        </div>
    );
}

export default function FlingUserList(props) {
    let location = useLocation();

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

    function path(tail) {
        if(props.fling && props.fling.sharing) {
            return `/f/${props.fling.sharing.shareUrl}/${tail}`;
        }

        return "";
    }

    return(
        <>
          <div className="container-center">

            <div className="col-6 col-xl-8 col-lg-10 col-md-12">

            <h3 className="ml-2"> {props.fling.name} </h3>
            <div className="ml-2 text-gray">{infoText}</div>

            <div className="card">
              <ul className="tab mx-2">
                <li className={`tab-item ${location.pathname !== path("upload") ? "active": ""}`}>
                  <Link to={path("files")}>Files</Link>
                </li>
                <li className={`tab-item ${location.pathname === path("upload") ? "active": ""}`}>
                  <Link to={path("upload")}>Upload</Link>
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
                <Switch>
                  <Route exact path="/f/:shareId"><Artifacts fling={props.fling} /></Route>
                  <Route path="/f/:shareId/files"><Artifacts fling={props.fling} /></Route>
                  <Route path="/f/:shareId/upload"><Upload fling={props.fling} /></Route>
                </Switch>
              </div>
            </div>

            <div className="d-hide" ref={iframeContainer} />

            </div>
          </div>
        </>
    );
}
