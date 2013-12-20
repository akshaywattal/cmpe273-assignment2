$(document).ready(function() {  
	if(window.WebSocket) {
        var client, destination;
        var url="ws://54.219.156.168:61623";
        var login = "admin";
        var passcode = "password"
        var libPort = location.port;

        if (libPort==8001)
        	{var destination = "/topic/41816.book.*";}
        else 
        	{var destination = "/topic/41816.book.computer";}	
              
        client = Stomp.client(url, "stomp");
        client.connect(login, passcode, function() {
            client.debug("connected to Stomp");
            //alert("connected");
            client.subscribe(destination, function(message) {
            	//alert(message);
            	$.ajax({
                    url: '/library/v1/books/update',
                    contentType: 'application/json',
                    type: 'POST',
                    data: message.body,
                    success: function(data) {
                                    window.location.reload();                        
                            },
                    error: function(xhr, status) {
                    				window.location.reload(); 
                            }
                    });
            });
          });
	}
});  
