import log from 'loglevel';
import React, { useState } from 'react';
import produce from 'immer';
import _ from 'lodash';

import { useSelector, useDispatch } from 'react-redux';
import { Fling } from '@fling/flingclient';

import { retrieveFlings } from "../../redux/actions";
import { FlingClient } from '../../util/fc';

export default function Settings(props) {
  let flingClient = new FlingClient();
  let dispatch = useDispatch();

  /**
   * The active fling from the redux store. Treat this as immutable.
   */
  let activeFling = useSelector(state => state.flings.activeFling);
  let _clone = _.cloneDeep(_.toPlainObject(_.cloneDeep(activeFling)));

  /**
   * Deep clone the active fling from redux into a draft. Changes to the
   * settings will be stored in the draft until saved and pushed to the
   * backend. This in turn will synchronize back to the redux store.
   *
   * The draft, just as the activeFling, is of type Fling
   */
  let [draft, setDraft] = useState({ fling: _clone });

  let [shareUrlUnique, setShareUrlUnique] = useState(true);
  let [authCodeChangeable, setAuthCodeChangeable] = useState(false);

  /**
   * Publishes the draft to the backend and refreshes the redux store
   */
  function publishDraft() {
    flingClient.putFling(draft).then(
      success => {
        log.info("Saved new settings {}", draft);
        dispatch(retrieveFlings());
      },
      error => log.error("Could not save new settings for {}: {}", activeFling.id, error));
  }

  /**
   * Resets the draft to a new clone of the active fling. All draft
   * modifications get lost.
   */
  function resetDraft() {
    setDraft(produce({}, draft => { return { fling: activeFling }; }));
  }

  /**
   * A helper shim for persistent produce.
   *
   * Executes `fun` in immer.produce, hereby generating a new draft `newDraft`,
   * and sets it into local state via `setDraft(newDraft)`
   */
  let _pproduce = (fun) => (...args) => {
    let x = produce(fun)(...args);
    setDraft(x);
  }

  /**
   * Sets the sharing toggles to valid combinations depending on the changed
   * setting and its new value.
   *
   * Creates a new draft and sets it into the local state.
   */
  let toggleSharing = _pproduce((newDraft, setting, enabled) => {
    switch (setting) {
      case "direct-download":
        if (enabled) {
          newDraft.fling.directDownload = true;
          newDraft.fling.shared = true;
          newDraft.fling.allowUpload = false;
        } else {
          newDraft.fling.directDownload = false;
        }
        return newDraft.fling;
      case "allow-upload":
        if (enabled) {
          newDraft.fling.allowUpload = true;
          newDraft.fling.shared = true;
          newDraft.fling.directDownload = false;
        } else {
          newDraft.fling.allowUpload = false;
        }
        return newDraft.fling;
      case "shared":
        if (enabled) {
          newDraft.fling.allowUpload = false;
          newDraft.fling.directDownload = false;
          newDraft.fling.shared = false;
        } else {
          newDraft.fling.shared = true;
        }
        return newDraft;
      default:
        log.warn("Unknown action");
        break;
    };
  })
  /** Sets the Fling.name. Creates a new draft and sets it into the local state. */
  let setName = produce((newDraft, name) => { newDraft.fling.name = name; return newDraft; });
  /** Sets the Fling.expirationTime. Creates a new draft and sets it into the local state. */
  let setExpirationTime = _pproduce((newDraft, time) => newDraft.fling.expirationTime = time);
  /** Sets the Fling.expirationClicks. Creates a new draft and sets it into the local state. */
  let setExpirationClicks = _pproduce((newDraft, clicks) => newDraft.fling.clicks = clicks);
  /** Sets the Fling.shareId. Creates a new draft and sets it into the local state. */
  let setShareId = _pproduce((newDraft, shareId) => newDraft.fling.shareId = shareId);
  /** Sets the Fling.authCode. Creates a new draft and sets it into the local state. */
  let setAuthCode = _pproduce((newDraft, authCode) => newDraft.fling.authCode = authCode);

  let resetAuthCode = _pproduce((newDraft) => newDraft.fling.authCode = activeFling.authCode);

  return (
    <div className="container">
      <div className="columns">
        <div className="p-centered column col-xl-9 col-sm-12 col-6">
          <form className="form-horizontal" onSubmit={publishDraft}>
            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-name">Name</label>
              </div>
              <div className="col-9 col-sm-12">
                <input className="form-input" type="text" id="input-name"
                  value={draft.fling.name}
                  onChange={(ev) => {
                    ev.preventDefault();
                    let nd = produce(draft, newDraft => {
                      newDraft.fling.name = ev.target.value;
                      return newDraft;
                    })
                    setDraft(nd);
                  }} />
              </div>
            </div>
            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-share-url">Share URL</label>
              </div>
              <div className="col-9 col-sm-12">
                <input className="form-input" type="text" id="input-share-url" onChange={ev => setShareId(ev.target.value)} />
                <i className={`icon icon-cross text-error ${shareUrlUnique ? "d-hide" : "d-visible"}`} />
              </div>
            </div>

            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-passcode">Passcode</label>
              </div>
              <div className="col-9 col-sm-12">
                <div className="input-group">
                  <input className={`form-input ${authCodeChangeable ? "d-visible" : "d-hide"}`} type="text" readOnly={!authCodeChangeable} onChange={ev => setAuthCode(ev.target.value)} />
                  <label className="form-switch ml-2 popover popover-bottom">
                    <input type="checkbox" checked={!!draft.fling.authCode} onChange={resetAuthCode} />
                    <i className="form-icon" /> Protected
                        <div className="popover-container card">
                      <div className="card-body">
                        {draft.fling.authCode ? "Click to reset passcode" : "Set passcode to enable protection"}
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
                  <select className="form-select" >
                    <option value="never">Never</option>
                    <option value="time">Date</option>
                    <option value="clicks">Clicks</option>
                  </select>
                </div>

                <div className={"clicks" === "clicks" ? "d-visible" : "d-hide"}>
                  <div className="input-group">
                    <span className="input-group-addon">Expire after</span>
                    <input className="form-input" type="number" />
                    <span className="input-group-addon">Clicks</span>
                  </div>
                </div>

                <div className={"clicks" === "time" ? "d-visible" : "d-hide"}>
                  <div className="input-group">
                    <span className="input-group-addon">Expire after</span>
                    <input className="form-input" type="date" />
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
                  <input type="checkbox" id="shared" checked={draft.fling.shared} onChange={toggleSharing} />
                  <i className="form-icon" /> Shared
                    </label>
                <label className="form-switch form-inline">
                  <input type="checkbox" id="allow-upload" checked={draft.fling.allowUpload} onChange={toggleSharing} />
                  <i className="form-icon" /> Uploads
                    </label>
                <label className="form-switch form-inline">
                  <input type="checkbox" id="direct-download" checked={draft.fling.directDownload} onChange={toggleSharing} />
                  <i className="form-icon" /> Direct Download
                    </label>
              </div>
            </div>

            <div className="float-right">
              <button className="btn btn-secondary mr-2" onClick={resetDraft}>Cancel</button>
              <input type="submit" className="btn btn-primary" value="Save" />
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
