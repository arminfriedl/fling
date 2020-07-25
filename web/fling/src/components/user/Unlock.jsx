import log from 'loglevel';
import React, { useState, useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';

import { AuthClient, fc } from '../../util/fc';

export default function Unlock() {
  const [authCode, setAuthCode] = useState("");
  const history = useHistory();
  const location = useLocation();
  const { from, shareId } = location.state || { from: { pathname: "/admin" }, shareId: "" };

  useEffect(() => {
    let authClient = new AuthClient();
    let userAuth = new fc.UserAuth(location.state.shareId, "")

    authClient.authenticateUser({ 'userAuth': userAuth })
      .then(response => {
        log.info("Fling is not protected. Logged in successfully.");
        sessionStorage.setItem('token', response);
        history.replace(location.state.from);
      }).catch(error => {
        log.info("Fling protected. Could not unlock without code.")
      });
  }, [location, history]);

  return (
    <div className="container-center">
      <div>
        <div className="column col-12">
          <h1>This Fling is <span className="text-primary">locked.</span></h1>
        </div>
        <div className="column col-12">
          <form id="auth-code-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <input id="auth-code" className="form-input" name="authCode" type="password" placeholder="Enter your code"
                     value={authCode} onChange={ev => setAuthCode(ev.target.value)} />
              <button className="btn btn-primary" type="submit">Unlock</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );

  function handleSubmit(ev) {
    ev.preventDefault();
    let authClient = new AuthClient();
    let userAuth = new fc.UserAuth(shareId, authCode)

    authClient.authenticateUser({ 'userAuth': userAuth })
      .then(response => {
        log.info("Logged in successfully");
        sessionStorage.setItem('token', response);
        history.replace(from);
      }).catch(error => {
        log.error(error);
      });
  };
}
