import React from 'react'
import { Link } from 'react-router-dom'
import logomono from '../../images/branding/prey-logo-monotono-alt.svg'

const recaptcha = {
  width: 250, 
  height: 40,
  borderColor: 'rgb(193, 193, 193)', 
  margin: 25, 
  padding: 0, 
  resize: 'none', 
  display: 'none'
}


const handleClick = (e)=>{
  var name=document.getElementById('name').value;
  var email=document.getElementById('email').value;
  var password1=document.getElementById('password1').value;
  var password2=document.getElementById('password2').value;
  var policy_rule_age=document.getElementById('user[policy_rule_age]').value;
  var policy_rule_privacy_terms=document.getElementById('user[policy_rule_privacy_terms]').value;
  
  if(window.Android){
     window.Android.mylogin(''+name,''+email);
  } else{
      alert("ola oso signup name:"+name+" email:"+email+"  password1:"+password1 );;
  }
}



export default () => (
  <div className="inner-content">
    <div className="content">
      <div className="login fs-onboarding regular inverted">
        <div className="column">
          <div className="sign-form">
            <form autoComplete="off" className="form form-session" id="new-session"acceptCharset="UTF-8">
              <input type="hidden" name="utf8" value="✓" />
              <span className="msg info-error">Password doesn't match. Try again.</span>
              <span className="msg info-error">That mail has already been taken. Did you already register?</span>
              <span className="msg info-notice">Please enter your email.</span>
              <span className="h1">SIGN UP</span>
              <span className="here">One step from your new Prey account.</span>
              <ul className="form vvv">
                <li>
                  <label>Your Name</label>
                  <input id="name" name="name" required type="text" tabIndex="1" />
                </li>
                <li>
                  <label>Your Email</label>
                  <input id="email" name="email" required type="email" tabIndex="2" />
                </li>
              </ul>
              <ul className="form hvv">
                <li>
                  <label className="tag-label" htmlFor="password">Your password</label>
                  <input type="password" name="password1" id="password1" tabIndex="3" />
                </li>
                <li>
                  <label className="tag-label" htmlFor="password">Confirm your password</label>
                  <input type="password" name="password2" id="password2" tabIndex="4" />
                </li>
              </ul>
              <ul className="form vvv">
                <div className="checkbox">
                  <div className="custom-checkbox">
                      <input type="checkbox" id="user[policy_rule_age]" type="checkbox" name="user[policy_rule_age]" value="on" />
                      <label htmlFor="user[policy_rule_age]" className="check-box"></label>
                  </div>
                  <label htmlFor="user[policy_rule_age]">I confirm that I am over 16 years old.</label>
                </div>
                <div className="checkbox">
                  <div className="custom-checkbox">
                      <input id="user[policy_rule_privacy_terms]" type="checkbox" name="user[policy_rule_privacy_terms]" value="on"  />
                      <label htmlFor="user[policy_rule_privacy_terms]" className="check-box"></label>
                  </div>
                  <label htmlFor="user[policy_rule_privacy_terms]">I have read and accept the <a target="_blank" href="https://preyproject.com/terms">Terms &amp; Conditions</a> and the <a target="_blank" href="https://preyproject.com/privacy">Privacy Policy</a>.</label>
                </div>
              </ul>
              <div className="button-group">
                <Link to="#" value="Log in"  onClick={handleClick} className="cta btn xl success icon-enter">Sign up</Link>
              </div>
            </form>   

            <div className="line-through" />
            <a className="btn-link btn-block" href="#/onboarding/android/login">Log in</a>
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
    </div>
  </div>
)