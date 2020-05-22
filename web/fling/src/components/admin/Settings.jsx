import log from 'loglevel';
import React, {useState, useEffect, useRef} from 'react';
import {useLocation, useHistory} from 'react-router-dom';

import classNames from 'classnames';

import {flingClient} from '../../util/flingclient';

export default function Settings(props) {
    let defaultState = () => ({name: "", authCode: "",
                               sharing: {directDownload: false, allowUpload: true, shared: true, shareUrl: ""},
                               expiration: {}});

    let [fling, setFling] = useState(defaultState());
    let [shareUrlUnique, setShareUrlUnique] = useState(true);
    let [authCodeChangeable, setAuthCodeChangeable] = useState(false);
    let location = useLocation();
    let history = useHistory();

    useEffect(() => loadSettings(), [props.activeFling]);

    function loadSettings() {
        if(props.activeFling) {
            flingClient.getFling(props.activeFling)
                .then(result => {
                    let f = {...fling, ...result};
                    let s = {...fling.sharing, ...result.sharing};
                    let e = {...fling.expiration, ...result.expiration};

                    f.sharing = s;
                    f.expiration = e;
                    setFling(f);

                    setAuthCodeChangeable(!f.authCode);
                });
        }
    }

    function reloadSettings(ev) {
        ev.preventDefault();
        setFling(defaultState());
        loadSettings();
    }

    function resetAuthCode(ev) {
        if(fling.authCode) {
            let f = {...fling};
            f.authCode = "";
            setFling(f);
        }

        if(!ev.currentTarget.checked) {
            setAuthCodeChangeable(true);
        }
    }

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

    function setName(ev) {
        let f = {...fling};
        let value = ev.currentTarget.value;

        f.name = value;
        setFling(f);
    }

    function setExpirationType(ev) {
        let f = {...fling};
        let e = {...fling.expiration}; //TODO: sharing is not cloned
        let value = ev.currentTarget.value;

        if(value === "never") {
            e = {};
        } else {
            e.type = value;
            e.value = "";
        }

        f.expiration = e;
        setFling(f);
    }

    function setExpirationValue(ev) {
        let f = {...fling};
        let e = {...fling.expiration}; //TODO: sharing is not cloned
        let value = e.type === "time" ? ev.currentTarget.valueAsNumber: ev.currentTarget.value;

        e.value = value;

        f.expiration = e;
        setFling(f);
    }

    function formatExpirationTime() {
        if (!fling.expiration || !fling.expiration.value || fling.expiration.type !== "time")
            return "";


        let date = new Date(fling.expiration.value);
        let fmt = date.toISOString().split("T")[0];
        return fmt;
    }

    function setAuthCode(ev) {
        let f = {...fling};
        let value = ev.currentTarget.value;

        f.authCode = value;
        setFling(f);
    }

    function handleSubmit(ev) {
        ev.preventDefault();
        log.info(fling);
        flingClient.putFling(props.activeFling, fling);
    }

    return(
        <div className="container">
          <div className="columns">
            <div className="p-centered column col-xl-9 col-sm-12 col-6">
              <form className="form-horizontal" onSubmit={handleSubmit}>
                <div className="form-group">
                  <div className="col-3 col-sm-12">
                    <label className="form-label" htmlFor="input-name">Name</label>
                  </div>
                  <div className="col-9 col-sm-12">
                    <input className="form-input" type="text" id="input-name" value={fling.name} onChange={setName}/>
                  </div>
                </div>
                <div className="form-group">
                  <div className="col-3 col-sm-12">
                    <label className="form-label" htmlFor="input-share-url">Share URL</label>
                  </div>
                  <div className="col-9 col-sm-12">
                    <input className="form-input" type="text" id="input-share-url" value={fling.sharing.shareUrl} onChange={setShareUrl} />
                    <i className={`icon icon-cross text-error ${shareUrlUnique ? "d-hide": "d-visible"}`} />
                  </div>
                </div>

                <div className="form-group">
                  <div className="col-3 col-sm-12">
                    <label className="form-label" htmlFor="input-passcode">Passcode</label>
                  </div>
                  <div className="col-9 col-sm-12">
                    <div className="input-group">
                      <input className={`form-input ${authCodeChangeable ? "d-visible":"d-hide"}`} type="text" readOnly={!authCodeChangeable} value={fling.authCode} onChange={setAuthCode} />
                      <label className="form-switch ml-2 popover popover-bottom">
                        <input type="checkbox" checked={!!fling.authCode} onChange={resetAuthCode} />
                        <i className="form-icon" /> Protected
                        <div className="popover-container card">
                          <div className="card-body">
                            {fling.authCode ? "Click to reset passcode": "Set passcode to enable protection"}
                          </div>
                        </div>
                      </label>

                    </div>
                  </div>
                </div>

                <div className="form-group">
                  <div className="col-3 col-sm-12">
                    <label className="form-label">Expiration</label>
                  </div>
                  <div className="col-9 col-sm-12">
                    <div className="form-group">
                      <select className="form-select" value={fling.expiration.type} onChange={setExpirationType}>
                        <option value="never">Never</option>
                        <option value="time">Date</option>
                        <option value="clicks">Clicks</option>
                      </select>
                    </div>

                    <div className={fling.expiration.type === "clicks" ? "d-visible": "d-hide"}>
                      <div className="input-group">
                        <span className="input-group-addon">Expire after</span>
                        <input className="form-input" type="number" value={fling.expiration.value || ""} onChange={setExpirationValue} />
                        <span className="input-group-addon">Clicks</span>
                      </div>
                    </div>

                    <div className={fling.expiration.type === "time" ? "d-visible": "d-hide"}>
                      <div className="input-group">
                        <span className="input-group-addon">Expire after</span>
                        <input className="form-input" type="date" value={formatExpirationTime()} onChange={setExpirationValue} />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="form-group">
                  <div className="col-3 col-sm-12">
                    <label className="form-label">Settings</label>
                  </div>
                  <div className="col-9 col-sm-12">

                    <label className="form-switch form-inline">
                      <input type="checkbox" id="shared" checked={fling.sharing.shared} onChange={toggleSharing}/>
                      <i className="form-icon" /> Shared
                    </label>
                    <label className="form-switch form-inline">
                      <input type="checkbox" id="allow-upload" checked={fling.sharing.allowUpload} onChange={toggleSharing}/>
                      <i className="form-icon" /> Uploads
                    </label>
                    <label className="form-switch form-inline">
                      <input type="checkbox" id="direct-download" checked={fling.sharing.directDownload} onChange={toggleSharing}/>
                      <i className="form-icon" /> Direct Download
                    </label>
                  </div>
                </div>

                <div className="float-right">
                  <button className="btn btn-secondary mr-2" onClick={reloadSettings}>Cancel</button>
                  <input type="submit" className="btn btn-primary" value="Save" />
                </div>
              </form>
            </div>
          </div>
        </div>
    );
}
