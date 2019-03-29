import React, {Fragment} from 'react'
// import Slider from '../../vendor/stash/components/Slider/Slider.js'
import Slider from "react-slick"
import isomono from '../../images/branding/prey-iso-monotono-alt.svg'

// import { BrowserRouter } from 'react-router-dom'
import { Route, Link } from 'react-router-dom'

import {Group} from 'prey-stash'

import Permissions from "./permissions"
import Security from './security'
import Signup from './signup'
import Forgot from './forgot'
import Login from './login'

import Activation from './activation'

// Your Security Hub
import sh1 from '../../images/onboarding/ilust/1_security-hub-01.png'
import sh2 from '../../images/onboarding/ilust/1_security-hub-02.png'
import sh3 from '../../images/onboarding/ilust/1_security-hub-03.png'
import sh4 from '../../images/onboarding/ilust/1_security-hub-04.png'
import sh5 from '../../images/onboarding/ilust/1_security-hub-05.png'
import sh6 from '../../images/onboarding/ilust/1_security-hub-06.png'
import sh7 from '../../images/onboarding/ilust/1_security-hub-07.png'
// Track & Find
import tf1 from '../../images/onboarding/ilust/02-Track-Find.png'
// React & Protect
import rp1 from '../../images/onboarding/ilust/03-React-Protect.png'
// Recover with evidence
import re1 from '../../images/onboarding/ilust/04-Evidencev.png'

class Start extends React.Component {
  render() {
    const settings = {
      dots: true,
      arrows: false,
      appendDots: dots => <Group><Group>{dots}</Group></Group>,
      dotsClass: 'dots-as-dots'
    }
    return(
      <div className="start fs-onboarding regular inverted">
        <div className="column">
          <Slider {...settings}>
            <div>
              <h4 className="heading">Your security hub</h4>
              <p className="lead">Protect your phone, laptop and tablet with the one app you can access anywhere</p>
              <figure className="secHub">
                <div className="bg" />
                <img src={sh1} className="first" alt="ref" />
                <img src={sh2} className="second" alt="ref" />
                <img src={sh3} className="third" alt="ref" />
                <img src={sh4} className="fourth" alt="ref" />
                <img src={sh5} className="fifth" alt="ref" />
                <img src={sh6} className="sixth" alt="ref" />
                <img src={sh7} className="seventh" alt="ref" />
              </figure>
            </div>
            <div>
              <h4 className="heading">Track and find</h4>
              <p className="lead">Know where you lost your device is and detect when it moves somewhere.</p>
              <figure className="trkFnd">
                <div className="bg" />
                <img src={tf1} className="first" alt="ref" />
              </figure>
            </div>
            <div>
              <h4 className="heading">React & protect</h4>
              <p className="lead">Protect your device and its data with security actions like Lock, Wipe and Alarm</p>
              <figure>
                <div className="bg" />
                <img src={rp1} className="first" alt="ref" />
              </figure>
            </div>
            <div>
              <h4 className="heading">Recover with evidence</h4>
              <p className="lead">Get evidence reports with pictures, screenshots, nearby WiFis, user data and locations.</p>
              <figure>
                <div className="bg" />
                <img src={re1} className="first" alt="ref" />
              </figure>
            </div>
          </Slider>
          <figure className="bg-fig">
            <img src={isomono} alt="Prey" className="logo" />
          </figure>
          <Link to={"/onboarding/android/permissions"} className="btn cta l success icon-right">
            Start
          </Link>
        </div>
      </div>
    )
  }
}

export default ({ match }) => (

  <Fragment>

    <Route
      exact
      path={"/"}
      component={Start}
    />

    <Route
      exact
      path={"/onboarding/android/"}
      component={Start}
    />

    <Route
      exact
      path={"/onboarding/android/permissions"}
      component={Permissions}
    />

    <Route
      exact
      path={"/onboarding/android/security"}
      component={Security}
    />

    <Route
      exact
      path={"/onboarding/android/signup"}
      component={Signup}
    />

    <Route
      exact
      path={"/onboarding/android/forgot"}
      component={Forgot}
    />

    <Route
      exact
      path={"/onboarding/android/login"}
      component={Login}
    />

    <Route
      exact
      path={"/onboarding/android/activation"}
      component={Activation}
    />


  </Fragment>
)
