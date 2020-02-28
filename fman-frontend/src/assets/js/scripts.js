function customScript() {

    // if ($('input[name="date"]').length) {
    //     var date_input = $('input[name="date"]'); //our date input has the name "date"
    //     var container = $('.bootstrap-iso form').length > 0 ? $('.bootstrap-iso form').parent() : "body";
    //     date_input.datepicker({
    //         format: 'mm/dd/yyyy',
    //         container: container,
    //         todayHighlight: true,
    //         autoclose: true
    //     });
    // }

    // if ($('#date').length) {
    //     var date_input = $('#date'); //our date input has the name "date"
    //     var container = $('.bootstrap-iso form').length > 0 ? $('.bootstrap-iso form').parent() : "body";
    //     date_input.datepicker({
    //         format: 'mm/dd/yyyy',
    //         container: container,
    //         todayHighlight: true,
    //         autoclose: true
    //     });
    // }

    if ($("#selectDeviceType").length) {
        $('#selectDeviceType').on('show.bs.modal', function (event) {
            var button = $(event.relatedTarget); // Button that triggered the modal
            var deviceid = button.data('deviceid'); // Extract info from data-* attributes
            // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
            // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.
            var modal = $(this);
            var device = modal.find('.modal-body input#device_id');

            //set device is
            device.val(deviceid);
            angular.element(device).triggerHandler('change'); //refresh the change
        });
        $('#selectDeviceType').on('hide.bs.modal', function (event) {
            $(".modal-body i").removeClass("on");
        });
    }  
    
    $('body').css('display', 'block');
};

function runBxSlider() {
    if ($('.scrolling-blocks').length) {
        $('.scrolling-blocks').bxSlider({
            slideWidth: 380,
            minSlides: 4,
            maxSlides: 4,
            moveSlides: 1,
            infiniteLoop: false,
            slideMargin: 10,
            controls: true,
            auto: false,
            pager: false
        });
    }
}

function changeClass(e){
    $(".modal-body .s-block i").removeClass("on");
    $(e).addClass("on");
}