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
            draft.flings.push(action.payload);
            break;
        case SET_ACTIVE_FLING:
            draft.activeFling = action.payload;
            break;
        default:
            break;
    }
    return draft;

}, initialState);
