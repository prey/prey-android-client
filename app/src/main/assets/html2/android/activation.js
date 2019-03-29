import React from 'react'
import { Link } from 'react-router-dom'
import { Wizard, Steps, Step } from 'react-albus'
import {Input, Group, Space} from 'prey-stash'
import logomonoalt from '../../images/branding/prey-logo-monotono-alt.svg'
import isomono from '../../images/branding/prey-iso-monotono-alt.svg'
import report from '../../images/onboarding/ilust/missing-report.png'
import map from '../../images/onboarding/map.jpg'
import front from '../../images/onboarding/front.jpg'
import back from '../../images/onboarding/back.jpg'



 

const handleClickLogin = (e)=>{
   
  var passlogin=document.getElementById('passlogin').value
  if(window.Android){
     window.Android.login(''+passlogin);
  } else{
      alert("ola oso passlogin:"+passlogin )
  }
}


export default () => (
  <div className="activation fs-onboarding regular inverted">
    <div className="column">
      <Wizard>
        <Steps>
          <Step id="try" render={({ next }) => (
              <div className="container">
                <div id="done" className="column">
                  <h4 className="tac">You're ready</h4>
                  <figure className="anime uno">
                    <figure className="anime dos">
                      <figure className="anime tres">
                        <i className="icon-shield-4"/>
                      </figure>
                    </figure>
                  </figure>
                  <h2><small>Current status</small>
                  <br/>Protected</h2>
                </div>
                <div id="final" className="step">
                   <h4 className="tac">You're ready</h4>
                  <figure className="anime uno">
                    <figure className="anime dos">
                      <figure className="anime tres">
                        <i className="icon-shield-4"/>
                      </figure>
                    </figure>
                  </figure>
                  <h2><small>Current status</small>
                  <br/>Protected</h2>
    
    
     <p className="lead">Credentials, please.</p>

          

          <Input
            label="Password"
            className="verticalForm"
            id="passlogin"
            name="passlogin"
            placeholder=" "
            type="password" 
            
            />

          

          <br/>
    
    
          <Group>
            <Link to="#"  onClick={handleClickLogin}   tabIndex="3" value="Log in" className="btn cta success xl">Log in</Link>
            <Group>
               
            </Group>
          </Group>
    
                </div>
              </div>
            )}
          />
          <Step
          id="arrived"
          render={({ next }) => (
            <React.Fragment>
                <div className="step">
                  <h4 className="tac">Your first Evidence Report arrived!</h4>
                  <figure className="bg-fig">
                    <i className="icon-clipboard"/>
                  </figure>
                  <div className="bubble report">
                    <img src={map} />
                    <div className="row report-logo">
                      <img src={logomonoalt} alt="Prey" />
                      <p>
                        <small>latitude</small>
                        <br/>
                        <b>-70.633</b>
                      </p>
                      <p>
                        <small>longitude</small>
                        <br/>
                        <b>-33.446</b>
                      </p>
                    </div>
                    <div className="row">
                      <span>
                        <small>ssid</small>
                        <br/>Obi Lan Kenobi
                      </span>
                      <span>
                        <small>Mac address</small>
                        <br/>02:00:00:00:00:00
                      </span>
                      <span>
                        <small>Public IP</small>
                        <br/>201.214.254.201
                      </span>
                    </div>
                    <div className="row">
                      <p>
                        <small>owner</small>
                        <br/>
                        <b>Luke Skywalker</b>
                      </p>
                      <p>
                        <small>device model</small>
                        <br/>
                        <b>Motorola Moto Z2 Play</b>
                      </p>
                    </div>
                    <div className="row">
                      <div className="front">
                        <img src={front} />
                      </div>
                      <div className="back">
                        <img src={back} />
                      </div>
                    </div>
                  </div>
                </div>
                <Link to="/" className="btn cta l success icon-thumb-up">
                  Done
                </Link>
              </React.Fragment>
            )}
          />
        </Steps>
      </Wizard>
      <figure className="bg-fig">
        <img src={isomono} alt="Prey" className="logo" />
      </figure>
    </div>
  </div>
)
