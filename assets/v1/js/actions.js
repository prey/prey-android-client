$(function(){

  // enable touch events
  var opts = { drag: true, transform: true };

  $('#main').hammer(opts)
    .on("swipeleft", function() {
      Wizard.toggle(+1);
    })
    .on("swiperight", function() {
      Wizard.toggle(-1);
    });

  $('#signup').live('submit', function(e){

    var data = {
      username: this.name.value,
      email: this.email.value,
      password: this.password.value
    }

    console.log(data);
    alert('Signing up!');

    // AndroidFunction.newuser();
    Wizard.load(6);

    e.preventDefault();
  })

  $('#login').live('submit', function(e){

    var data = {
      email: this.email.value,
      password: this.password.value
    }

    //console.log(data);
    //alert('Logging in!');

    AndroidFunction.login(data.email,data.password);
    

    //e.preventDefault();
  })

  $('a.panel').live('click', function(e){
    // AndroidFunction.goPanel();
    //alert('Going to panel');
    //e.preventDefault();
  })

  $('a.grant-rights').live('click', function(e){
    //alert('Showing grants');
    //e.preventDefault();
    AndroidFunction.permission();
  });

})
