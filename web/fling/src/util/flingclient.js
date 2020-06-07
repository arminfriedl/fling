import log from 'loglevel';
import axios from 'axios';

import request from './request';

let flingClient = {

    deleteFling: function(flingId) {
        return request.delete(`/fling/${flingId}`)
            .then(response => log.info(`Deleted fling ${flingId}`));
    },

    getFlings: function() {
        return request.get('/fling')
            .then(response => {
                log.info(`Got ${response.data.length} flings`);
                return response.data;
            });
    },

    putFling: function(flingId, update) {
        return request.put(`/fling/${flingId}`, update)
            .then(response => log.info(`Updated fling ${flingId}`));
    },

    postFling: function(newFling) {
        return request.post(`/fling`, newFling)
            .then(response => log.info(`Created fling ${response.data}`));
    },

    getFling: function(flingId) {
        return request.get(`/fling?flingId=${flingId}`)
            .then(response => {
                log.info(`Got fling ${flingId}`);
                return response.data;
            });
    },

    getFlingByShareId: function(shareId) {
        if(!shareId) return Promise.resolve(null);

        return request.get(`/fling?shareId=${shareId}`)
            .then(response => {
                log.info(`Got fling ${response.data.id}`);
                return response.data;
            })
            .catch(err => {
                if (err.isAxiosError && err.response.status === 404) {
                    return null;
                }
                throw err;
            });
    },

    getShareExists: function(shareId) {
        if(!shareId) {
            console.info("Empty share id, do not check uniqueness");
            return Promise.resolve(true);
        }

        return request.get(`/fling/shareExists/${shareId}`)
            .then(response => {
                if(response.data){
                    log.info(`Share with id ${shareId} already exists`);
                } else {
                    log.info(`Share with id ${shareId} does not exist`);
                }
                return response.data;
            });
    },

    packageFling: function(flingId) {
        return request.get(`/fling/${flingId}/package`)
            .then(response => {
                log.debug("Got fling download id", response.data);
                let url = `${process.env.REACT_APP_API}/fling/${flingId}/download/${response.data}`;
                log.debug("Download url", url);
                return url;
            });
    },
};

let artifactClient = {

    getArtifacts: function(flingId) {
        return request.get(`/artifacts?flingId=${flingId}`)
            .then(response => {
                log.info(`Got ${response.data.length} artifacts`);
                return response.data;
            });
    },

    postArtifact: function(flingId, artifact, progressIndicator) {
        return request.post(`/artifacts/${flingId}`, artifact)
            .then(response => {
                log.info(`Uploaded artifact: ${response.data}`);
                return request.patch(`/artifacts/${response.data.id}`,
                                     {name: artifact.name, size: artifact.size});
            })
            .then(response => {
                log.info(`Updated artifact data: ${response.data}`);
                return response.data;
            })
            .catch(err => log.error(`Error while uploading artifact: ${err}`));
    },

    deleteArtifact: function(artifactId) {
        return request.delete(`/artifacts/${artifactId}`)
            .then(response => {
                log.info(`Delete artifact ${artifactId}`);
            });
    },

    downloadArtifact: function(artifactId) {
        return request.get(`/artifacts/${artifactId}/downloadid`)
            .then(response => {
                log.debug("Got download id", response.data);
                let url = `${process.env.REACT_APP_API}/artifacts/${artifactId}/${response.data}/download`;
                log.debug("Download url", url);
                return url;
            });
    }
};

export {flingClient, artifactClient};
