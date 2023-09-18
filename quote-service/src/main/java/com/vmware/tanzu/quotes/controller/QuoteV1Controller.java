package com.vmware.tanzu.quotes.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.tanzu.quotes.domain.Quote;
import com.vmware.tanzu.quotes.exception.SymbolNotFoundException;
import com.vmware.tanzu.quotes.service.QuoteService;

/**
 * Rest Controller providing the REST API for the Quote Service. Provides two
 * calls (both HTTP GET methods): - /quote/{symbol} - Retrieves the current
 * quote for a given symbol. - /company/{name} - Retrieves a list of company
 * information for companies that match the {name}.
 * 
 * @author David Ferreira Pinto
 *
 */
@RestController
@RequestMapping(value = "/v1")
public class QuoteV1Controller {
	private static final Logger logger = LoggerFactory.getLogger(QuoteV1Controller.class);

	/**
	 * The service to delegate calls to.
	 */
	@Autowired
	private QuoteService service;

	/**
	 * Retrieves the current quotes for the given symbols.
	 * 
	 * @param query
	 *            request parameter with q=symbol,symbol
	 * @return The Quote
	 * @throws SymbolNotFoundException
	 *             if the symbol is not valid.
	 */
	@RequestMapping(value = "/quotes", method = RequestMethod.GET)
	public ResponseEntity<List<Quote>> getQuotes(@RequestParam(value="q", required=false) String query) throws SymbolNotFoundException{
		logger.debug("received Quote query for: "+ query);
		if (query == null) {
			//return empty list.
			return new ResponseEntity<List<Quote>>(new ArrayList<Quote>(), getNoCacheHeaders(), HttpStatus.OK);
		}
		List<Quote> quotes;
		String[] splitQuery = query.split(",");
		if (splitQuery.length > 1) {
			quotes = service.getQuotes(query);

		} else {
			quotes = new ArrayList<>();
			quotes.add(service.getQuote(splitQuery[0]));
		}
		logger.info(String.format("Retrieved symbols: %s with quotes {}", query, quotes));
		return new ResponseEntity<List<Quote>>(quotes, getNoCacheHeaders(), HttpStatus.OK);
	}

	/**
	 * Generates HttpHeaders that have the no-cache set.
	 * 
	 * @return HttpHeaders.
	 */
	private HttpHeaders getNoCacheHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}

	/**
	 * Handles the response to the client if there is any exception during the
	 * processing of HTTP requests.
	 * 
	 * @param e
	 *            The exception thrown during the processing of the request.
	 * @param response
	 *            The HttpResponse object.
	 * @throws IOException
	 */
	@ExceptionHandler({ Exception.class })
	public void handleException(Exception e, HttpServletResponse response) throws IOException {
		logger.warn("Handle Error: " + e.getMessage());
		logger.warn("Exception:", e);
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR: " + e.getMessage());
		// return "ERROR: " + e.getMessage();
	}
}
