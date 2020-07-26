import log from 'loglevel';
import React, { useRef, useState, useEffect } from 'react';

import { AuthClient } from '../../util/fc';

export default function FlingUser(props) {
  let iframeContainer = useRef(null);
  let [packaging, setPackaging] = useState(true);
  let [done, setDone] = useState(false);
  let [waitingMessage, setWaitingMessage] = useState("");
  let [downloadUrl, setDownloadUrl] = useState("");

  useEffect(handleDownload, []);

  function handleDownload() {
    let authClient = new AuthClient();
    authClient.deriveToken({ singleUse: true })
      .then(token => {
        let url = `${process.env.REACT_APP_API.replace(/\/+$/, '')}/api/fling/${props.fling.id}/data?derivedToken=${token}`;
        log.trace(`Generated download url for link: ${url}`);
        setDownloadUrl(url);
      })
      .then(
        authClient.deriveToken({ singleUse: true })
          .then(token => {
            setPackaging(false);
            // We need this iframe hack because with a regular href, while
            // the browser downloads the file fine, it also reloads the page, hence
            // loosing all logs and state
            let frame = document.createElement("iframe");
            let url = `${process.env.REACT_APP_API.replace(/\/+$/, '')}/api/fling/${props.fling.id}/data?derivedToken=${token}`;
            log.trace(`Generated download url: ${url}`);
            frame.src = url;
            iframeContainer.current.appendChild(frame);
          }));

    let randMsg = ["Please stay patient...",
      "Your download will be ready soon...",
      "Packaging up your files...",
      "Almost there..."];
    setInterval(() => setWaitingMessage(randMsg[Math.floor(Math.random() * randMsg.length)]), 10000);
  }

  function invalidateLink(ev) {
    setDone(true);
    window.location.href = downloadUrl;
  }

  function reloadPage(ev) {
    window.location.reload();
  }

  return (
    <div>
      <div className="container-center">
        <div className="card direct-download-card">
          <div className="card-body ">
            {packaging
              ? <><div className="loading loading-lg" />
                {waitingMessage ? waitingMessage : "Packaging up your files..."}
              </>
              : !done
                ? <>
                  <h5>Your download is <span className="text-primary">ready!</span></h5>
                  <i className="icon icon-check icon-2x text-primary" /><br />
                  <span className="text-dark">Download doesn't start? <br />
                    <button className="btn btn-link" onClick={invalidateLink}>Click here</button></span>
                </>
                : <>
                  <h5>Thanks for <span className="text-primary">downloading!</span></h5>
                  <i className="icon icon-check icon-2x text-primary" /><br />
                  <span className="text-dark">Want to download again? <br />
                    <button className="btn btn-link" onClick={reloadPage}>Reload page</button></span>
                </>
            }
          </div>
        </div>
      </div>
      <div className="d-hide" ref={iframeContainer} />
    </div>
  );
}
