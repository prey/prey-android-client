function pointCircle(cantidad,pag){
    for (var i=1;i<cantidad;i++){
    		var pointCircle=document.getElementById("circle0"+i);
    		pointCircle.className="circle off";
	}
	var pointCirclePag=document.getElementById("circle0"+pag);
	pointCirclePag.className="circle on";
}

function onActiveCamouflage(){
	activeCamouflageValue=document.getElementById("activeCamouflage").checked; 	 
	AndroidFunction.activeCamouflageHtml(""+activeCamouflageValue);
} 

function onConfigureSim(){
	configureSimValue=document.getElementById("configureSim").checked;
	AndroidFunction.configureSimHtml(""+configureSimValue);
}

function onChooseYourHero(){
	AndroidFunction.chooseYourHeroHtml();
}

function onUninstallLock(){
	uninstallLockValue=document.getElementById("uninstallLock").checked;
	AndroidFunction.uninstallLockHtml(""+uninstallLockValue);
}

function onUnlockPass(){
	 unlockPassValue=document.getElementById("unlockPass").checked;
	 AndroidFunction.onUnlockPass(unlockPassValue);
} 

function prueba(){
	 AndroidFunction.showToast("Prueba");
} 

function login(){
	user=document.getElementById("email").value;
	pwd =document.getElementById("password").value;
	AndroidFunction.login(user,pwd);
}  

function newuser(){
    name=document.getElementById("username").value;
	user=document.getElementById("email").value;
	pwd =document.getElementById("password").value;
	pwd2 =document.getElementById("password2").value;
	AndroidFunction.newuser(name,user,pwd,pwd2);
} 

function userRegistered(){
	pwd =document.getElementById("password").value;
	AndroidFunction.userRegistered(pwd);
}

function onPanel(){
	AndroidFunction.goPanel();
}


 