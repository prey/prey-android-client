    var from='conf';

    function panel(){
      from='panel';
      if(!Android.openSettings()){
        openSettings();
      }else{
        openSettingsTwoStep();
      }
    }

    function setting(){
      from='conf';
      if(!Android.openSettings()){
        openSettings();
      }else{
        openSettingsTwoStep();
      }
    }

    function signin(){
      var passs=$("#lpass").val();
      if(passs==''){
         $('#label_pass').addClass("form-inline-error");
      }else{
         if(!Android.openSettings()){
             $('.popover').toggleClass("show");
             $('#mobile').show();
             Android.signIn(passs,from);
         }else{
            $('#modal-login').hide();
            $('#modal-loginTwoStep').show();
         }
      }
    }

    function signInTwoStep(){
       var lpass2=$("#lpass2").val();
       var sixdigit=$("#sixdigit").val();
       if(lpass2==''||sixdigit==''){
          if(lpass2==''){
            $('#label_pass2').addClass("form-inline-error");
          }else{
            $('#label_pass2').removeClass("form-inline-error");
          }
          if(sixdigit==''){
            $('#label_sixdigit').addClass("form-inline-error");
          }else{
            $('#label_sixdigit').removeClass("form-inline-error");
          }
       }else{
          $('.popover').toggleClass("show");
          $('#mobile').show();
          Android.signInTwoStep(lpass2,sixdigit,from);
       }
    }

    function openSettings(){
       $('#modal-login').show();
       $('#modal-loginTwoStep').hide();
       $("#lpass").val('');
       $('.popover').toggleClass("show");
       $('#label_pass').removeClass("form-inline-error");
       $('#mobile').hide();
    }

    function openSettingsTwoStep(){
       $('#modal-loginTwoStep').show();
       $('#modal-login').hide();
       $("#lpass2").val('');
       $("#sixdigit").val('');
       $("#label_sixdigit").val('');
       $('.popover').toggleClass("show");
       $('#label_pass2').removeClass("form-inline-error");
       $('#label_sixdigit').removeClass("form-inline-error");
       $('#mobile').hide();
    }

    function closeSettings(){
      $('#modal-login').hide();
      $('#mobile').show();
    }

    function closeSettingsTwoStep(){
      $('#modal-loginTwoStep').hide();
      $('#mobile').show();
    }

    function givePermissions(){
      Android.givePermissions();
    }