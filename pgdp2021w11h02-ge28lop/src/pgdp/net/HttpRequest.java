package pgdp.net;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {
    private String firstLine, body;
    private HttpMethod httpMethod;
    private String path;
    private Map<String, String> map = new HashMap<String, String>();

    public HttpRequest(String firstLine, String body) {
        try {
            this.firstLine = firstLine;
            this.body = body;
            httpMethod = getMethod();
            path = getPath();
            map = getParameters();
        } catch (Exception e) {
            throw new InvalidRequestException("Invalid Request! " + e);
        }
    }

    public HttpMethod getMethod() {
        String result = null;
        if (firstLine.contains(" "))
            result = firstLine.substring(0, firstLine.indexOf(" "));
        // we may need an if statement that checks if result == null and then possibly throw an exception!
        return HttpMethod.valueOf(result);
    }

    public String getPath() {

        if (firstLine.contains("?")) {
            return firstLine.substring(firstLine.indexOf("/"), firstLine.indexOf("?"));
        }
        return firstLine.substring(firstLine.indexOf("/"), firstLine.indexOf("H") - 1);
    }

    public Map<String, String> getParameters() {
        Map<String, String> map = new HashMap<String, String>();
        if (firstLine.contains("?")) {
            String toHandle = firstLine.substring(firstLine.indexOf("?") + 1, firstLine.indexOf("H") - 1);
            // splitting all parameters in name and value
            String[] allParametersAndValues = toHandle.split("&");
            for (int i = 0; i < allParametersAndValues.length; ++i) {
                String[] parameterAndValuesSplitted = allParametersAndValues[i].split("=");
                map.put(URLDecoder.decode(parameterAndValuesSplitted[0]), URLDecoder.decode(parameterAndValuesSplitted[1]));
            }
        }

        // multiple parameters in body
        if (body.contains("&")) {
            String[] allParametersAndValues = body.split("&");
            for (int i = 0; i < allParametersAndValues.length; ++i) {
                String[] parameterAndValuesSplitted = allParametersAndValues[i].split("=");
                map.put(URLDecoder.decode(parameterAndValuesSplitted[0]), URLDecoder.decode(parameterAndValuesSplitted[1]));
            }
        }
        // only one parameter in body
        else {
            if (body.contains("=")) {
                String[] toHandle = body.split("=");
                map.put(URLDecoder.decode(toHandle[0]), URLDecoder.decode(toHandle[1]));
            }
        }
        return map;
    }

    //Test that it works
    /* public static void main(String[] args) {
         class ParametersAndValues {
             private String key, value;

             ParametersAndValues(String key, String value) {
                 this.key = key;
                 this.value = value;
             }

             String getKey() {
                 return key;
             }

             String getValue() {
                 return value;
             }
         }

         String firstLine = "POST /something?pathKey=pathValue HTTP/1.1";
         String body = "";

         HttpRequest req = new HttpRequest(firstLine, body);

         System.out.println(req.httpMethod);
         System.out.println(req.path);

         List<ParametersAndValues> l = req.map.entrySet().stream()
                 .map(x -> new ParametersAndValues(x.getKey(), x.getValue()))
                 .collect(Collectors.toList());

         for (int i = 0; i < l.size(); i++) {
             System.out.println(l.get(i).getKey());
             System.out.println((l.get(i).getValue()));
             System.out.println();
         }
     }*/
}


