import { FLING_FILTERS } from "../selectorTypes";

export const flingSelector = (store, flingFilter) => {
    switch(flingFilter) {
        case FLING_FILTERS.ALL:
            return store.flings.flings;
        default:
            return [];
    }
}
