package dev.resteasy.grpc.test;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.utils.TestUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import dev.resteasy.example.grpc.greet.GreetServiceGrpc;
import dev.resteasy.example.grpc.greet.GreetServiceGrpc.GreetServiceBlockingStub;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralEntityMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.GeneralReturnMessage;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___GeneralGreeting;
import dev.resteasy.example.grpc.greet.Greet_proto.dev_resteasy_example_grpc_greet___Greeting;
import dev.resteasy.grpc.arrays.ArrayUtility;
import dev.resteasy.grpc.arrays.Array_proto.dev_resteasy_grpc_arrays___ArrayHolder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@RunWith(Arquillian.class)
@RunAsClient
public class GrpcToJakartaRESTTest {

    private static ManagedChannel channel;
    private static GreetServiceBlockingStub blockingStub;

    @Deployment
    static Archive<?> deploy() throws Exception {
        WebArchive war = TestUtil.prepareArchive(GrpcToJakartaRESTTest.class.getSimpleName());
        String version = System.getProperty("grpc.example.version", "1.0.1.Final-SNAPSHOT");
        war.merge(ShrinkWrap.createFromZipFile(WebArchive.class,
                TestUtil.resolveDependency("dev.resteasy.examples:grpcToRest.example.grpc:war:" + version)));
        WebArchive archive = (WebArchive) TestUtil.finishContainerPrepare(war, null, (Class<?>[]) null);
        // log.info(archive.toString(true));
        // archive.as(ZipExporter.class).exportTo(new File("/tmp/GrpcToJaxrs.jar"), true);
        return archive;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        channel = ManagedChannelBuilder.forTarget("localhost:9555").usePlaintext().build();
        blockingStub = GreetServiceGrpc.newBlockingStub(channel);
        Client client = ClientBuilder.newClient();
        Response response = client.target("http://localhost:8080/GrpcToJakartaRESTTest/grpcToJakartaRest/grpcserver/context")
                .request().get();
        Assert.assertEquals(200, response.getStatus());
        client.close();
    }

    @Test
    public void testGreeting() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/greet/Bill").build();
        try {
            GeneralReturnMessage grm = blockingStub.greet(gem);
            dev_resteasy_example_grpc_greet___Greeting greeting = grm.getDevResteasyExampleGrpcGreetGreetingField();
            Assert.assertEquals("hello, Bill", greeting.getS());
        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }

    @Test
    public void testGeneralGreeting() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        GeneralEntityMessage gem = builder.setURL("http://localhost:8080/salute/Bill?salute=Heyyy").build();
        try {
            GeneralReturnMessage grm = blockingStub.generalGreet(gem);
            dev_resteasy_example_grpc_greet___GeneralGreeting greeting = grm
                    .getDevResteasyExampleGrpcGreetGeneralGreetingField();
            Assert.assertEquals("Heyyy", greeting.getSalute());
            Assert.assertEquals("Bill", greeting.getS());

        } catch (StatusRuntimeException e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }

    @Test
    public void testArraysInts1() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        int[] is = new int[] { 1, 2, 3 };
        dev_resteasy_grpc_arrays___ArrayHolder ah1 = ArrayUtility.getHolder(is);
        GeneralEntityMessage gem = builder.setDevResteasyGrpcArraysDevResteasyGrpcArraysArrayHolderField(ah1).build();
        GeneralReturnMessage response;
        try {
            response = blockingStub.arrayOne(gem);
            dev_resteasy_grpc_arrays___ArrayHolder ah2 = response
                    .getDevResteasyGrpcArraysDevResteasyGrpcArraysArrayHolderField();
            System.out.println(ah2);
            Object array = ArrayUtility.getArray(ah2);
            Assert.assertArrayEquals(is, (int[]) array);
        } catch (Exception e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }

    @Test
    public void testArraysInts2() throws Exception {
        GeneralEntityMessage.Builder builder = GeneralEntityMessage.newBuilder();
        Integer[][] iis = new Integer[][] { { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) },
                { Integer.valueOf(4), Integer.valueOf(5) } };
        dev_resteasy_grpc_arrays___ArrayHolder ah1 = ArrayUtility.getHolder(iis);
        GeneralEntityMessage gem = builder.setDevResteasyGrpcArraysDevResteasyGrpcArraysArrayHolderField(ah1).build();
        GeneralReturnMessage response;
        try {
            response = blockingStub.arrayTwo(gem);
            dev_resteasy_grpc_arrays___ArrayHolder ah2 = response
                    .getDevResteasyGrpcArraysDevResteasyGrpcArraysArrayHolderField();
            System.out.println(ah2);
            Object array = ArrayUtility.getArray(ah2);
            Assert.assertArrayEquals(iis, (Integer[][]) array);
        } catch (Exception e) {
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                Assert.fail(writer.toString());
            }
        }
    }
}
