import React, {useState} from 'react';

import New from './New';
import send from '../resources/send.svg';

export default function Navbar() {
    let [newModalActive, setNewModalActive] = useState(false);

    function handleOnClick(ev) {
        setNewModalActive(true);
    }

    function closeNewModal() {
        setNewModalActive(false);
    }

    return (
        <>
          <header className="navbar">
            <section className="navbar-section">
              <a href="/admin" className="navbar-brand">
                <img src={send} alt="Fling logo"/>
                Fling
              </a>
            </section>
            <section className="navbar-center">
              <div className="input-group input-inline">
                <input className="form-input input-sm" type="text" placeholder="Search" />
                <button className="btn btn-sm btn-link input-group-btn"><i className="icon icon-search"/></button>
              </div>
            </section>
            <section className="navbar-section navbar-control">
              <button className="btn btn-sm btn-link" onClick={handleOnClick}><i className="icon icon-plus"/> New</button>
              <a className="btn btn-sm btn-link" href="/admin/login"><i className="icon icon-shutdown"/> Logout</a>
            </section>
          </header>

          <New active={newModalActive} closeModalFn={closeNewModal} />
        </>
    );
}
