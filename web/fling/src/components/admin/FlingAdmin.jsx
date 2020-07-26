import React, { useEffect } from 'react';
import { useDispatch } from "react-redux";
import { useParams } from 'react-router-dom';

import { retrieveFlings, setActiveFling } from "../../redux/actions";

import Navbar from './Navbar';
import FlingList from './FlingList';
import FlingContent from './FlingContent';

export default function FlingAdmin() {
  const { flingId } = useParams();
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(retrieveFlings());
  }, [dispatch]);

  useEffect(() => {
    if (flingId) {
      dispatch(setActiveFling(flingId));
    }
  }, [flingId, dispatch]);

  return (
    <div>
      <Navbar />

      <div className="container">
        <div className="columns mt-2">
          <div className="column col-sm-12 col-lg-3 col-2">
            <FlingList />
          </div>
          <div className="column col-sm-12">
            <FlingContent />
          </div>
        </div>
      </div>
    </div>
  );
}
