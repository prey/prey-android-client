$(function(){
 

  $('.m-scooch').scooch();

  $('#uncomplete').toggleClass('off on');





  $('#btn_login').click(function(){
    var email=$('#email').val();
    var pass=$('#pass').val();
    Android.login(email,pass);
  });

  $('#btn_register').click(function(e){
    var email=$('#email2').val();
    var pass=$('#pass2').val();
    var usr=$('#usr2').val();
    Android.register(usr,email,pass);
  });

});

