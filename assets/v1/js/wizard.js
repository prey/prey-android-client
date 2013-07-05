//////////////////////////////////////////
// Prey / Android Wizard Logic
// Written by Tomas Pollak
// (c) 2013 Fork Ltd. - forkhq.com
//////////////////////////////////////////

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

  $('#prev').hide();
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
  AndroidFunction.startPage(url);
  try{
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-8743344-9', 'preyproject.com');
  ga('send', 'pageview');
  } catch(err) {}

  $('#main').fadeOut(function(){
    $(this).load(url).fadeIn();
  });

  $('body').attr('id', 'page-' + page); // for css flexibility

  this.updateCircles(page);
  current = page;
};

Wizard.toggle = function(dir){
  var target = current + dir;

  if (target > total_pages || target < start_page)
    return;

  this.load(target);

  $('.button').show();

  if (target <= start_page) { // first page
    $('#prev').hide();
  } else if (target >= total_pages) { // last page
    $('#next').hide();
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
