package edu.sjsu.cmpe.library.api.resources;

import java.net.URL;

import javax.jms.Connection;
import javax.jms.TextMessage;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;
import edu.sjsu.cmpe.library.dto.BookDto;
import edu.sjsu.cmpe.library.dto.BooksDto;
import edu.sjsu.cmpe.library.dto.LinkDto;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.stomp.ApolloSTOMP;

@Path("/v1/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {
    /** bookRepository instance */
    private final BookRepositoryInterface bookRepository;
    
    /** apolloSTOMP instance */
    private ApolloSTOMP apolloSTOMP; 
    
    /**
     * BookResource constructor
     * 
     * @param bookRepository
     *            a BookRepository instance
     * @param apolloSTOMP
     *            a ApolloSTOMP instance           
     *            
     */
    public BookResource(BookRepositoryInterface bookRepository, ApolloSTOMP apolloSTOMP) {
	this.bookRepository = bookRepository;
	this.apolloSTOMP = apolloSTOMP;
    }
    
    /*
     * API to view book based on ISBN
     */
    @GET
    @Path("/{isbn}")
    @Timed(name = "view-book")
    public BookDto getBookByIsbn(@PathParam("isbn") LongParam isbn) {
    	Book book = bookRepository.getBookByISBN(isbn.get());
	
    	BookDto bookResponse = new BookDto(book);
    	bookResponse.addLink(new LinkDto("view-book", "/books/" + book.getIsbn(), "GET"));
    	bookResponse.addLink(new LinkDto("update-book-status","/books/" + book.getIsbn(), "PUT"));
    	bookResponse.addLink(new LinkDto("delete-book","/books/" + book.getIsbn(), "DELETE"));
    	bookResponse.addLink(new LinkDto("view-all-books","/books/", "GET"));

	return bookResponse;
    }
    
    /*
     * API to create a new Book, NOT USED IN ASSIGNMENT -2 USE CASE
     */
    @POST
    @Timed(name = "create-book")
    public Response createBook(@Valid Book request) {
    	// Store the new book in the BookRepository so that we can retrieve it.
    	Book savedBook = bookRepository.saveBook(request);

    	String location = "/books/" + savedBook.getIsbn();
    	BookDto bookResponse = new BookDto(savedBook);
    	bookResponse.addLink(new LinkDto("view-book", location, "GET"));
    	bookResponse.addLink(new LinkDto("update-book-status", location, "PUT"));
    	bookResponse.addLink(new LinkDto("delete-book",location, "DELETE"));
    	bookResponse.addLink(new LinkDto("view-all-books","/books/", "GET"));

	return Response.status(201).entity(bookResponse).build();
    }
    
    /*
     * API to View all Books
     */
    @GET
    @Timed(name = "view-all-books")
    public BooksDto getAllBooks() {
	
    	BooksDto booksResponse = new BooksDto(bookRepository.getAllBooks());
    	booksResponse.addLink(new LinkDto("create-book", "/books", "POST"));

	return booksResponse;
    }
    
    /*
     * API to Update Book Status
     */
    @PUT
    @Path("/{isbn}")
    @Timed(name = "update-book-status")
    public Response updateBookStatus(@PathParam("isbn") LongParam isbn,
	    @DefaultValue("available") @QueryParam("status") Status status) throws Exception {
	
    	Book book = bookRepository.getBookByISBN(isbn.get());
    	
    	/*
    	 * Publish message to Queue if Book is lost (one message only per lost book)
    	 */
    	if (status.getValue()=="lost" && book.getStatus() == Status.available) {
    		book.setStatus(status);
    		Connection connect = apolloSTOMP.makeConnection();
    		apolloSTOMP.sendQueueMessage(connect,book.getIsbn());
    		apolloSTOMP.endConnection(connect);	
    	}
	
    	BookDto bookResponse = new BookDto(book);
    	String location = "/books/" + book.getIsbn();
    	bookResponse.addLink(new LinkDto("view-book", location, "GET"));
    	bookResponse.addLink(new LinkDto("delete-book",location, "DELETE"));
    	bookResponse.addLink(new LinkDto("view-all-books","/books/", "GET"));

	return Response.status(200).entity(bookResponse).build();
    }
    
    /*
     * API to Delete book
     */
    @DELETE
    @Path("/{isbn}")
    @Timed(name = "delete-book")
    public BookDto deleteBook(@PathParam("isbn") LongParam isbn) throws Exception {
    
    	bookRepository.delete(isbn.get());
    	BookDto bookResponse = new BookDto(null);
    	bookResponse.addLink(new LinkDto("create-book", "/books", "POST"));

	return bookResponse;
    }
    
    /*
     * API to update Library
     */
    @POST
    @Path("/update")
    @Timed(name = "update-library")
    public Response updateLibrary(@Valid String msg) throws Exception {
    	
    	String[] finalMessage=msg.split(":", 4); 
        Long isbn = Long.valueOf(finalMessage[0]);
        Status status = Status.available;

        Book book = bookRepository.getBookByISBN(isbn);
        
        /**If book received from Publisher is equal to lost book, update status*/
        if (book != null && book.getStatus()==Status.lost) {
        	book.setStatus(status);
        	System.out.println("Updated status as AVAILABLE for book with ISBN " + book.getIsbn());
        }
        
        /**If book received from Publisher is new book, add to hashmap*/
        else if (book == null){
        	String title = finalMessage[1];
        	String category = finalMessage[2];
        	URL coverImage = new URL(finalMessage[3]);
        	Book book1 = new Book();
        	book1.setIsbn(isbn);
        	book1.setTitle(title);
        	book1.setCategory(category);
        	book1.setCoverimage(coverImage);
        	bookRepository.saveBook(book1);
        	System.out.println("Added new book with ISBN " + book1.getIsbn() + " to library");
        }
        
        /**Book already present in library*/
        else {
        	System.out.println("Book with ISBN " + book.getIsbn() + " already AVAILABLE in Library");
        }

	return Response.ok().build();
    }
}

