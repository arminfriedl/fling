import log from 'loglevel';
import React from 'react';

import { Switch, Route, Redirect } from "react-router-dom";

import jwt from './util/jwt.js';

import Login from './components/admin/Login';
import FlingAdmin from './components/admin/FlingAdmin';

import Unlock from './components/user/Unlock';
import FlingUser from './components/user/FlingUser';
import LandingPage from './components/LandingPage';

/**
 * Front routes, defaults to a 404 Page.
 * Routes:
 * - / : Landing page
 * - /admin/login : A login page. Redirects with admin token upon successful
     login
 * - /admin : The fling administration page. Redirects to a login page if not
     authenticated
 * - /admin/[fling id]/* : Go directly to a fling (sub-)page. Redirects to a
     login page if not authenticated
 * - /unlock : A unlock page. Redirects with user token upon successful login.
 * - /f/[shareId] : Opens a fling page for a user
 */
export default () => {
  return (
    <Switch>
      <Route exact path="/" component={LandingPage} />

      <Route exact path="/admin/login" component={Login} />
      <OwnerRoute exact path="/admin"><FlingAdmin /></OwnerRoute>
      <OwnerRoute path="/admin/:flingId"><FlingAdmin /></OwnerRoute>

      <Route exact path="/unlock" component={Unlock} />
      <UserRoute path="/f/:shareId"><FlingUser /></UserRoute>

      <Route match="*">Not implemented</Route>
    </Switch>
  );
}

/*
 * A wrapper for <Route> that redirects to the login screen if no admin
 * authentication token was found.
 *
 * Note that the token check is purely client-side. It provides no actual
 * protection! It is hence possible to reach the admin site with some small
 * amount of trickery. Without a valid token no meaningful actions are possible
 * on the admin page though.
 */
function OwnerRoute({ children, ...rest }) {
  log.info(`Routing request for ${rest['path']}`);
  return (
    <Route
      {...rest}
      render={({ location }) => {
        if (jwt.hasSubject("admin")) { return children; }
        else {
          return <Redirect to={{
            pathname: "/admin/login",
            state: { from: location }
          }} />;
        }
      }}
    />
  );
}

/* A wrapper for <Route> that redirects to the unlock screen if no authorized
 * token * was found.
 *
 * Note that the token check is purely client-side. It provides no actual
 * protection! It is hence possible to reach the target site with some small
 * amount of trickery. Without a valid token, no meaningful actions are possible
 * on the target page though - this must be checked server side.
 */
function UserRoute({ children, ...rest }) {
  log.debug(`Routing request for ${rest['path']}`);
  return (
    <Route
      {...rest}
      render={({ match, location }) => {
        let state = { from: location, shareId: match.params.shareId };

        let authorized =
          jwt.hasSubject("admin")
          || ( jwt.hasSubject("user") && jwt.hasClaim("id", state['shareId']) );

        if (authorized) { return children; }
        else { return <Redirect to={{ pathname: "/unlock", state: state }} />; }
      }}
    />
  );
}
