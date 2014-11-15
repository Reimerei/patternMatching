function $buildCardSvg(color, shape, pattern, count){

  var W = 300,
      PW = W * 0.05,
      r2 = 0.3,
      r3 = 0.3,
      squareWidth = W*0.2,
      diameter = W*0.1,
      triangleSide = W*0.3;

  var shapes = [
    //square
    function(s,pos){ return s.rect(pos[0]-(squareWidth/2.0), pos[1]-(squareWidth/2.0), squareWidth, squareWidth); },

    //circle
    function(s,pos){ return s.circle(pos[0], pos[1], diameter); },

    //triangle
    function(s,pos){
      var p0 = [pos[0], pos[1]-(W*0.1)],
          p1 = [pos[0]-(triangleSide/2.0), pos[1]+(W*0.1)],
          p2 = [pos[0]+(triangleSide/2.0), pos[1]+(W*0.1)];
      return s.path("M "+p0[0]+" "+p0[1]+ " L "+p1[0]+" "+p1[1] + " L "+p2[0]+" "+p2[1] +" z");}
  ];

  var colors = ['red', 'green', 'blue'];

  var positions = [
    //0
    [[W/2.0, W/2.0]],
    //1
    [[W*r2, W*r2],[W*(1-r2),W*(1-r2)]],
    //2
    [[W/2.0, W*r2],[W*r3,W*(1-r3)],[W*(1-r3), W*(1-r3)]]
  ];


  //build it

  var s = Snap(W, W);
  s.rect(0,0,W,W).attr({fill: '#EEE'});

  var patterns = [
    //fill
    function(el){ return el.attr({fill: colors[color]}); },
    //border
    function(el){ return el.attr({fill: 'transparent', stroke: colors[color], strokeWidth: 3});},
    //squares
    function(el){
        var padding=1;
        var pattern = s.rect(padding,padding, PW-(2* padding), PW-(2*padding))
            .attr({fill: colors[color]})
            .pattern(0,0,PW,PW);
        el.attr({fill: pattern});
    },
    //honeycomb
    /*function(el){ var a = s.path("M14 33L0 25L0 8L14 0L28 8L28 25L14 33L14 50");
                  var b = s.path("M14 0L14 17L0 25L0 42L14 50L28 42L28 25L14 17");
                    function pathattrs(p){ p.attr({fill: c}); }
                 pathattrs(a);
                 pathattrs(b);
                 var honeycomb = s.g(a,b).attr({
        fill: c,
        stroke: "#111",
        strokeWidth: 1
      }).pattern(0, 0, 28, 50);
                 el.attr({fill: honeycomb});
    },*/
  ];

  for(var i in positions[count])
    patterns[pattern]( shapes[shape](s, positions[count][i]) );

  return $(s.node).detach();
}