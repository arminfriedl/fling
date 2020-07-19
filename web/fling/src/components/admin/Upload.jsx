import log from 'loglevel';
import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from "react-redux";

import { ArtifactClient, FlingClient, fc } from '../../util/fc';
import { prettifyBytes, prettifyTimestamp } from '../../util/fn';

import upload from '../resources/upload.svg';
import drop from '../resources/drop.svg';

export default function Upload() {
  let fileInputRef = useRef(null);
  let [files, setFiles] = useState([]);
  let [dragging, setDragging] = useState(false);
  let [dragCount, setDragCount] = useState(0);

  const activeFling = useSelector(state => state.flings.activeFling);

  useEffect(() => {
    // prevent browser from trying to open the file when drag event not
    // recognized properly
    window.addEventListener("dragover", e => e.preventDefault(), false);
    window.addEventListener("drop", e => e.preventDefault(), false);
  });

  function fileList() {
    let fileList = [];
    files.forEach((file, idx) => {
      if (!file.uploaded) {
        fileList.push(
            <div key={idx} className="column col-6 col-md-12 mb-2">
            <div className="card">
              <div className="card-header">
                <i className="icon icon-cross float-right c-hand"
                  onClick={ev => deleteFile(idx)} />
                <div className="card-title h5">{file.name}</div>
                <div className="card-subtitle text-gray">
                  {`${prettifyTimestamp(file.lastModified)}, ` +
                    `${prettifyBytes(file.size)}`}
                </div>
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
    let totalSize = 0;
    for (let file of files) {
      totalSize += file.size;
    }

    return prettifyBytes(totalSize);
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
    if (fileList === undefined || fileList === null) {
      return [];
    }

    let arr = [];
    for (let i = 0; i < fileList.length; i++) { arr.push(fileList[i]); }

    return arr;
  }

  function handleOnDragEnter(ev) {
    stopEvent(ev);
    if (dragCount === 0) setDragging(true);

    setDragCount(dragCount + 1);
  }

  function handleOnDragLeave(ev) {
    stopEvent(ev);
    let dc = dragCount;

    dc -= 1;
    setDragCount(dc);

    if (dc === 0) setDragging(false);
  }

  function stopEvent(ev) {
    ev.preventDefault();
    ev.stopPropagation();
  }

  function logFiles() {
    log.info("Files so far: [" + files.map((i) => i.name).join(',') + "]");
  }

  function setFileUploaded(idx) {
    let f = [...files];
    f[idx].uploaded = true;
    setFiles(f);
  }

  function handleUpload() {
    const flingClient = new FlingClient();
    const artifactClient = new ArtifactClient();

    files.forEach((file, idx) => {
      let artifact = new fc.Artifact(file.name)

      flingClient.postArtifact(activeFling.id, { artifact: artifact })
        .then(artifact => {
          artifactClient.uploadArtifactData(artifact.id, { body: file });
          setFileUploaded(idx);
        });
    });
  }

  function zoneContent(dragging) {
    if (dragging) {
      return (
        <>
          <img className="dropzone-icon" alt="dropzone icon" src={drop} />
          <h5 className="text-primary">Drop now!</h5>
        </>
      );
    } else {
      return (
        <>
          <img className="dropzone-icon-upload" alt="dropzone icon" src={upload} />
          <h5>Click or Drop</h5>
        </>
      );
    }
  }

  return (
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
