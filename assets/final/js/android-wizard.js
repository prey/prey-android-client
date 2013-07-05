var current = 0,
    start_page,
    total_pages;

var load_page = function(page){
  $("#central").load("wizard0" + page + "_div.html");
  current = page;
};

var toggle_page = function(dir){

  var target = current + dir;
  load_page(target);

  $("#btn_back_div").show();
  $("#btn_next_div").show();

  if (target <= start_page) { // first page
    $("#btn_back_div").hide();
  } else if (target >= total_pages) { // last page
    $("#btn_next_div").hide();
  }

  pointCircle(target);
};

var load_wizard = function(start, total){
  total_pages = total;
  start_page  = start;

  $("#btn_back_div").hide();
  load_page(start);

  $("#btn_next_div").click(function() {
    toggle_page(+1);
  });

  $("#btn_back_div").click(function() {
    toggle_page(-1);
  });
};
