$(document).ready(function() {
   $('<span class="note">note</span>').insertBefore('note').on('touchstart', function(e) {
       e.stopPropagation();
       var note = $(this).next('note');
       var note_height = note.outerHeight();
       if ($(window).scrollTop() + note_height > note_height + 10) {
           var top_position = $(this).offset().top - note_height;
           note.offset({top: top_position});
       }
       //var top_position = $(this).offset().top - note_height;
       //note.offset({top: top_position});
       note.fadeIn('fast', function() {
           var height = note.height();
           $('body').css('margin-bottom' , '+=' + height);
       });
       $('body').on('touchstart', function() {
           var height = note.height();
           $('body').css('margin-bottom' , '-=' + height);
           note.fadeOut('fast');
           $('body').off('touchstart');
       });
   });
});