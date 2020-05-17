import log from 'loglevel';
import React from 'react';

import {Switch, Route, Redirect} from "react-router-dom";

import request, {isOwner} from './util/request';

import Login from './components/admin/Login';
import Fling from './components/admin/Fling';

export default () => {
    return (
        <Switch>
          <Route exact path="/admin/login" component={Login} />
          <OwnerRoute exact path="/admin"><Fling /></OwnerRoute>
          <OwnerRoute path="/admin/:fling"><Fling /></OwnerRoute>
          <Route match="*">Not implemented</Route>
        </Switch>
    );
}

// A wrapper for <Route> that redirects to the login
// screen if you're not yet authenticated.
function OwnerRoute({ children, ...rest }) {
    return (
        <Route
          {...rest}
          render={({ location }) => {
              log.info(request.defaults);
              if(isOwner()) { return children; }
              else { return <Redirect to={{pathname: "/admin/login", state: {from: location}}} />; }
          }}
        />
    );
}
