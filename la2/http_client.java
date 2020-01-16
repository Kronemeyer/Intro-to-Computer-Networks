
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class http_client {

	public static void main(String[] args) {
		try {
			PrintWriter output = new PrintWriter("http_client_output");
			/* establish connection and check for redirect */
			URL url = new URL(args[0]);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			int code = connection.getResponseCode();
			String redirect = "";
			while ((code >= 300 && code <= 303) || code == 307 || code == 308) {
				redirect = connection.getHeaderField("Location");
				url = new URL(redirect);
				connection = (HttpURLConnection) url.openConnection();
				code = connection.getResponseCode();
			}
			if (!redirect.isEmpty()) {
				output.println("URL redirected to " + redirect);
			}

			output.println("Printing HTTP header info from " + connection.getURL());

			/* lets go get the headers */
			Map<String, List<String>> headers = connection.getHeaderFields();
			headers.forEach((name, header) -> output.println(name + " " + header));
			output.println();

			/* lets go get the url content */
			InputStream urlContent = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlContent));
			String line;
			output.println("URL Content...");
			while ((line = reader.readLine()) != null) {
				output.println(line);
			}
			output.close();
		} catch (Exception i) {
			System.out.println(i);
		}
	}

}
