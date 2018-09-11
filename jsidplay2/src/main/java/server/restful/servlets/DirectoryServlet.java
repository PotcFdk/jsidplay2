package server.restful.servlets;

import static server.restful.JSIDPlay2Server.ROLE_ADMIN;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.util.URIUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import server.restful.common.ServletUtil;
import ui.entities.config.Configuration;

public class DirectoryServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String SERVLET_PATH_DIRECTORY = "/directory";

	private ServletUtil util;

	public DirectoryServlet(Configuration configuration, Properties directoryProperties) {
		this.util = new ServletUtil(configuration, directoryProperties);
	}

	/**
	 * Get directory contents containing music collections.
	 * 
	 * E.g.
	 * http://haendel.ddns.net:8080/jsidplay2service/JSIDPlay2REST/directory/C64Music/MUSICIANS
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String decodedPath = URIUtil.decodePath(request.getRequestURI());
		String filePath = decodedPath
				.substring(decodedPath.indexOf(SERVLET_PATH_DIRECTORY) + SERVLET_PATH_DIRECTORY.length());
		String filter = request.getParameter("filter");

		List<String> files = util.getDirectory(filePath, filter, request.isUserInRole(ROLE_ADMIN));

		response.setContentType(MimeTypes.Type.APPLICATION_JSON_UTF_8.asString());
		response.getWriter().println(new ObjectMapper().writer().writeValueAsString(files));
		response.setStatus(HttpServletResponse.SC_OK);
	}

}