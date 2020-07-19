import log from "loglevel";
import produce from "immer";

import { SET_FLINGS, SET_ACTIVE_FLING, ADD_FLING } from "../actionTypes";

const initialState = {
    // type [fc.Fling]
    flings: [],
    // fc.Fling.id of the currently active fling
    // or null of no fling is active
    activeFling: null
}

export default produce((draft, action) => {
    switch (action.type) {
        case SET_FLINGS:
            draft.flings = action.payload;
            break;
        case ADD_FLING:
            // Check storage again here, otherwise there could be a race
            // condition due to async calls of SET_FLINGS and ADD_FLING
            let foundFlingIdx = draft.flings.findIndex(fling =>
                fling.id === action.payload.id);

            if (foundFlingIdx === -1) {
                log.debug(`Adding new fling with id ${action.payload.id}`)
                draft.flings.push(action.payload);
            } else {
                log.debug(`Fling already exists. ` +
                    `Updating fling with id ${action.payload.id}`)
                draft.flings[foundFlingIdx] = action.payload
            }
            break;
        case SET_ACTIVE_FLING:
            draft.activeFling = action.payload;
            break;
        default:
            break;
    }
    return draft;

}, initialState);
