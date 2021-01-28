package web.mvc._1_3_3.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.example.demo.gson.JSON;

import web.mvc._1_3_3.service.Service;

@RestController
@RequestMapping("/api")
public class TestController {

	private Service service;

	public TestController(Service service) {
		this.service = service;
	}

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/request")
	public Map<String, Object> webRequest(WebRequest webRequest, NativeWebRequest nativeWebRequest) {
		log.info("{}", JSON.toJSONString(webRequest.getParameterMap()));
		return null;
	}

	@GetMapping("/native")
	public Map<String, Object> nativeRequest(NativeWebRequest nativeWebRequest) {
		log.info("{}", nativeWebRequest.getNativeRequest().getClass());
		RequestFacade requestFacade = nativeWebRequest.getNativeRequest(RequestFacade.class);
		log.info("{}", requestFacade.getLocalPort());
		return new HashMap<String, Object>() {
			{
				put("String", requestFacade.getLocalPort());
			}
		};
	}

	@GetMapping("/servlet")
	public Map<String, String[]> servletRequest(ServletRequest servletRequest, ServletResponse servletResponse) {
		return servletRequest.getParameterMap();
	}

	@GetMapping("/session")
	public Map<String, Object> session(HttpSession session) {
		if (session.getAttribute("id") != null) {
			session.setAttribute("id", "hh");
		}
		Map<String, Object> map = new HashMap<>();
		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String n = names.nextElement();
			map.put(n, session.getAttribute(n));
		}
		return map;
	}

	@GetMapping("/timeZone")
	public Object timeZone(TimeZone timeZone) {
		return JSON.toJSONString(timeZone.toZoneId());
	}

	@RequestMapping("/stream")
	public void stream(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] bytes = new byte[4096];
		int n = 0;
		StringBuilder builder = new StringBuilder();
		while ((n = inputStream.read(bytes)) != -1) {
			builder.append(new String(bytes, 0, n));
			outputStream.write(bytes, 0, n);
		}
		inputStream.close();

		outputStream.flush();
		outputStream.close();
	}

	@RequestMapping("/receiveRedirect")
	public void receiveRedirect(HttpServletRequest request) {
		log.info("{}", RequestContextUtils.getInputFlashMap(request).get("test"));
	}

	@GetMapping("/redirect")
	public void redirect(HttpServletRequest request, HttpServletResponse response) {
		try {
			FlashMap flashMap = new FlashMap();
			flashMap.put("test", 1);
			RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(flashMap, request, response);
			response.sendRedirect("/api/receiveRedirect");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			try (Writer writer = response.getWriter()) {
				writer.write(e.getMessage());
				writer.flush();
			} catch (IOException ioException) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 在执行请求之前，添加一些属性或执行一些方法,controller的每个方法都会执行
	 * 
	 * @param list
	 * @return
	 */
	@ModelAttribute(name = "attribute")
	public List<String> attribute(@ModelAttribute(name = "attribute2") List<String> list) {
		list.add("1");
		return list;
	}

	@ModelAttribute(name = "attribute2")
	public List<String> attribute2(@RequestBody List<String> list) {
		list.add("2");
		return list;
	}

	@PostMapping("/getAttribute")
	public ResponseEntity<List<String>> getAttribute(@ModelAttribute(name = "attribute2") List<String> list) {
		return ResponseEntity.ok().body(list);
	}

	/**
	 * 在执行请求之前，添加一些属性或执行一些方法,controller的每个方法都会执行,有多个url时，如果参数类型等不同，可能会导致错误
	 * 
	 * @param map
	 * @return
	 */
//	@PostMapping("/testAttribute")
//	public Map<String, Object> testAttribute(@RequestBody Map<String, Object> map) {
//		return map;
//	}
}
