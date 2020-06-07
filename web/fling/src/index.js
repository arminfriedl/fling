import 'core-js';

import React from 'react';
import ReactDOM from 'react-dom';

import log from 'loglevel';

import { Provider } from 'react-redux';
import { createStore } from 'redux';
import reducer from './redux/reducer';

import { BrowserRouter } from "react-router-dom";

import App from './App';

import "./style/fling.scss";

import * as serviceWorker from './serviceWorker';

/* Logging Setup */
log.setDefaultLevel(log.levels.TRACE);
if(process.env.REACT_APP_LOGLEVEL) {
    log.setLevel(process.env.REACT_APP_LOGLEVEL);
}

/* Store setup */
let store = createStore(reducer,
                        (window.__REDUX_DEVTOOLS_EXTENSION__
                         && window.__REDUX_DEVTOOLS_EXTENSION__()));

/* Fling App Setup */
ReactDOM.render(
    <Provider store={store}>
      <BrowserRouter>
        <React.StrictMode>
          <App />
        </React.StrictMode>
      </BrowserRouter>
    </Provider>,
  document.getElementById('root')
);

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
