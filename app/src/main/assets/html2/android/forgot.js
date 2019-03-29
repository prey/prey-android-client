import React from 'react'
import logomono from '../../images/branding/prey-logo-monotono-alt.svg'

export default () => (
  <div className="inner-content">
    <div className="content">
      <div className="login fs-onboarding regular inverted">
        <div className="column">
          <div className="sign-form">
            <form autoComplete="off" className="form form-session" id="new-session" acceptCharset="UTF-8">
              <input type="hidden" name="utf8" value="✓" />
              <span className="here">Reset your password, you say?</span>
              <ul className="form vvv">
                <li>
                  <label>Please enter your email</label>
                  <input id="email" name="email" required type="email" tabIndex="1" />
                </li>
              </ul>
              <a href="/onboarding/android/login" tabIndex="2" value="Send instructions" className="btn l">Send instructions</a>
            </form>
            <div className="line-through" />
            <a className="btn-link btn-block" href="/onboarding/android/login">Log in</a>
            <a className="btn-link btn-block" href="/onboarding/android/signup">Don't have an account?</a>
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