package pgdp.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PinguPlagWebServer {
	static int port = 80;
	private PinguTextCollection collection;
	private HtmlGenerator generator;
	private ServerSocket ss;

	public PinguPlagWebServer() throws IOException {
		collection = new PinguTextCollection();
		generator = new HtmlGenerator();
	}

	public static void main(String[] args) throws IOException {
		PinguPlagWebServer pinguPlagWebServer = new PinguPlagWebServer();
		pinguPlagWebServer.run();
	}

	public void run(){

		class Execution implements Runnable{
			private final Socket socket;

			Execution(Socket s){
				this.socket = s;
			}

			@Override
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String firstLine = br.readLine();
					String body = tryReadBody(br);
					HttpResponse httpResponse = handleRequest(firstLine, body);
					String result = httpResponse.toString();
					PrintWriter bw= new PrintWriter(socket.getOutputStream());
					bw.println(result);
					bw.flush();
					socket.close();
				}
				catch (Exception e){
					System.err.println("Something went wrong");
				}
			}
		}

		try {
			ss = new ServerSocket(port);
		}
		catch (Exception ignored){}

		while(!Thread.currentThread().isInterrupted()){
			try {
				Socket socket = ss.accept();
				Thread thread = new Thread(new Execution(socket));
				thread.start();
			}
			catch (Exception ignored){}
		}

	}

	HttpResponse handleRequest(String firstLine, String body) {
		String finalBody = "";
		if(body != null)
			finalBody = body;
		HttpRequest httpRequest = null;
		try {
			httpRequest = new HttpRequest(firstLine, finalBody);
		}
		catch (Exception e){
			return new HttpResponse(HttpStatus.BAD_REQUEST, "");
		}
		HttpMethod methodEnum = httpRequest.getMethod();
		String path = httpRequest.getPath();

		if(methodEnum == HttpMethod.GET && path.equals("/"))
			return handleStartPage(httpRequest);

		if(methodEnum == HttpMethod.POST && path.equals("/"))
			return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, "");

		if(path.startsWith("/texts/")){
			String[] splittedUp = path.split("/texts/");
			if(!isOnlyDigits(splittedUp[1]))
				return new HttpResponse(HttpStatus.NOT_FOUND, "");

			if(methodEnum == HttpMethod.POST)
				return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, "");

			return handleTextDetails(httpRequest);
		}

		if(methodEnum == HttpMethod.POST && path.equals("/texts"))
			return handleNewText(httpRequest);

		if(methodEnum == HttpMethod.GET && path.equals("/texts"))
			return new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED, "");

		return new HttpResponse(HttpStatus.NOT_FOUND, "");
	}

	HttpResponse handleStartPage(HttpRequest request) {
		List<PinguText> list = collection.getAll();
		String generateStart = generator.generateStartPage(list);
		return new HttpResponse(HttpStatus.OK, generateStart);
	}

	HttpResponse handleTextDetails(HttpRequest request) {
		String path = request.getPath();
		String[] splittedUp = path.split("/texts/");

		int idToHandle = Integer.parseInt(splittedUp[1]);

		Map<PinguText, Double> map= new HashMap<>();
		map = collection.findPlagiarismFor(idToHandle);

		PinguText pingu = collection.findById(idToHandle);

		return new HttpResponse(HttpStatus.OK, generator.generateTextDetailsPage(pingu, map));
	}

	HttpResponse handleNewText(HttpRequest request) {
		Map<String, String> map = new HashMap<>();
		map = request.getParameters();

		if(!map.containsKey("author") ||!map.containsKey("text") || !map.containsKey("title"))
			return new HttpResponse(HttpStatus.BAD_REQUEST, "");

		String author = map.get("author");
		String text = map.get("text");
		String title = map.get("title");

		PinguText toHandle = collection.add(title, author, text);
		long idOfPinguToHandle = toHandle.getId();

		return new HttpResponse(HttpStatus.SEE_OTHER, "", "/texts/"+ idOfPinguToHandle);
	}

	private boolean isOnlyDigits(String s){
		if(s.length() == 0)
			return false;
		for(int i = 0; i < s.length(); ++i){
			if(!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Tries to read a HTTP request body from the given {@link BufferedReader}.
	 * Returns null if no body was found. This method consumes all lines of the
	 * request, read the first line of the HTTP request before using this method.
	 */
	static String tryReadBody(BufferedReader br) throws IOException {
		String contentLengthPrefix = "Content-Length: ";
		int contentLength = -1;
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) {
				if (contentLength == -1)
					return null;
				char[] content = new char[contentLength];
				int read = br.read(content);
				if (read == -1)
					return null;
				if (read < content.length)
					content = Arrays.copyOf(content, read);
				return new String(content);
			}
			if (line.startsWith(contentLengthPrefix)) {
				try {
					contentLength = Integer.parseInt(line.substring(contentLengthPrefix.length()));
				} catch (@SuppressWarnings("unused") RuntimeException e) {
					// ignore and just continue
				}
			}
		}
		return null;
	}

	PinguTextCollection getPinguTextCollection(){
		return collection;
	}
}
