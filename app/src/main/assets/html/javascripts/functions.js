 function panel(){
       Android.panel();
    }

    function signin(){
      var passs=$("#lpass").val();
      if(passs==''){
         $('#label_pass').addClass("form-inline-error");
      }else{
         $('.popover').toggleClass("show");
         $('#mobile').show();
         Android.signIn(passs);
      }
    }

    function openSettings(){
      $('#modal-login').show();
      $("#lpass").val('');
      $('.popover').toggleClass("show");
      $('#label_pass').removeClass("form-inline-error");
      $('#mobile').hide();
    }

    function closeSettings(){
      $('#modal-login').hide();
      $('#mobile').show();
    }

    function givePermissions(){
          Android.givePermissions();
    }