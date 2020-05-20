package server.restful.servlets.whatssid;

import static server.restful.JSIDPlay2Server.CONTEXT_ROOT_SERVLET;
import static server.restful.JSIDPlay2Server.getEntityManager;

import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libsidutils.fingerprinting.rest.beans.SongNoBean;
import server.restful.common.JSIDPlay2Servlet;
import server.restful.common.ServletUtil;
import sidplay.fingerprinting.MusicInfoBean;
import ui.entities.config.Configuration;
import ui.entities.whatssid.service.WhatsSidService;

@SuppressWarnings("serial")
public class FindTuneServlet extends JSIDPlay2Servlet {

	public static final String FIND_TUNE_PATH = "/tune";

	@SuppressWarnings("unused")
	private ServletUtil util;

	public FindTuneServlet(Configuration configuration, Properties directoryProperties) {
		this.util = new ServletUtil(configuration, directoryProperties);
	}

	@Override
	public String getServletPath() {
		return CONTEXT_ROOT_SERVLET + FIND_TUNE_PATH;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		SongNoBean songNoBean = getInput(request, SongNoBean.class);

		EntityManager entityManager = getEntityManager();
		final WhatsSidService whatsSidService = new WhatsSidService(entityManager);
		MusicInfoBean musicInfoBean = whatsSidService.findTune(songNoBean);
		entityManager.clear();
		
		setOutput(request, response, musicInfoBean, MusicInfoBean.class);
	}
}
