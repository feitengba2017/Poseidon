$(function(){
	$(document).on('input','#name',function(){
		$('#name_error').show();
	})
	$(document).on('input','#pass',function(){
		$('#pass_error').show();
	})
})