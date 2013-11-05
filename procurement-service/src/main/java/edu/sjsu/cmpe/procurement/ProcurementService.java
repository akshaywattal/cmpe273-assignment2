package edu.sjsu.cmpe.procurement;

import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import de.spinscale.dropwizard.jobs.JobsBundle;
import edu.sjsu.cmpe.procurement.api.resource.ProcurementResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;

public class ProcurementService extends Service<ProcurementServiceConfiguration> {

    public static void main(String[] args) throws Exception {
	new ProcurementService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap) {
	bootstrap.setName("procurement-service");
	
	/*
	 * Added JobsBundle for Cron Jobs
	 */
	bootstrap.addBundle(new JobsBundle("edu.sjsu.cmpe.procurement"));
    }

    @Override
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception {
    	final Client client = new JerseyClientBuilder().using(configuration.getJerseyClientConfiguration())
                .using(environment)
                .build();
    	/*
    	 * Added Procurement Resource for Future Use - Not used in Assignment-2
    	 */
    	environment.addResource(new ProcurementResource(client));
    }
}
