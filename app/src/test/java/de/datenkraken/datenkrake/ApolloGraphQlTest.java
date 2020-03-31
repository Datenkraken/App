package de.datenkraken.datenkrake;

import android.content.Context;
import android.content.res.Resources;

import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import de.datenkraken.datenkrake.network.TaskDistributor;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloQuery;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class ApolloGraphQlTest {

    // Rule to pipe Timber output to system.out
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();

    @Mock
    private Context mockApplicationContext;
    @Mock
    private Resources mockContextResources;

    @Test
    public void testResponse() {
        MockitoAnnotations.initMocks(this);

        MockWebServer server = new MockWebServer();
        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/GraphQlResponses/response.txt")));
        } catch (IOException e) {
            assert false;
        }

        when(mockApplicationContext.getResources()).thenReturn(mockContextResources);
        when(mockContextResources.getString(anyInt())).thenReturn(server.url("/").toString());
        TaskDistributor.setup(mockApplicationContext);

        ApolloTaskTest testTask = new ApolloTaskTest();
        testTask.request();

        while(!testTask.done) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Assert.fail("This shouldn't happen!");
            }
        }

        List<String> data = Arrays.asList("Test", "run", "successful", "!");
        String end = "hoorray!";


        Assert.assertEquals(data, testTask.data);
        Assert.assertEquals(end, testTask.end);
    }

    class ApolloTaskTest extends ApolloQuery<TestQuery.Data> {

        public List<String> data;
        public String end;
        public boolean done = false;

        @Override
        public Query getQuery() {
            return TestQuery.builder().build();
        }

        @Override
        public void onResponse(@NotNull Response<TestQuery.Data> response) {
            data = response.data().test().data();
            end = response.data().test().end();
            done = true;
        }

        @Override
        public void onFailure(@NotNull ApolloException e) {
            Assert.fail("This shouldn't happen!");
        }
    }
}
