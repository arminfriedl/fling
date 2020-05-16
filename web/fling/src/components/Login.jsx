import log from 'loglevel';
import React, {useState, useEffect} from 'react';
import {useHistory, useLocation} from 'react-router-dom';

import request, {setAuth} from '../util/request';

import './Login.scss';

import Error from './Error';

export default () => {
    const [errors, setErrors] = useState([]);
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const history = useHistory();
    const location = useLocation();
    const { from } = location.state || { from: { pathname: "/" } };

    useEffect(() => setAuth(null), []);

    return (
        <div className="container-center">
          <div>
            <Error errors={errors} clearErrors={clearErrors} >
              <form className="container-login" onSubmit={handleSubmit}>
                <div className="form-group">
                  <label className="form-label" htmlFor="username">Username</label>
                  <input className="form-input" id="username" name="username" type="text" placeholder="Username"
                         value={username} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="password">Password</label>
                  <input className="form-input" id="password" name="password" type="password" placeholder={"*".repeat(18)}
                         value={password} onChange={handleChange} />
                </div>
               <div className="form-action-row">
                 <div className="form-group">
                   <label className="form-switch input-sm">
                     <input type="checkbox" />
                     <i className="form-icon" /> Remember me
                   </label>
                 </div>
                 <button className="btn btn-primary" type="submit">Sign In</button>
                </div>
              </form>
            </Error>

            <p className="bottom-text">Ready. Set. Fling.</p>
          </div>

        </div>
    );

    function handleSubmit(ev) {
        ev.preventDefault();

        request.post("/auth/owner", {'username': username, 'password': password})
            .then(response => {
                log.info("Logged in successfully");
                setAuth(response.data);
                history.replace(from);
            })
            .catch(error => {
                log.error(error);
                let response = error.response;
                response.data && response.data.message && setErrors( prev => [response.data.message, ...prev] );
            });
    };

    function handleChange(ev) {
        let name = ev.target.name;
        let val = ev.target.value;

        switch(name) {
        case "username":
            setUsername(val);
            break;
        case "password":
            setPassword(val);
            break;
        }
    };

    function clearErrors() {
        setErrors([]);
    }
}
