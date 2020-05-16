import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';

import classNames from 'classnames';

import {artifactClient} from '../util/flingclient';

import upload from './upload.svg';
import drop from './drop.svg';
import './Upload.scss';


export default function Upload(props) {
    let fileInputRef = useRef(null);
    let [files, setFiles] = useState([]);
    let [dragging, setDragging] = useState(false);

    useEffect(() => {
        window.addEventListener("dragover",function(e){
            e.preventDefault();
        },false);
        window.addEventListener("drop",function(e){
            e.preventDefault();
        },false);
    });

    function fileList() {
        let fileList = [];

        files.forEach((file,idx) => {
            fileList.push(
                <div className="column col-6 col-md-12 mb-2">
                  <div className="card">
                    <div className="card-header">
                      <i className="icon icon-cross float-right c-hand" onClick={ev => deleteFile(idx)}/>
                      <div className="card-title h5">{file.name}</div>
                      <div className="card-subtitle text-gray">{(new Date(file.lastModified)).toLocaleString()}</div>
                    </div>
                  </div>
                </div>
            );
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
        setDragging(true);
    }

    function handleOnDragLeave(ev) {
        stopEvent(ev);
        setDragging(false);
    }

    function stopEvent(ev) {
        ev.preventDefault();
        ev.stopPropagation();
    }

    function logFiles() {
        log.info("Files so far: ["+files.map((i) => i.name).join(',')+"]");
    }

    function handleUpload() {
        for(let file of files) {
            artifactClient.postArtifact(props.activeFling, file);
        }
    }

    function zoneContent(dragging) {
        if(dragging){
            return(
                <>
                  <img className="dropzone-icon" alt="dropzone icon" src={drop}
                       onDragOver={stopEvent} onDragLeave={stopEvent} />
                  <h5 className="text-primary"
                      onDragOver={stopEvent} onDragLeave={stopEvent}>
                    Drop now!
                  </h5>
                </>
            );
        }else {
            return(
                <>
                  <img className="dropzone-icon-upload" alt="dropzone icon" src={upload}
                       onDragOver={stopEvent} onDragLeave={stopEvent} />
                  <h5 onDragOver={stopEvent} onDragLeave={stopEvent}>
                    Click or Drop
                  </h5>
                </>
            );
        }
    }

    return(
        <div className="container">
          {logFiles()}
          <div className="columns">
            <div className="column col-4 pl-0"
                 onDrop={handleDrop}
                 onClick={handleClick}
                 onDragOver={stopEvent}
                 onDragEnter={handleOnDragEnter}
                 onDragLeave={handleOnDragLeave}>

              <div className="dropzone c-hand py-2">
                <input className="d-hide" ref={fileInputRef} type="file" multiple
                       onDragOver={stopEvent} onDragEnter={stopEvent} onDragLeave={stopEvent}
                       onChange={handleFileInputChange} />
                {zoneContent(dragging)}
              </div>
            </div>

            <div className="column col-8 pr-0" >
              <div className="file-list">
                <div className="row">
                  <div className="container">
                    <div className="columns">
                      {fileList()}
                    </div>
                  </div>
                </div>
                <div className="row">
                  <span className="total-upload">Total Size: {totalSize()}</span>
                  <button className="btn btn-primary btn-upload" onClick={handleUpload}>Upload</button>
                </div>
              </div>
            </div>
          </div>
        </div>
    );
}
