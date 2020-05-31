import log from 'loglevel';
import React, {useRef, useState, useEffect} from 'react';

import {flingClient} from '../../util/flingclient';

export default function FlingUser(props) {
    let iframeContainer = useRef(null);
    let [packaging, setPackaging] = useState(true);
    let [waitingMessage, setWaitingMessage] = useState("");
    let [downloadUrl, setDownloadUrl] = useState("");

    useEffect(handleDownload, []);

    function handleDownload() {
        flingClient.packageFling(props.fling.id)
            .then(downloadUrl => {
                setPackaging(false);
                // We need this iframe hack because with a regular href, while
                // the browser downloads the file fine, it also reloads the page, hence
                // loosing all logs and state
                let frame = document.createElement("iframe");
                frame.src = downloadUrl;
                iframeContainer.current.appendChild(frame);
                setDownloadUrl(downloadUrl);
            });

        let randMsg = ["Please stay patient...",
                       "Your download will be ready soon...",
                       "Packaging up your files...",
                       "Almost there..."];
        setInterval(() => setWaitingMessage(randMsg[Math.floor(Math.random() * randMsg.length)]), 10000);
    }

    return(
        <div>
          <div className="container-center">
            <div className="card direct-download-card">
              <div className="card-body ">
                {packaging
                 ? <><div className="loading loading-lg" />
                     {waitingMessage ? waitingMessage: "Packaging up your files..."}
                   </>
                 : <>
                     <h5>Your download is <span className="text-primary">ready!</span></h5>
                     <i className="icon icon-check icon-2x text-primary" /><br/>
                     <span className="text-dark">Download doesn't start? <br/><a href={downloadUrl}>Click here</a></span>
                   </>
                }
              </div>
            </div>
          </div>
          <div className="d-hide" ref={iframeContainer} />
        </div>
    );
}
