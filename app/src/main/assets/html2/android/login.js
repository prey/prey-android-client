import React from 'react'
import { Link } from 'react-router-dom'
import {Input, Group, Space} from 'prey-stash'
import logomono from '../../images/branding/prey-logo-monotono-alt.svg'

 
 
 

const handleClickLogin = (e)=>{
  var emaillogin=document.getElementById('emaillogin').value
  var passlogin=document.getElementById('passlogin').value
  if(window.Android){
     window.Android.mylogin(''+emaillogin,''+passlogin);
  } else{
      alert("ola oso emaillogin:"+emaillogin+"  passlogin:"+passlogin )
  }
}

const handleClickQR = (e)=>{
   
  if(window.Android){
     window.Android.qr();
  } else{
      alert("ola oso qr" )
  }
}

export default () => (
  <div className="login fs-onboarding regular inverted">
    <div className="column">
      <div className="sign-form">
        <form autoComplete="off" className="form form-session" id="new-session" acceptCharset="UTF-8" >
          <input type="hidden" name="utf8" value="✓" />
          {/*<span className="msg info-error">Good try. We almost fell for it.</span>
          <span className="msg info-notice">Please enter your email.</span>*/}
          <h1>SIGN IN</h1>
          <p className="lead">Credentials, please.</p>

          <Input
            label="Your Email"
            className="verticalForm"
            id="emaillogin"
            name="emaillogin"
            placeholder=" "
            type="email"
            
          />

          <Input
            label="Password"
            className="verticalForm"
            id="passlogin"
            name="passlogin"
            placeholder=" "
            type="password" 
            
            />

          <Input
            label="Remember Me"
            className="verticalForm"
            id="remember_me"
            type="checkbox"
           
            />

          <br/>

          <Group>
            <Link to="#"  onClick={handleClickLogin}   tabIndex="3" value="Log in" className="btn cta success xl">Log in</Link>
            <Group>
              <Space />
              <Link to="#"  onClick={handleClickQR}  tabIndex="4" className="btn alt cta">&nbsp; Scan QR Code &nbsp;</Link>
            </Group>
          </Group>


          {/*
          <ul className="form vvv">
            <li>
              <label>Your Email</label>
              <input id="email" name="email" required type="email" tabIndex="1" />
            </li>
          </ul>
          <ul className="form vvv">

            <li>
              <label className="tag-label" htmlFor="password">Your password for <span>cate@preyhq.com</span>
              </label>
              <div className="button-group">
                  <div className="stretch">
                      <input type="password" name="password" id="password" tabIndex="1" className="form-control stretch" />
                  </div>
                  <a className="ch-acc"><small>Change account</small></a>
              </div>
              <div className="checkbox">
                <div className="custom-checkbox">
                    <input id="remember_me" type="checkbox" name="remember_me" value="1" tabIndex="3" />
                    <label htmlFor="remember_me" className="check-box"></label>
                </div>
                <label htmlFor="remember_me">Remember me</label>
              </div>
            </li>
          </ul>
          <div className="button-group">
            <a href="/onboarding/android/security" tabIndex="3" value="Log in" className="cta btn success icon-off">Log in</a>
            <button type="" name="" tabIndex="4" value="" className="cta btn icon-qr-code">Scan QR Code</button>
          </div>
          */}
        </form>
          <span className="here">Login or create account with</span>
          <div className="button-group">
            <a href="/auth/facebook" className="btn btn-facebook icon-facebook-squared">Facebook</a>
            <a href="/auth/twitter" className="btn btn-twitter icon-twitter">Twitter</a>
          </div>
          <p className="here">Maybe you...</p>
          <a className="btn-link btn-block" href="#/onboarding/android/signup">Don't have an account?</a>
          <a className="btn-link btn-block" href="/onboarding/android/forgot">Forgot your password?</a>
      </div>

      <div id="footer">
        <footer className="copy">
          <div className="footer-links">
            <a href="https://preyproject.com/terms" target="_blank">Terms of Service</a>
            <a href="https://preyproject.com/privacy" target="_blank">Privacy Policy</a>
            <a href="https://www.preyproject.com/privacy#cookies" target="_blank">Cookies</a>
          </div>
          <a className="backToLogin" data-tooltip="Back to login" href="/">
            <img src={logomono} alt="Prey" className="logo" />
          </a>
        </footer>
      </div>
    </div>
  </div>
)
