import log from 'loglevel';
import React, {useState, useEffect} from 'react';
import {useHistory, useLocation} from 'react-router-dom';

import {fc, AuthClient} from '../../util/fc';

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const history = useHistory();
  const location = useLocation();
  const { from } = location.state || { from: { pathname: "/admin" } };

  useEffect(() => {
    sessionStorage.removeItem("token")
  });

  function handleSubmit(ev) {
    ev.preventDefault();

    let authClient = new AuthClient();
    let opt = {adminAuth: new fc.AdminAuth(username, password)};

    authClient.authenticateOwner(opt)
      .then(response => {
        log.info("Login successful");
        sessionStorage.setItem('token', response);
        log.debug("Returning back to", from);
        history.replace(from);
      }).catch(log.error);
  };

  return (
    <div className="container-center">
      <div>
        <form className="login-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">Username</label>
            <input className="form-input" id="username" name="username" type="text" placeholder="Username"
                   value={username} onChange={ev => setUsername(ev.currentTarget.value)} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input className="form-input" id="password" name="password" type="password" placeholder={"*".repeat(18)}
                   value={password} onChange={ev => setPassword(ev.currentTarget.value)} />
          </div>
          <div className="login-action-row">
            <div className="form-group">
              <label className="form-switch input-sm">
                <input type="checkbox" />
                <i className="form-icon" /> Remember me
              </label>
            </div>
            <button className="btn btn-primary" type="submit">Sign In</button>
          </div>
        </form>

        <p className="login-footer">Ready. Set. Fling.</p>
      </div>

    </div>
  );
}
