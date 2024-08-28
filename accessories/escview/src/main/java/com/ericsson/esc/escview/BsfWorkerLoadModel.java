package com.ericsson.esc.escview;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class BsfWorkerLoadModel extends Thread {
    private boolean connected = false;
    
    private String kubeHostname;
    private int    kubePort;

    private       int    curNoOfPods = 0;
    private final Object podNoLock   = new Object();

    private class PodInfo {
        String name;
        String nameAbbrev;
        float  load;
    }

    private       PodInfo[] podInfo;
    private final Object    podInfoLock = new Object();

    public BsfWorkerLoadModel(String kubeHostname, int kubePort, int maxPods) {
        this.kubeHostname = kubeHostname;
        this.kubePort     = kubePort;
        this.podInfo      = new PodInfo[maxPods];
        for (int pod=0; pod < this.podInfo.length; pod++)
            podInfo[pod] = new PodInfo();
    }

    // create abbreviated name that fits bar chart legend
    public synchronized String getNameForPod(int podNum) {
        String name;

        synchronized(podInfoLock) {
            name = this.podInfo[podNum].nameAbbrev;
        }

        return name;
    }

    public synchronized float getLoadForPod(int podNum) {
        float load;

        synchronized(podInfoLock) {
            load = this.podInfo[podNum].load;
        }

        return load;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(500);

                URIBuilder builder = new URIBuilder("http://" + this.kubeHostname + ":" + this.kubePort + "/monitor/api/v0.1/commands");
                builder.setParameter("command", "cpu");

                http(builder.build());
                // http("http://10.210.52.30:31270/monitor/api/v0.1/commands?command=cpu", "");
            } catch (InterruptedException e) {
                System.err.println("Got interrupted.");
            } catch (URISyntaxException e) {
                System.err.println("Wrong URI syntax. FATAL: " + e);
                System.exit(1);
            }

            // for (int podNum=0; podNum < podInfo.length; podNum++)
            //    podInfo[podNum].load = (int) (Math.random() * 100);
        }
    }

    private HttpResponse http(java.net.URI url) {
        // System.out.println("GET " + url);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = new HttpGet(url);
            // StringEntity params = new StringEntity(body);
            // request.addHeader("content-type", "application/json");
            // request.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(request);

            if (!connected) {
                connected = true;

                System.out.println("Connected to app: eric-monitor on " + this.kubeHostname + " on port " + this.kubePort + ".");
            }

            String json = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            // System.out.println(json);


            try {
                JSONParser parser = new JSONParser();
                Object response = parser.parse(json);
                JSONObject responseObj =(JSONObject)response;

                Object results = responseObj.get("results");

                if (results instanceof JSONArray) {
                    JSONArray resultsArray = (JSONArray)results;

                    int pod = 0;

                    for (Object result : resultsArray) {
                        JSONObject resultObj =(JSONObject)result;

                        float  cpus    = Float.parseFloat(resultObj.get("available-processors").toString());
                        float  podLoad = Float.parseFloat(resultObj.get("system-load-average").toString());
                        String podName = resultObj.get("source").toString();

                        if (pod < podInfo.length) {
                            synchronized(podInfoLock) {
                                podInfo[pod].name       = podName;
                                podInfo[pod].nameAbbrev = abbreviated(podName);
                                podInfo[pod].load       = Math.max(podLoad * 100.0f / (cpus >= Float.MIN_VALUE ? cpus : 1.0f), 5.0f);

                                System.out.println(">>>> POD " + pod + ": " + podInfo[pod].name + " (" + podInfo[pod].nameAbbrev + "), load = " + podInfo[pod].load + " (avgload:" + podLoad + ", cpus:" + cpus + ")");
                            }

                            pod++;
                        }
                    }

                    int curNoOfPodsLocal;

                    synchronized(podNoLock) {
                        curNoOfPodsLocal = curNoOfPods;
                        curNoOfPods      = pod;
                    }

                    if (curNoOfPodsLocal != pod)
                        if (curNoOfPodsLocal == 0)
                            System.out.println("Displaying " + pod + " active BSF pod" + (pod > 1 ? "s" : "") + ".");
                        else
                            System.out.println("Number of pods changed from " + curNoOfPodsLocal + " to " + pod + ".");

                    for (; pod < podInfo.length; pod++)
                        synchronized(podInfoLock) {
                            podInfo[pod].name = podInfo[pod].nameAbbrev = "";
                            podInfo[pod].load = 0;
                        }
                } else {
                    System.err.println("\"results\" not a JSON array!");
                    System.exit(1);
                }

            } catch (Exception e) {
                System.err.println("Problem occured while parsing JSON result. IGNORED: " + e);
            }

        } catch (IOException ex) {
            System.err.println("Fatal I/O: " + ex);
            System.exit(1);
        }
        return null;
    }

    private String abbreviated(String podName) {
        if (podName.startsWith("eric-bsf-manager"))
            return "bsfmgr";
        else if (podName.length() >= 3)
            return "bsfwrk-" + podName.substring(podName.length() - 3, podName.length());
        else
            return "-";
    }
}
