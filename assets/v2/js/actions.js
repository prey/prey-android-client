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
    e.preventDefault();

    var data = {
      username : this.name.value,
      email    : this.email.value,
      password : this.password.value
    }

    if (typeof AndroidFunction != 'undefined')
      AndroidFunction.newuser(data.username, data.email, data.password);

    // Wizard.load('enable');
  })

  $('#login').live('submit', function(e){
    e.preventDefault();

    var data = {
      email    : this.email.value,
      password : this.password.value
    }

    if (typeof AndroidFunction != 'undefined')
        AndroidFunction.login(data.email, data.password);

    // Wizard.load('enable');
  })

  $('#submitGrant').live('click', function(e){
    e.preventDefault();
    if (typeof AndroidFunction != 'undefined')
      AndroidFunction.permission();
  });

  $('#submitWeb').live('click', function(e){
    e.preventDefault();

    if (typeof AndroidFunction != 'undefined')
     	AndroidFunction.openPanel();
  });

  $('#submitWeb2').live('click', function(e){
    e.preventDefault();

    if (typeof AndroidFunction != 'undefined')
     	AndroidFunction.loginPanel();
  });



})
