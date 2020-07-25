import log from 'loglevel';
import React, { useState } from 'react';
import produce from 'immer';

import { useSelector, useDispatch } from 'react-redux';

import { retrieveFlings } from "../../redux/actions";
import { FlingClient } from '../../util/fc';

export default function Settings(props) {
  let flingClient = new FlingClient();
  let dispatch = useDispatch();

  /**
   * The active fling from the redux store. Treat this as immutable.
   */
  let activeFling = useSelector(state => state.flings.activeFling);

  /**
   * Deep clone the active fling from redux into a draft. Changes to the
   * settings will be stored in the draft until saved and pushed to the
   * backend. This in turn will synchronize back to the redux store.
   *
   * The draft, just as the activeFling, is of type Fling
   */
  let [draft, setDraft] = useState(produce(activeFling, draft => draft));

  let [shareUrlUnique, setShareUrlUnique] = useState(true);
  let [authCodeChangeable, setAuthCodeChangeable] = useState(false);
  let [expirationType, setExpirationType] = useState(activeFling.expirationClicks
    ? "clicks"
    : activeFling.expirationTime ? "time" : "never");

  /**
   * Publishes the draft to the backend and refreshes the redux store
   */
  function publishDraft() {
    flingClient.putFling(activeFling.id, { fling: draft })
      .then(
        success => {
          log.info("Saved new settings {}", draft);
          dispatch(retrieveFlings());
        })
      .catch(error => log.error(`Could not save new settings for ${activeFling.id}: `, error));
  }

  /**
   * Resets the draft to a new clone of the active fling. All draft
   * modifications get lost.
   */
  function resetDraft() {
    setDraft(produce({}, draft => activeFling));
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
          newDraft.directDownload = true;
          newDraft.shared = true;
          newDraft.allowUpload = false;
        } else {
          newDraft.directDownload = false;
        }
        return newDraft;
      case "allow-upload":
        if (enabled) {
          newDraft.allowUpload = true;
          newDraft.shared = true;
          newDraft.directDownload = false;
        } else {
          newDraft.allowUpload = false;
        }
        return newDraft;
      case "shared":
        if (!enabled) {
          newDraft.allowUpload = false;
          newDraft.directDownload = false;
          newDraft.shared = false;
        } else {
          newDraft.shared = true;
        }
        return newDraft;
      default:
        log.warn("Unknown action");
        break;
    };
  })
  /** Sets the Fling.name. Creates a new draft and sets it into the local state. */
  let setName = _pproduce((newDraft, name) => { newDraft.name = name });
  /** Sets the Fling.shareId. Creates a new draft and sets it into the local
   *  state. Sets `setShareUrlUnique`. */
  let setShareId = _pproduce((newDraft, shareId) => {
    newDraft.shareId = shareId;

    flingClient.getFlingByShareId(shareId)
      .then(result => shareId !== activeFling.shareId && setShareUrlUnique(false))
      .catch(error => error.status === 404 && setShareUrlUnique(true));
  });
  /** Sets the Fling.authCode. Creates a new draft and sets it into the local state. */
  let setAuthCode = _pproduce((newDraft, authCode) => {
    setAuthCodeChangeable(true);
    if (!authCode) return newDraft;
    newDraft.authCode = authCode
  });

  let resetAuthCode = _pproduce((newDraft) => {
    setAuthCodeChangeable(true);
    newDraft.authCode = "";
    return newDraft;
  });

  let setExpiration = _pproduce((newDraft, type, value) => {
    setExpirationType(type)
    switch (type) {
      case "clicks":
        newDraft.expirationTime = "";
        newDraft.expirationClicks = value;
        break;
      case "time":
        newDraft.expirationClicks = "";
        newDraft.expirationTime = value;
        break;
      case "never":
        newDraft.expirationClicks = "";
        newDraft.expirationTime = "";
        break;
      default:
        log.error("Unknown expiration type");
        break;
    }
  });


  let resetExpiration = (draft, type) => {
    setExpiration(draft, type, "");
  };

  return (
    <div className="container">
      <div className="columns">
        <div className="p-centered column col-xl-9 col-sm-12 col-6">
          <form className="form-horizontal" onSubmit={(ev) => { ev.preventDefault(); publishDraft(); }}>
            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-name">Name</label>
              </div>
              <div className="col-9 col-sm-12">
                <input className="form-input" type="text" id="input-name"
                  value={draft.name}
                  onChange={(ev) => setName(draft, ev.target.value)} />
              </div>
            </div>
            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-share-url">Share Id</label>
              </div>
              <div className="col-9 col-sm-12">
                <input className="form-input" type="text" id="input-share-url"
                  value={draft.shareId}
                  onChange={ev => setShareId(draft, ev.target.value)} />
                <i className={`icon icon-cross text-error ${shareUrlUnique ? "d-hide" : "d-visible"}`} />
              </div>
            </div>

            <div className="form-group">
              <div className="col-3 col-sm-12">
                <label className="form-label" htmlFor="input-passcode">Passcode</label>
              </div>
              <div className="col-9 col-sm-12">
                <div className="input-group">
                  <input className={`form-input ${!draft.authCode || authCodeChangeable ? "d-visible" : "d-hide"}`} type="text"
                    value={draft.authCode}
                    onChange={ev => setAuthCode(draft, ev.target.value)} />

                  <label className="form-switch ml-2 popover popover-bottom">
                    <input type="checkbox" checked={!!draft.authCode} onChange={ev => resetAuthCode(draft)} />
                    <i className="form-icon" /> Protected
                        <div className="popover-container card">
                      <div className="card-body">
                        {draft.authCode ? "Click to reset passcode" : "Set passcode to enable protection"}
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
                  <select className="form-select" value={expirationType} onChange={ev => resetExpiration(draft, ev.currentTarget.value)}>
                    <option value="never">Never</option>
                    <option value="time">Date</option>
                    <option value="clicks">Clicks</option>
                  </select>
                </div>

                <div className={expirationType === "clicks" ? "d-visible" : "d-hide"}>
                  <div className="input-group">
                    <span className="input-group-addon">Expire after</span>
                    <input className="form-input" type="number" value={draft.expirationClicks} onChange={ev => setExpiration(draft, "clicks", ev.target.value)} />
                    <span className="input-group-addon">Clicks</span>
                  </div>
                </div>

                <div className={expirationType === "time" ? "d-visible" : "d-hide"}>
                  <div className="input-group">
                    <span className="input-group-addon">Expire after</span>
                    <input className="form-input" type="date"
                           value={draft.expirationTime ? (new Date(draft.expirationTime)).toISOString().split('T')[0]: ""}
                           onChange={ev => setExpiration(draft, "time", ev.target.valueAsNumber)} />
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
                  <input type="checkbox" id="shared" checked={draft.shared}
                    onChange={ev => toggleSharing(draft, ev.target.id, ev.target.checked)} />
                  <i className="form-icon" /> Shared
                    </label>
                <label className="form-switch form-inline">
                  <input type="checkbox" id="allow-upload" checked={draft.allowUpload}
                    onChange={ev => toggleSharing(draft, ev.target.id, ev.target.checked)} />
                  <i className="form-icon" /> Uploads
                    </label>
                <label className="form-switch form-inline">
                  <input type="checkbox" id="direct-download" checked={draft.directDownload}
                    onChange={ev => toggleSharing(draft, ev.target.id, ev.target.checked)} />
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
