

!function($){

 	$(document).ready( function() { 
 		 
 		 
 		 
	    $("#central").load("login_div.html");
	 
	 
		$("#submit").click(function() {
				user=document.getElementById("username").value;
				pwd =document.getElementById("password").value;
				AndroidFunction.login(user,pwd);
		});
		
		$("#submitSignUp").click(function() {
						var url="newUser_div.html";
						  $("#central").load(url);
						  $("#account").show();
						  $("#noaccount").hide();
		});
		
		$("#sigIn").click(function() {
						  var url="login_div.html";
						  $("#central").load(url);
						  $("#account").hide();
						  $("#noaccount").show();
		});
					
 	} );
}(window.jQuery);	