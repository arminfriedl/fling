import log from 'loglevel';
import React, {useState, useEffect} from 'react';
import {useHistory, useLocation} from 'react-router-dom';

import request, {setAuth} from '../../util/request';

export default function Unlock() {
    const [authCode, setAuthCode] = useState("");
    const history = useHistory();
    const location = useLocation();
    const { from, shareId } = location.state || { from: { pathname: "/admin" }, shareId: "" };

    useEffect(() => setAuth(null), []);

    useEffect(() => {
        request.post("/auth/user", {"shareId": location.state.shareId})
            .then(response => {
                log.info("Fling is not protected. Logged in successfully.");
                setAuth(response.data);
                history.replace(location.state.from);
            })
            .catch(err => {/* ignored */});
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
                  <input id="auth-code" className="form-input" name="authCode" type="password" placeholder="Enter your code" value={authCode} onChange={handleChange} />
                  <button className="btn btn-primary" type="submit">Unlock</button>
                </div>
              </form>
            </div>
          </div>
        </div>
    );

    function handleSubmit(ev) {
        ev.preventDefault();

        request.post("/auth/user", {"shareId": shareId, "code": authCode})
            .then(response => {
                log.info("Logged in successfully");
                setAuth(response.data);
                history.replace(from);
            })
            .catch(error => {
                log.error(error);
            });
    };

    function handleChange(ev) {
        let val = ev.target.value;
        setAuthCode(val);
    };
}
