import log from 'loglevel';

import { SET_FLINGS, SET_ACTIVE_FLING, ADD_FLING } from "./actionTypes";
import { FlingClient } from "../util/fc";

function setFlingsAction(flings) {
    return {
        type: SET_FLINGS,
        payload: flings
    }
}

function addFlingAction(fling) {
    return {
        type: ADD_FLING,
        payload: fling
    }
}

function setActiveFlingAction(fling) {
    return {
        type: SET_ACTIVE_FLING,
        payload: fling
    }
}


function setActiveFling(id) {
    return (dispatch, getState) => {
        if (!id) {
            log.debug(`Not setting active Fling. No id given.`);
            return;
        }
        const { flings: { flings } } = getState();
        let foundFling = flings.find(f => f.id === id);

        if (foundFling) {
            log.info(`Found active fling ${id} in local storage`);
            dispatch(setActiveFlingAction(foundFling));
        } else {
            log.info(`Active fling ${id} not found in local storage. ` +
                `Trying to retrieve from remote.`);

            let flingClient = new FlingClient();
            flingClient.getFling(id)
                .then(fling => {
                    dispatch(addFlingAction(fling));
                    dispatch(setActiveFlingAction(fling))
                })
                .catch(error => {
                    log.warn(`Could not find active fling. ` +
                        `Resetting active fling`);
                    dispatch(setActiveFlingAction(undefined));
                })
        }
    }
}

function retrieveFlings() {
    return (dispatch) => {
        let flingClient = new FlingClient();
        flingClient.getFlings()
            .then(flings => dispatch(setFlingsAction(flings)));
    }
}

export { retrieveFlings, setActiveFling };
