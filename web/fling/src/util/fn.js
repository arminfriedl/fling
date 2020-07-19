/*
 * Returns a human readable presentation of `bytes`
 */
export function prettifyBytes(bytes) {
    if (bytes <= 0) return "0 KB";

    var i = Math.floor(Math.log(bytes) / Math.log(1024)),
        sizes = ['Byte', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    return (bytes / Math.pow(1024, i)).toFixed(2) * 1 + ' ' + sizes[i];
}

/**
 * Returns a human readable date for a unix timestamp in milliseconds
 */
export function prettifyTimestamp(timestamp, withTime=false) {
    let date = new Date(timestamp);
    return withTime ? date.toLocaleString(): date.toLocaleDateString();
}
