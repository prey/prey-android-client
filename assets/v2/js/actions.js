$(function(){

  // enable touch events only if not viewing a specific page
  if (!window.location.hash.match('#')) {

   var opts = {}; // { drag: true, transform: true };

    $('#main').hammer(opts)
      .on("swipeleft", function() {
        Wizard.toggle(+1);
      })
      .on("swiperight", function() {
        var dir = current == 5 ? -2 : -1; // back two if on signup page
        Wizard.toggle(dir);
      });

  }

  $('#signup').live('submit', function(e){

    var data = {
      username: this.name.value,
      email: this.email.value,
      password: this.password.value
    }

   // console.log(data);
   // alert('Signing up!');

    AndroidFunction.newuser(data.username,data.email,data.password);
    // Wizard.load('enable');

   //  e.preventDefault();
  })

  $('#login').live('submit', function(e){

    var data = {
      email: this.email.value,
      password: this.password.value
    }

   // console.log(data);
   // alert('Logging in!');

    AndroidFunction.login(data.email,data.password);
    // Wizard.load('enable');

   // e.preventDefault();
  })

  $('a.panel').live('click', function(e){
    // AndroidFunction.goPanel();
   // alert('Going to panel');
   // e.preventDefault();
  })

  $('a.grant-rights').live('click', function(e){
    // alert('Showing grants');
    // e.preventDefault();
     AndroidFunction.permission();
  });
  
    $('#submitGrant').live('click', function(e){
    // alert('Showing grants');
    // e.preventDefault();
     AndroidFunction.permission();
  });

 $('#submitWeb').live('click', function(e){
 	// alert('Showing grants');
   	AndroidFunction.openPanel();
  });


 
 	
 

})
