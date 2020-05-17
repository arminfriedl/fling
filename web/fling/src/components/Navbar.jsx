import log from 'loglevel';
import React from 'react';

import request from '../util/request';

import send from './send.svg';

export default function Navbar() {
    return (
        <header className="navbar">
          <section className="navbar-section">
            <a href="/" className="navbar-brand">
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
            <button className="btn btn-sm btn-link"><i className="icon icon-plus"/> New</button>
            <a className="btn btn-sm btn-link" href="/login"><i className="icon icon-shutdown"/> Logout</a>
          </section>
        </header>
    );
}
