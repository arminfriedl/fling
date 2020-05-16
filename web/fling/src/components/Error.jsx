import React, {useState} from 'react';
import classNames from 'classnames';

import './Error.scss';

import log from 'loglevel';

export default (props) => {
    function renderError() {
        return (
            <div className="toast toast-error mb-2">
              <button className="btn btn-clear float-right" onClick={props.clearErrors}></button>
              <h5>Ooops!</h5>
              <li>
                { props.errors.map( (err, idx) => <ul key={idx}>{err}</ul> ) }
              </li>
            </div>
        );
    }

    return (
        <>
          { props.errors.length > 0 && !props.below ? renderError() : "" }
          { props.children }
          { props.errors.length > 0 && props.below ? renderError() : "" }
        </>
    );
}
