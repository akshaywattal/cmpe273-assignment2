package edu.sjsu.cmpe.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.stomp.ApolloSTOMP;
import edu.sjsu.cmpe.library.stomp.LibraryListener;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
	new LibraryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");
	bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
    	
	/*
	 * Pulling Configurations from config file
	 */
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	log.debug("Queue name is {}. Topic name is {}", queueName, topicName);
	String apolloUser = configuration.getApolloUser();
	String apolloPassword = configuration.getApolloPassword();
	String apolloHost = configuration.getApolloHost();
	int apolloPort = configuration.getApolloPort();
	String libraryName = configuration.getLibraryName();
	
	/** Root API */
	environment.addResource(RootResource.class);
	
	/** Books APIs */
	BookRepositoryInterface bookRepository = new BookRepository();
	
	/*
	 * Adding the Messaging Layer and Declaring its instance
	 */
	ApolloSTOMP apolloSTOMP = new  ApolloSTOMP(apolloUser, apolloPassword, apolloHost, apolloPort, libraryName, queueName, topicName, bookRepository);
	
	environment.addResource(new BookResource(bookRepository, apolloSTOMP));

	/** UI Resources */
	environment.addResource(new HomeResource(bookRepository));	
	
	/** Library Listener Class */
	environment.addServerLifecycleListener(new LibraryListener(apolloSTOMP));
	
    }
}
