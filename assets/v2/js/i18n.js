var I18n = {};

I18n.replace = function(page) {
  return false; // disabled for now

  if (typeof AndroidFunction != 'undefined') {
    $('#html_1_first').html(AndroidFunction.textStringXml("html_1_first"));
    $('#html_1_second').html(AndroidFunction.textStringXml("html_1_second"));

    $('#html_2_first').html(AndroidFunction.textStringXml("html_2_first"));
    $('#html_2_second').html(AndroidFunction.textStringXml("html_2_second"));

    $('#html_3_first').html(AndroidFunction.textStringXml("html_3_first"));
    $('#html_3_second').html(AndroidFunction.textStringXml("html_3_second"));

    $('#html_4_first').html(AndroidFunction.textStringXml("html_4_first"));
    $('#html_4_second').html(AndroidFunction.textStringXml("html_4_second"));
    $('#html_4_third').html(AndroidFunction.textStringXml("html_4_third"));

    $('#html_5_first').html(AndroidFunction.textStringXml("html_5_first"));
    $('#html_5_second').html(AndroidFunction.textStringXml("html_5_second"));
    $('#html_5_third').html(AndroidFunction.textStringXml("html_5_third"));

    $('#html_ok_first').html(AndroidFunction.textStringXml("html_ok_first"));
    $('#html_ok_second').html(AndroidFunction.textStringXml("html_ok_second"));
    $('#submitWeb').html(AndroidFunction.textStringXml("html_ok_third"));

    $('#html_warn_first').html(AndroidFunction.textStringXml("html_warn_first"));
    $('#html_warn_second').html(AndroidFunction.textStringXml("html_warn_second"));
    $('#submitGrant').html(AndroidFunction.textStringXml("html_warn_third"));
  }
}
