//////////////////////////////////////////
// Prey / Android Wizard Logic
// Written by Tomas Pollak
// (c) 2013 Fork Ltd. - forkhq.com
//////////////////////////////////////////
$.getScript('js/i18n.js');

var current = 0,
    start_page,
    total_pages;

var Wizard = {};

$(function(){
  // when a '.toggle' link is clicked, switch to that page
  $('.toggle').live('click', function(e){

    // get page id from link's href (eg. '#page-5')
    var page = $(this).attr('href').replace('#page-', '');
    Wizard.load(page);

    // the browser should not follow the link
    e.preventDefault();
  })
})

//////////////////////////////////////////
// start/load/toggle
//////////////////////////////////////////

Wizard.start = function(total){
  total_pages = total;
  start_page  = 1;

  this.drawCircles(total);

  $('#prev').addClass('disabled');
  Wizard.load(1);

  $('#next').click(function() {
    Wizard.toggle(+1);
  });

  $('#prev').click(function() {
    Wizard.toggle(-1);
  });
};

Wizard.load = function(page){
  var url = 'views/' + page + '.html';

  $.get(url, function(html){
    $('#main').fadeOut(function(){
      $(this).html(html).fadeIn();
	  replaceI18n(page);
    });
  });

  $('body').attr('id', 'page-' + page); // for css flexibility

  this.updateCircles(page);
  current = page;
};

Wizard.toggle = function(dir){
  var target = parseInt(current) + dir;

  if (target > total_pages || target < start_page)
    return;

  this.load(target);
  $('#navigation button').removeClass('disabled');

  if (target <= start_page) { // first page
    $('#prev').addClass('disabled');
  } else if (target >= total_pages) { // last page
    $('#next').addClass('disabled');
  }
};

//////////////////////////////////////////
// circles
//////////////////////////////////////////

Wizard.drawCircles = function(number){
  var html = '',
      el = $('#circles');

  for (i = 0; i < number; i++) {
    html += '<li id="circle-' + (i+1) + '">';
  }

  el.html(html).children().first().addClass('on');
}

Wizard.updateCircles = function(page){
  $('#circle-' + page).addClass('on').siblings().removeClass('on');
}
