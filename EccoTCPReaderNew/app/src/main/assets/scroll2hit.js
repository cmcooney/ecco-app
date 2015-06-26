function scrollToHighlight(elem, pos)
                {
                    var y = elem.scrollTop;
                    y += Math.round((pos - y) * 0.3);
                    if (Math.abs(y-pos) < 2){
                        elem.scrollTop = pos;
                        return;
                    }
                    elem.scrollTop = y;
                    setTimeout(scrollToHighlight, 40, elem, pos);
                }
                function getOffset() {
                    var position = $('.highlight').eq(0).offset().top - 150;
                    $('body').animate({scrollTop: position});
                }