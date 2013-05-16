


!function($){

 	$(document).ready( function() { 



		$("#btn_back_div").hide();

		$("#central").load("wizard02_div.html");
		
 		 var pag=2; 
 		 var cantidad=3

         $("#btn_next_div").click(function() {
            			  pag=pag+1;
            			  if (pag>1){
            			 	    $("#btn_back_div").show();
            			  }
            			  if (pag<(cantidad+1)){
							    $("#central").load("wizard0"+pag+"_div.html");
						  }
						  if (pag>=cantidad){
							    $("#btn_next_div").hide();
							    $("#wrap_circles").hide();
						  }
						  pointCircle(cantidad,pag);
		});

		$("#btn_back_div").click(function() {
            			  pag=pag-1;
            			  if (pag>1){
								$("#central").load("wizard0"+pag+"_div.html");
						  }
						  if (pag<=2){
            			 	    $("#btn_back_div").hide();	
						  }
						  if (pag<cantidad)	{
							    $("#btn_next_div").show();
							    $("#wrap_circles").show();
						  }
						  pointCircle(cantidad,pag); 
		});
								 
		
					
 	} );
}(window.jQuery);	