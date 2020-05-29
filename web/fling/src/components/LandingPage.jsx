import log from 'loglevel';
import React, {useState} from 'react';

import admin_area from './resources/admin_area.svg';
import view_fling from './resources/view_fling.svg';

export default function LandingPage() {
    let [shareId, setShareId] = useState("");

    function openAdminPage(ev) {
        ev.preventDefault();
        window.location = "/admin";
    }

    function openFling(ev) {
        ev.preventDefault();

        window.location = `/f/${shareId}`;
    }

    function changeShareId(ev) {
        ev.preventDefault();
        setShareId(ev.currentTarget.value);
    }

    return (
        <div className="container-center">
          <div id="landing-rows">
            <div id="landing-header">
              <h1>Welcome !</h1>
              <h2>Where are you heading?</h2>
            </div>

            <div id="landing-content">
              <div id="landing-tile">
                <h5>I am the owner...</h5>
                <img src={admin_area} alt="Admin area" />
                <button className="btn btn-secondary input-group-btn btn-sm mt-2" onClick={openAdminPage}>Manage</button>
              </div>

              <div className="divider-vert" data-content="OR" />

              <div id="landing-tile">
                <h5>I got a code...</h5>
                <img src={view_fling} alt="Fling view" />
                <div className="input-group mt-2">
                  <input type="text" className="form-input input-sm" value={shareId} onChange={changeShareId} placeholder="My code" />
                  <button className="btn btn-secondary input-group-btn btn-sm" onClick={openFling}>Open</button>
                </div>
              </div>
            </div>
          </div>
        </div>
    );
}
