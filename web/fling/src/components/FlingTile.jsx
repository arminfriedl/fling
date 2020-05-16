import log from 'loglevel';
import React, {useRef, useState} from 'react';
import classNames from 'classnames';

import {flingClient} from '../util/flingclient';

import './FlingTile.scss';

function TileAction(props) {
    let shareUrlRef = useRef(null);

    return(
        <div className="tile-action dropdown">
          <a className="btn btn-link btn dropdown-toggle" tabIndex="0">
            <i className="icon icon-more-vert" />
          </a>
          <ul className="menu text-left">
            <li className="menu-item input-group">
              <div className="input-group">
                <input type="text" ref={shareUrlRef} className="form-input input-sm input-share-id" readOnly value={props.fling.sharing.shareUrl} />
                <span className="input-group-addon addon-sm input-group-addon-sm" onClick={copyShareUrl} ><i className="icon icon-copy" /></span>
              </div>
            </li>
            <li className="menu-item">
              <div className="form-group">
                <label className="form-switch">
                  <input type="checkbox" checked={props.fling.sharing.shared} onChange={toggleShared} />
                  <i className="form-icon" />
                  {props.fling.sharing.shared ? "Shared":"Private"}
                </label>
              </div>
            </li>
            <li className="menu-item">
              <a href="#" className="text-warning" onClick={deleteFling}>
                <i className="icon icon-delete mr-1" /> Remove
              </a>
            </li>
          </ul>
        </div>
    );

    function copyShareUrl() {
        shareUrlRef.current.focus();
        shareUrlRef.current.select();

        try {
            let successful = document.execCommand('copy');
            let msg = successful ? 'successful' : 'unsuccessful';
            console.log('Copying to clipoard ' + msg);
        } catch (err) {
            log.error("Couldn't copy to clipboard: ", err);
        }
    }

    async function deleteFling() {
        await flingClient.deleteFling(props.fling.id);
        await props.refreshFlingListFn();
    }

    async function toggleShared() {
        await flingClient.putFling(props.fling.id, {"sharing": {"shared": !props.fling.sharing.shared}});
        await props.refreshFlingListFn();
    }
}

export default function FlingTile(props) {
    let tileClasses = classNames(
        "tile", "tile-centered", "p-2", "c-hand",
        {"active": props.activeFling === props.fling.id}
    );

    return (
        <div>
          <div className={tileClasses}>
            <div className="tile-content" onClick={() => activateTile(props.fling.id)}>
              <div className="tile-title">{props.fling.name}</div>
              <small className="tile-subtitle text-gray">14MB · Public · 1 Jan, 2017</small>
            </div>
            <TileAction fling={props.fling} refreshFlingListFn={props.refreshFlingListFn} />
          </div>
        </div>
    );

    function activateTile() {
        log.debug(`Activating fling ${props.fling.id}`);
        props.setActiveFlingFn(props.fling.id);
    }
}
