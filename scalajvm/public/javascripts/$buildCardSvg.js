function buildCardSvg(cssClass, color, shape, pattern, count, selected){

  var W = 170,
      PW = W * 0.05,
      r2 = 0.3,
      r3 = 0.3,
      padding = 10,
      squareWidth = W*0.2,
      diameter = W*0.1,
      triangleSide = W*0.3;

  var shapes = [
    //square
    function(s,pos){
        var rect = s.rect(pos[0]-(squareWidth/2.0), pos[1]-(squareWidth/2.0), squareWidth, squareWidth);
        rect.scaledBy = 1;
        return rect
    },

    //typesafe
    function(s,pos){
        //first I'll build a square
        //var square = shapes[0](s,pos);

        //let's scale it down without fucking up the pattern
        var svg = s.svg(pos[0]-(squareWidth/2.0),pos[1]-(squareWidth/2.0),squareWidth, squareWidth, 0, 0, 154,154 );
        var p = svg.path("M49.7 114c-4.8 0-8.7-5.1-8.7-9.9v-.2c0-4.8 3.9-7.9 8.7-7.9h100c.9-3 1.6-7 2-11h-84c-4.8 0-8.7-3.7-8.7-8.5s3.9-8.5 8.7-8.5h83.7c-.5-4-1.2-8-2.3-11h-63.4c-4.8 0-8.7-3.7-8.7-8.5s3.9-8.5 8.7-8.5h56c-13.1-23-37.2-37.8-64.7-37.8-41.4 0-75 33.3-75 74.7s33.6 75.6 75 75.6c28.4 0 53.1-15.4 65.8-38.4h-93.1z");

        svg.scaledBy = squareWidth/154.0;

        return svg;
    },

    //triangle
    function(s,pos){
      var p0 = [pos[0], pos[1]-(W*0.1)],
          p1 = [pos[0]-(triangleSide/2.0), pos[1]+(W*0.1)],
          p2 = [pos[0]+(triangleSide/2.0), pos[1]+(W*0.1)];
      var p = s.path("M "+p0[0]+" "+p0[1]+ " L "+p1[0]+" "+p1[1] + " L "+p2[0]+" "+p2[1] +" z");
      p.scaledBy = 1;
      return p;
    },

    //circle
    function(s,pos){
      var c = s.circle(pos[0], pos[1], diameter);
      c.scaledBy = 1;
      return c;
    },

  ];

  var colors = ['red', 'green', 'blue', 'black'];

  var positions = [
    //0
    [[W/2.0, W/2.0]],
    //1
    [[W*r2, W*r2],[W*(1-r2),W*(1-r2)]],
    //2
    [[W/2.0, W*r2],[W*r3,W*(1-r3)],[W*(1-r3), W*(1-r3)]],
    //3
    [[W*r3, W*r2], [W*(1-r3), W*r2],[W*r3,W*(1-r3)],[W*(1-r3), W*(1-r3)]]
  ];


  //build it


  var s = Snap(W, W);
  var selecteds = {
    true: {fill: s.gradient("r(0.5, 0.5, 1.0)#fff-#888"), stroke: 'black', strokeWidth: 2},
    false: {fill: '#eee'}
  };
  s.rect(padding,padding,W-(2*padding),W-(2*padding), padding, padding).attr(selecteds[selected]);

  var patterns = [
    //fill
    function(el){ return el.attr({fill: colors[color]}); },
    //border
    function(el){ return el.attr({fill: 'transparent', stroke: colors[color], strokeWidth: 2/el.scaledBy});},
    //squares
    function(el){
        var padding=1 /el.scaledBy;
        var width=PW  /el.scaledBy;

        var pattern = s.rect(padding,padding, width-(2* padding), width-(2*padding))
            .attr({fill: colors[color]})
            .pattern(0,0,width,width);
        el.attr({fill: pattern});
    },
    //diagonal lines
    function(el){
        var width=PW*0.8 /el.scaledBy;
        var wh = width/2.0;
        var pattern = s.path("M 0 "+wh+" L "+wh+" 0 M "+wh+" "+width+" L "+width+" "+wh)
            .attr({strokeWidth: Math.sqrt(2*wh), stroke: colors[color], strokeLinecap: 'square'})
            .pattern(0,0,width,width);
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

  $('.'+cssClass).find('svg').remove();
  $(s.node).detach().appendTo($('.'+cssClass));
}