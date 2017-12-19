$(function(){

  window.mySwiper = new Swiper ('.swiper-container', {
    init: false,
    runCallbacksOnInit: true,
    speed: 600,
    spaceBetween: 600,
    loop: false,
    parallax: false,
    paginationClickable: true,
    pagination: {
      el: '.swiper-pagination',
    },
    simulateTouch: true,
    mousewheel: {
      invert: true,
    }
  });

  // window.mySwiper.on('init', function () {
  //   window.myVivus =  new Vivus('doodles-01', {duration: 500, file: '../images/doodles-01.svg', type: 'delayed', pathTimingFunction: Vivus.EASE, animTimingFunction: Vivus.EASE_OUT });
  // });
  // window.mySwiper.on('slideChange', function () {
  //   if (window.mySwiper.activeIndex === 1) {
  //     // console.log('slide 2');
  //     window.myVivus2 =  new Vivus('doodles-02', {duration: 500, file: '../images/doodles-02.svg', type: 'delayed', pathTimingFunction: Vivus.EASE, animTimingFunction: Vivus.EASE_OUT });
  //   }
  //   if (window.mySwiper.activeIndex === 2) {
  //     // console.log('slide 3');
  //     window.myVivus3 =  new Vivus('doodles-03', {duration: 500, file: '../images/doodles-03.svg', type: 'delayed', pathTimingFunction: Vivus.EASE, animTimingFunction: Vivus.EASE_OUT });
  //   }
  // });

  $('.btn-settings').click(function(){
    $('.popover').toggleClass("show");
  });
  $('.popover .close').click(function(){
    $('.popover').toggleClass("show");
  });

  $('.btn-reminder').click(function(){
    window.mySwiper.init();
    $('#reminder').toggleClass("show");
  });

  $('#reminder .close').click(function(){
    $('#reminder').toggleClass("show");
  });

});
