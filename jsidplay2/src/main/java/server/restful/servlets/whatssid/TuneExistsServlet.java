package server.restful.servlets.whatssid;

import static server.restful.JSIDPlay2Server.CONTEXT_ROOT_SERVLET;
import static server.restful.JSIDPlay2Server.whatsSidService;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import libsidutils.fingerprinting.rest.beans.MusicInfoBean;
import server.restful.common.JSIDPlay2Servlet;
import server.restful.common.ServletUtil;
import ui.entities.config.Configuration;

@SuppressWarnings("serial")
public class TuneExistsServlet extends JSIDPlay2Servlet {

	public static final String TUNE_EXISTS_PATH = "/tune-exists";

	@SuppressWarnings("unused")
	private ServletUtil util;

	public TuneExistsServlet(Configuration configuration, Properties directoryProperties) {
		this.util = new ServletUtil(configuration, directoryProperties);
	}

	@Override
	public String getServletPath() {
		return CONTEXT_ROOT_SERVLET + TUNE_EXISTS_PATH;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MusicInfoBean musicInfoBean = getInput(request, MusicInfoBean.class);

		Boolean exists = whatsSidService.tuneExists(musicInfoBean);

		setOutput(request, response, exists, Boolean.class);
	}
}