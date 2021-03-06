package server.restful.servlets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static libsidutils.ZipFileUtils.convertStreamToString;
import static server.restful.JSIDPlay2Server.CONTEXT_ROOT;
import static server.restful.common.ContentTypeAndFileExtensions.MIME_TYPE_HTML;
import static server.restful.common.ContentTypeAndFileExtensions.MIME_TYPE_TEXT;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libsidplay.sidtune.SidTune;
import server.restful.common.Connectors;
import server.restful.common.JSIDPlay2Servlet;
import ui.entities.config.Configuration;
import ui.entities.config.EmulationSection;

@SuppressWarnings("serial")
public class StartPageServlet extends JSIDPlay2Servlet {

	public StartPageServlet(Configuration configuration, Properties directoryProperties) {
		super(configuration, directoryProperties);
	}

	@Override
	public String getServletPath() {
		return CONTEXT_ROOT + "/";
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		EmulationSection emulationSection = configuration.getEmulationSection();

		Connectors appServerConnectors = emulationSection.getAppServerConnectors();
		String preferredProtocol = appServerConnectors.getPreferredProtocol();
		String hostname = InetAddress.getLocalHost().getHostAddress();
		String portNum = String.valueOf(appServerConnectors.getPreferredPortNum());
		Map<String, String> replacements = new HashMap<>();
		replacements.put("https://haendel.ddns.net:8443", preferredProtocol + "://" + hostname + ":" + portNum);

		try (InputStream is = SidTune.class.getResourceAsStream("/doc/restful.html")) {
			if (is != null) {
				response.setContentType(MIME_TYPE_HTML.toString());
				response.getWriter().println(is != null ? convertStreamToString(is, UTF_8.name(), replacements) : "");
			} else {
				response.setContentType(MIME_TYPE_TEXT.toString());
				new PrintStream(response.getOutputStream()).print("File not found: /doc/restful.html");
			}
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

}
