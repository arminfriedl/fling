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
            return false;
        }
        const { flings: { flings } } = getState();
        const foundFling = flings.find(f => f.id === id);

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
                    log.warn(`Could not find active fling: ${error} \n` +
                        `Resetting active fling`);
                    dispatch(setActiveFlingAction(undefined));
                })
        }
    }
}

function retrieveFlings() {
    return (dispatch, getState) => {
        const { flings: { activeFling } } = getState();
        const flingClient = new FlingClient();

        flingClient.getFlings()
            .then(flings => {
                dispatch(setFlingsAction(flings));
                if (activeFling) {
                    dispatch(setActiveFling(activeFling.id));
                }
            });
    }
}

function deleteFling(id) {
    return (dispatch, getState) => {
        if (!id) {
            log.debug(`Not deleting Fling. No id given.`);
            return;
        }

        const flingClient = new FlingClient();

        flingClient.deleteFling(id)
            .then(() => dispatch(retrieveFlings()))
            .catch(error =>
                log.error(`Could not delete fling ${id}: ${error}`));
    }
}

export { retrieveFlings, setActiveFling, deleteFling };
