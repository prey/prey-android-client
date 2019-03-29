import React from 'react'
import { Link } from 'react-router-dom'

const handleClick = (e)=>{
  alert("ola oso")
  if(window.Android){
    window.Android.givePermissions()
  } 
}

export default () => (
  <div className="perms fs-onboarding regular inverted">
  	<div className="column">
    	<h4 className="heading">Prey needs permissions to completely secure your phone.</h4>
  		<figure className="bg-fig">
        <i className="icon-attention"/>
      </figure>
      <h5>Device Administrator</h5>
  		<p>Required for the Remote Wipe and Lock features.</p>
      <h5>Camera, Location, State</h5>
  		<p>Needed for tracking and pictures in Evidence reports.</p>
      <h5>Draw Over Other Apps</h5>
  		<p>Used in Prey’s remote lock screen to avoid user navigation.</p>
      <h5>Access Contact and Calls</h5>
      <p>For providing the phone’s IMEI on Evidence Reports only.</p>
      <h5>Access Media and Files</h5>
      <p>Allows the use of our remote File Retrieval and Wipe tools.</p>
    	<Link to="#" onClick={handleClick} className="btn cta l success icon-right">Approve</Link>
    </div>
  </div>
)
