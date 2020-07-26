/**
 * Utilities for working with JWT tokens
 */
import jwtDecode from 'jwt-decode';

let jwt = {
  /**
   * Check the session store token for an arbitrary claim
   */
  hasClaim: function (name, value) {
    if(!sessionStorage.getItem('token')) return false;
    let tokenPayload = jwtDecode(sessionStorage.getItem('token'));
    return tokenPayload[name] === value;
  },

  /**
   * Check the session store token for a subject
   */
  hasSubject: function (value) {
    if(!sessionStorage.getItem('token')) return false;
    let tokenPayload = jwtDecode(sessionStorage.getItem('token'));
    return tokenPayload['sub'] === value;
  }
};

export default jwt;
