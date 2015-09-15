$(function(){


  $('.m-scooch').scooch();
  var aok = 0;

  $('#complete').toggleClass('off on');




  $('#close1').click(function(){
    Android.closeTour();
    $('#aTour').addClass('removeTourBtn');
  });
  $('#close2').click(function(){
    Android.closeTour();
    $('#aTour').addClass('removeTourBtn');
  });
  $('#close3').click(function(){
    Android.closeTour();
    $('#aTour').addClass('removeTourBtn');
  });
  $('#close4').click(function(){
    Android.closeTour();
    $('#aTour').addClass('removeTourBtn');
  });
  $('#close5').click(function(){
    Android.closeTour();
    $('#aTour').addClass('removeTourBtn');
  });
  $('#close6').click(function(){
      Android.closeTour();
      $('#aTour').addClass('removeTourBtn');
  });
  $('#close7').click(function(){
        Android.closeTour();
        $('#aTour').addClass('removeTourBtn');
  });

  $('#remoteControl').click(function(){
    Android.remoteControl();
  });
  $('#settings').click(function(){
    Android.settings();
  });

  $('#uninstall').click(function(){
    Android.uninstall();
  });

  $('#signIn').click(function(){
    var pass=$('#pass').val();
    Android.signIn(pass);
  });

  $('#forgotPassword').click(function(){
    Android.forgotPassword();
  });

});
