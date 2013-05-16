

!function($){

 	$(document).ready( function() { 
 		 
 		 $("#noaccount").hide();
 		 $("#account").hide();
 		 $("#btn_back_div").hide();
 		 $("#central").load("install01_div.html");

 		 var pag=1; 
 		 var cantidad=5;
         $("#btn_next_div").click(function() {
            			 pag=pag+1;
            			 if (pag>1){
            			 	  $("#btn_back_div").show();
            			 }
            			 if (pag<(cantidad+1)){
            			 	  var url="";
            			 	  if (pag==cantidad){
            			         url="login_div.html";
            			         $("#central").load(url,function(responseTxt,statusTxt,xhr){
            			         		if(statusTxt=="success"){
            			         			$("#email").val(AndroidFunction.loginMail());
            			         		}
            			         });            			         
            			      }else{
            			      	 url="install0"+pag+"_div.html"; 
            			      	 $("#central").load(url);           			      	 
            			      }
						 }
						 if (pag>=cantidad){
							  $("#btn_next_div").hide();
							  $("#noaccount").show();
							  $("#wrap_circles").hide();	
						 }
						 pointCircle(cantidad,pag);
		});
		
		$("#btn_back_div").click(function() {
            			 pag=pag-1;
            			 if (pag>0){
            			 	  var url="install0"+pag+"_div.html";
							  $("#central").load(url);
						 }
						 if (pag<=1){
            			 	  $("#btn_back_div").hide();	
						 }
						 if (pag<cantidad)	{
							  $("#btn_next_div").show();
							  $("#noaccount").hide();
							  $("#wrap_circles").show();	
						 }
						 pointCircle(cantidad,pag); 
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