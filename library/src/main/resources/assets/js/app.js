function deleteBook(id) {
var isbn = id;
    
    $.ajax({
        url: '/library/v1/books/' + isbn,
        dataType: "json",
        type: 'DELETE',
        success: function(data) {
                        alert('Deleted Book with ISBN ' + isbn);
                        window.location.reload();                        
                },
        error: function(xhr, status) {
                        alert("Sorry, there was a problem!");
                }
        });
}


function sendLost(id) {
    var isbn = id;
    
    $.ajax({
        url: '/library/v1/books/' + isbn + '/?status=lost',
        dataType: "json",
        type: 'PUT',
        success: function(data) {
                        alert('Reported lost on ISBN ' + isbn);
                        window.location.reload();
                },
        error: function(xhr, status) {
                        alert("Sorry, there was a problem!");
                }
        });
};	