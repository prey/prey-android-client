!function($){

 	$(document).ready( function() { 
 						
 						
 		$("#central").load("userRegistered_div.html");
 						 
 						 
 		$("#submit").click(function() {
				pwd =document.getElementById("password").value;
				AndroidFunction.userRegistered(pwd);
		});
 						
 	} );
}(window.jQuery);	