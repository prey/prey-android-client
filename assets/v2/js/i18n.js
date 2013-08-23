function replaceI18n(page) {
 if (page==1){
 	$('#html_1_first').html(AndroidFunction.textStringXml("html_1_first"));
 	$('#html_1_second').html(AndroidFunction.textStringXml("html_1_second"));
 }
 if (page==2){
 	$('#html_2_first').html(AndroidFunction.textStringXml("html_2_first"));
	$('#html_2_second').html(AndroidFunction.textStringXml("html_2_second"));
 }
 if (page==3){
 	$('#html_3_first').html(AndroidFunction.textStringXml("html_3_first"));
 	$('#html_3_second').html(AndroidFunction.textStringXml("html_3_second"));
 }
 if (page==4){
 	$('#html_4_first').html(AndroidFunction.textStringXml("html_4_first"));
 	$('#html_4_second').html(AndroidFunction.textStringXml("html_4_second"));
 	$('#html_4_third').html(AndroidFunction.textStringXml("html_4_third"));
 }
 if (page==5){
 	$('#html_5_first').html(AndroidFunction.textStringXml("html_5_first"));
 	$('#html_5_second').html(AndroidFunction.textStringXml("html_5_second"));
 	$('#html_5_third').html(AndroidFunction.textStringXml("html_5_third"));
 }
}