import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';

import classNames from 'classnames';

import {flingClient} from '../util/flingclient';

export default function Settings(props) {
    let [fling, setFling] = useState({name: "", sharing: {directDownload: false, allowUpload: true, shared: true, shareUrl: ""}});
    let [shareUrlUnique, setShareUrlUnique] = useState(true);

    useEffect(() => {
        if(props.activeFling) {
            flingClient.getFling(props.activeFling)
                .then(result => {
                    setFling(result);
                });
        }
    }, [props.activeFling]);

    function toggleSharing(ev) {
        let f = {...fling};
        let s = {...fling.sharing};

        if(ev.currentTarget.id === "direct-download") {
            if(ev.currentTarget.checked) {
                s.directDownload = true;
                s.shared = true;
                s.allowUpload = false;
            } else {
                s.directDownload = false;
            }
        } else if(ev.currentTarget.id === "allow-upload") {
            if(ev.currentTarget.checked) {
                s.allowUpload = true;
                s.shared = true;
                s.directDownload = false;
            } else {
                s.allowUpload = false;
            }
        } else if(ev.currentTarget.id === "shared") {
            if(!ev.currentTarget.checked) {
                s.allowUpload = s.directDownload = s.shared = false;
            } else {
                s.shared = true;
            }
        }

        f.sharing = s;

        setFling(f);
    }

    function setShareUrl(ev) {
        let f = {...fling};
        let s = {...fling.sharing}; //TODO: expiration is not cloned
        let value = ev.currentTarget.value;

        if(!value) {
            setShareUrlUnique(false);
            s.shareUrl = value;
            f.sharing = s;
            setFling(f);
            return;
        }

        flingClient.getFlingByShareId(ev.currentTarget.value)
            .then(result => {
                if(!result) {
                    setShareUrlUnique(true);
                } else if(props.activeFling === result.id) { // share url didn't change
                    setShareUrlUnique(true);
                } else {
                    setShareUrlUnique(false);
                }

                s.shareUrl = value;
                f.sharing = s;
                setFling(f);
            });
    }

    function handleSubmit(ev) {
        ev.preventDefault();
        log.info(fling);
        flingClient.putFling(props.activeFling, fling);
    }

    return(
        <div className="container">
          {log.info(props)}
          <div className="columns">
            <div className="column col-6">
              <form onSubmit={handleSubmit}>
                <div className="form-group">
                  <label className="form-label" htmlFor="input-name">Name</label>
                  <input className="form-input" type="text" id="input-name" value={fling.name} />

                  <label className="form-label" htmlFor="input-share-url">Share</label>
                  <input className="form-input" type="text" id="input-share-url" value={fling.sharing.shareUrl} onChange={setShareUrl} />
                  <i className={`icon icon-cross text-error ${shareUrlUnique ? "d-hide": "d-visible"}`} />
                </div>

                <div className="form-group">
                  <label className="form-switch form-inline">
                    <input type="checkbox" id="direct-download" checked={fling.sharing.directDownload} onChange={toggleSharing}/>
                    <i className="form-icon" /> Direct Download
                  </label>

                  <label className="form-switch form-inline">
                    <input type="checkbox" id="allow-upload" checked={fling.sharing.allowUpload} onChange={toggleSharing}/>
                    <i className="form-icon" /> Allow Uploads
                  </label>

                  <label className="form-switch form-inline">
                    <input type="checkbox" id="shared" checked={fling.sharing.shared} onChange={toggleSharing}/>
                    <i className="form-icon" /> Shared
                  </label>
                </div>

                <input type="submit" className="btn float-right" value="Save" />
              </form>
            </div>
          </div>
        </div>
    );
}
