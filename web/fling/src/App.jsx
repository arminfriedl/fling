import log from 'loglevel';
import React from 'react';

import {Switch, Route, Redirect} from "react-router-dom";

import request, {isOwner, isUser} from './util/request';

import Login from './components/admin/Login';
import FlingAdmin from './components/admin/FlingAdmin';

import Unlock from './components/user/Unlock';
import FlingUser from './components/user/FlingUser';

export default () => {
    return (
        <Switch>
          <Route exact path="/admin/login" component={Login} />
          <OwnerRoute exact path="/admin"><FlingAdmin /></OwnerRoute>
          <OwnerRoute path="/admin/:fling"><FlingAdmin /></OwnerRoute>

          <Route exact path="/unlock" component={Unlock} />
          <UserRoute exact path="/f/:shareId"><FlingUser /></UserRoute>
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

// A wrapper for <Route> that redirects to the unlock
// screen if the fling is protected
function UserRoute({ children, ...rest }) {
    return (
        <Route
          {...rest}
          render={({ match, location }) => {
              log.info(request.defaults);
              log.info(match);
              log.info(location);
              let x = {from: location, shareId: match.params.shareId};

              if(isOwner()) { return children; }
              else if(isUser(match.params.shareId)) { return children; }
              else { return <Redirect to={ {pathname: "/unlock", state: x} } />; }
          }}
        />
    );
}
