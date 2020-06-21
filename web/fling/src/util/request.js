import axios from 'axios';
import jwtDecode from 'jwt-decode';

function Request() {
    let request = axios.create({baseURL: process.env.REACT_APP_API});
    if(sessionStorage.getItem('token')) {
        request.defaults.headers = {'Authorization': `Bearer ${sessionStorage.getItem('token')}`};
    }

    return request;
}

function hasClaim(name, value) {
    if(!sessionStorage.getItem('token')) return false;
    let tokenPayload = jwtDecode(sessionStorage.getItem('token'));
    return tokenPayload[name] === value;
}

function setAuth(token) {
    if(token == null){ // reset the auth
        sessionStorage.removeItem('token');
        request.defaults.headers = {};
        return;
    }

    sessionStorage.setItem('token', token);
    request.defaults.headers = {'Authorization': 'Bearer '+sessionStorage.getItem('token')};
}

let request = new Request();
let isOwner = () => hasClaim("sub", "owner");
let isUser = (shareId) => hasClaim("sub", "user") && hasClaim("sid", shareId);

export {isOwner, isUser, setAuth};
export default request;
