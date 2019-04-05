import React, { Component }  from 'react'
import { Link } from 'react-router-dom'
import Modal from 'react-modal';

const onb = {
  title: 'Configure Protection2',
 
}
 
const customStyles = {
  content : {
    top                   : '50%',
    left                  : '50%',
    right                 : 'auto',
    bottom                : 'auto',
    marginRight           : '-50%',
    transform             : 'translate(-50%, -50%)'
  }
};

export default class Configure extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      runInBg: true,
      blockUninstall: true,
      shieldOffBbtn:true,
      modalIsOpen: false
    }
    this.openModal = this.openModal.bind(this);
    this.afterOpenModal = this.afterOpenModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
  }

  wipe() {
    if(window.Android){
      window.Android.wipe();
   } else{
       alert("ola oso wipe:" );;
   }
  }

  openModal() {
    this.setState({modalIsOpen: true});
  }

  afterOpenModal() {
    // references are now sync'd and can be accessed.
    this.subtitle.style.color = '#f00';
  }

  closeModal() {
    this.setState({modalIsOpen: false});
  } 

  componentDidMount () {
    console.log('mounted', onb);

    if(window.Android){
      this.setState({
        runInBg: false
      })

     }  
  }
  handleChangeRunBackground = (e) => {
    var runinbg=document.getElementById('run-in-bg').checked;
     
    
    if(window.Android){
       window.Android.runInBg(runinbg);
    } else{
        alert("ola oso runInBg:"+runinbg );;
    }
    this.setState({
      runInBg: !this.state.runInBg
    })
    console.log('mounted', this.state.runInBg);
  }
  handleChangeBlockUninstall = (e) => {
    var blockUninstall=document.getElementById('block-uninstall').checked;
     
    
    if(window.Android){
       window.Android.blockUninstall(blockUninstall);
    } else{
        alert("ola oso blockUninstall:"+blockUninstall );;
    }
    this.setState({
      blockUninstall: !this.state.blockUninstall
    })
    console.log('mounted', this.state.blockUninstall);
  }
  handleChangeShieldOffBtn = (e) => {
    var shieldOffBbtn=document.getElementById('shield-off-btn').checked;
     
    
    if(window.Android){
       window.Android.shieldOffBbtn(shieldOffBbtn);
    } else{
        alert("ola oso shieldOffBbtn:"+shieldOffBbtn );;
    }
    this.setState({
      shieldOffBbtn: !this.state.shieldOffBbtn
    })
    console.log('mounted', this.state.shieldOffBbtn);
  }
  render(){
    return (
      <div className="security fs-onboarding regular inverted">
        <div className="column">
          <h4 className="heading">{onb.title}</h4>
          <p>Complete the setup by activating extra features</p>
          <figure className="bg-fig">
            <i className="icon-cog"/>
          </figure>
          <div className="item">
            <div className="row">
              <figure>
                <i className="icon-panel-settings"/>
              </figure>
              <h5>Run in Background</h5>
              <div className="toggle">
                <input type="checkbox" name="run-in-bg" className="toggle-checkbox" id="run-in-bg"    onChange={(e) => this.handleChangeRunBackground(e)} checked={this.state.runInBg} />
                <label className="toggle-label" htmlFor="run-in-bg">
                    <span className="toggle-inner"></span>
                    <span className="toggle-switch"></span>
                </label>
              </div>
            </div>
            <span>As of Android 8, Prey needs to display a notification to run in the background and actively track the phone.</span>
          </div>
          <div className="line-through" />
          <div className="item">
            <div className="row">
              <figure>
                <i className="icon-login-box-lock"/>
              </figure>
              <h5>Setup Prey PIN</h5>
              <Link to="#" onClick={this.openModal} className="btn">Activate</Link>
            </div>
            <span>Create your 4-digit PIN code to activate the use of remote actions via SMS commands and Android's extra safety features.</span>
          </div>
          <div className="prey-pin">

            <div className="item">
              <div className="row">
                <figure>
                  <i className="icon-mobile-lock"/>
                </figure>
                <h5>Block Uninstall Attempts</h5>
                <div className="toggle">
                  <input type="checkbox" name="block-uninstall" className="toggle-checkbox" id="block-uninstall" onChange={(e) => this.handleChangeBlockUninstall(e)} checked={this.state.blockUninstall} />
                  <label className="toggle-label" htmlFor="block-uninstall">
                      <span className="toggle-inner"></span>
                      <span className="toggle-switch"></span>
                  </label>
                </div>
              </div>
              <span>Blocks the app’s uninstall with your Prey PIN, or native lock screen if available.</span>
            </div>
            <div className="item">
              <div className="row">
                <figure>
                  <i className="icon-lock-shield" />
                </figure>
                <h5>Shield OFF Button</h5>
                <div className="toggle">
                  <input type="checkbox" name="shield-off-btn" className="toggle-checkbox" id="shield-off-btn" onChange={(e) => this.handleChangeShieldOffBtn(e)} checked={this.state.shieldOffBbtn} />
                  <label className="toggle-label" htmlFor="shield-off-btn">
                      <span className="toggle-inner"></span>
                      <span className="toggle-switch"></span>
                  </label>
                </div>
              </div>
              <span>Blocks phone shutdown attempts with your Prey PIN.</span>
            </div>
          </div>
          <div className="line-through" />

          <div className="item">
            <div className="row">
              <figure>
                <i className="icon-login-box-lock"/>
              </figure>
              <h5>Deteach</h5>
              <Link to="#" onClick={this.wipe} className="btn">Wipe</Link>
            </div>
            <span>CreateWipes.</span>
          </div>


           
        </div>

        <div>
         
        <Modal
          isOpen={this.state.modalIsOpen}
          onAfterOpen={this.afterOpenModal}
          onRequestClose={this.closeModal}
          style={customStyles}
          contentLabel="Example Modal"
        >

          <h2 ref={subtitle => this.subtitle = subtitle}>Hello</h2>
          <button onClick={this.closeModal}>close</button>
          <div>I am a modal</div>
          <form>
            <input />
            <button>tab navigation</button>
            <button>stays</button>
            <button>inside</button>
            <button>the modal</button>
          </form>
        </Modal>
      </div>
      </div>   



    )
  }
}
 