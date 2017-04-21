/* 2016-04 Edited by Luis Veiga and Joao Garcia */
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.auth.AWSStaticCredentialsProvider;

import java.util.ArrayList;
import java.util.Date;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class EC2LaunchMeasureCPU {

    static AmazonEC2      ec2;
    static AmazonCloudWatch cloudWatch;

    private static void init() throws Exception {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-west-2").withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }


    public static void main(String[] args) throws Exception {
        boolean startInstance = false;
        if (args.length < 1) {
            System.out.println("Missing argument <startInstance>. Exiting...");
            System.exit(1);
        } else {
            if (args[0].equals("1")) {
                startInstance = true;
            } else if (args[0].equals("0")) {
                startInstance = false;
            } else {
                System.out.println("Argument <startInstance> must be 0 or 1. Exiting...");
                System.exit(1);
            }
        }

        init();

        try {
            if (startInstance) {
                System.out.println("Starting a new instance.");
                RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

                runInstancesRequest.withImageId("ami-2355c943").withInstanceType("t2.micro").withMinCount(1)
                        .withMaxCount(1).withKeyName("CNV-sigma").withSecurityGroups("CNV-ssh+http");
                RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
                String newInstanceId = runInstancesResult.getReservation().getInstances().get(0).getInstanceId();
            }

            DescribeInstancesResult describeInstancesResult = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesResult.getReservations();
            Set<Instance> instances = new HashSet<Instance>();

            System.out.println("total reservations = " + reservations.size());
            for (Reservation reservation : reservations) {
                instances.addAll(reservation.getInstances());
            }
            System.out.println("total instances = " + instances.size());

            /* TODO total observation time in milliseconds */
            long offsetInMilliseconds = 1000 * 60 * 10;
            Dimension instanceDimension = new Dimension();
            instanceDimension.setName("InstanceId");
            List<Dimension> dims = new ArrayList<Dimension>();
            dims.add(instanceDimension);
            for (Instance instance : instances) {
                String name = instance.getInstanceId();
                String state = instance.getState().getName();
                if (state.equals("running")) {
                    System.out.println("running instance id = " + name);
                    instanceDimension.setValue(name);
                    GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                            .withStartTime(new Date(new Date().getTime() - offsetInMilliseconds))
                            .withNamespace("AWS/EC2").withPeriod(60).withMetricName("CPUUtilization")
                            .withStatistics("Average").withDimensions(instanceDimension).withEndTime(new Date());
                    GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch.getMetricStatistics(request);
                    List<Datapoint> datapoints = getMetricStatisticsResult.getDatapoints();
                    for (Datapoint dp : datapoints) {
                        System.out.println(" CPU utilization for instance " + name + " = " + dp.getAverage());
                    }
                } else {
                    System.out.println("instance id = " + name);
                }
                System.out.println("Instance State : " + state + ".");
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }
    }
}
