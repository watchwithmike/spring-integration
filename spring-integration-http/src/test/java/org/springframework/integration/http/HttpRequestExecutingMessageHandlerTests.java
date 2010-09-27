/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.http;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.transform.Source;

import org.junit.Test;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.Message;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 */
public class HttpRequestExecutingMessageHandlerTests {
	
	@Test
	public void simpleStringKeyStringValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, String> form = new LinkedHashMap<String, String>();
		form.put("a", "1");
		form.put("b", "2");
		form.put("c", "3");
		Message<?> message = MessageBuilder.withPayload(form).build();
		QueueChannel replyChannel = new QueueChannel();
		handler.setOutputChannel(replyChannel);
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		assertEquals("1", map.get("a").iterator().next());
		assertEquals("2", map.get("b").iterator().next());
		assertEquals("3", map.get("c").iterator().next());
		assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getHeaders().getContentType());
	}
	
	@Test
	public void simpleStringKeyObjectValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		form.put("a", new City("Philadelphia"));
		form.put("b", new City("Ambler"));
		form.put("c", new City("Mohnton"));
		Message<?> message = MessageBuilder.withPayload(form).build();
		QueueChannel replyChannel = new QueueChannel();
		handler.setOutputChannel(replyChannel);
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		assertEquals("Philadelphia", map.get("a").get(0).toString());
		assertEquals("Ambler", map.get("b").get(0).toString());
		assertEquals("Mohnton", map.get("c").get(0).toString());
		assertEquals(MediaType.MULTIPART_FORM_DATA, request.getHeaders().getContentType());
	}
	
	@Test
	public void simpleObjectKeyObjectValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<Object, Object> form = new LinkedHashMap<Object, Object>();
		form.put(1, new City("Philadelphia"));
		form.put(2, new City("Ambler"));
		form.put(3, new City("Mohnton"));
		Message<?> message = MessageBuilder.withPayload(form).build();
		QueueChannel replyChannel = new QueueChannel();
		handler.setOutputChannel(replyChannel);
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof Map<?, ?>);
		Map<?, ?> map = (Map <?, ?>) body;
		assertEquals("Philadelphia", map.get(1).toString());
		assertEquals("Ambler", map.get(2).toString());
		assertEquals("Mohnton", map.get(3).toString());
		assertEquals("application", request.getHeaders().getContentType().getType());
		assertEquals("x-java-serialized-object", request.getHeaders().getContentType().getSubtype());
	}

	@Test
	public void stringKeyStringArrayValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		form.put("a", new String[] { "1", "2", "3" });
		form.put("b", "4");
		form.put("c", new String[] { "5" });
		form.put("d", "6");
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		List<?> aValue = map.get("a");
		assertEquals(3, aValue.size());
		assertEquals("1", aValue.get(0));
		assertEquals("2", aValue.get(1));
		assertEquals("3", aValue.get(2));
		
		List<?> bValue = map.get("b");
		assertEquals(1, bValue.size());
		assertEquals("4", bValue.get(0));

		List<?> cValue = map.get("c");
		assertEquals(1, cValue.size());
		assertEquals("5", cValue.get(0));
		
		List<?> dValue = map.get("d");
		assertEquals(1, dValue.size());
		assertEquals("6", dValue.get(0));
		assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getHeaders().getContentType());
	}
	
	@Test
	public void stringKeyPrimitiveArrayValueMixedFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		form.put("a", new int[]{1,2,3});
		form.put("b", "4");
		form.put("c", new String[] { "5" });
		form.put("d", "6");
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		List<?> aValue = map.get("a");
		assertEquals(1, aValue.size());
		Object value = aValue.get(0);
		assertTrue(value.getClass().isArray());
		int[] y = (int[]) value;
		assertEquals(1, y[0]);
		assertEquals(2, y[1]);
		assertEquals(3, y[2]);
		
		List<?> bValue = map.get("b");
		assertEquals(1, bValue.size());
		assertEquals("4", bValue.get(0));

		List<?> cValue = map.get("c");
		assertEquals(1, cValue.size());
		assertEquals("5", cValue.get(0));
		
		List<?> dValue = map.get("d");
		assertEquals(1, dValue.size());
		assertEquals("6", dValue.get(0));
		assertEquals(MediaType.MULTIPART_FORM_DATA, request.getHeaders().getContentType());
	}
	@Test
	public void stringKeyNullArrayValueMixedFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		form.put("a", new Object[]{null, 4, null});
		form.put("b", "4");
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		List<?> aValue = map.get("a");
		assertEquals(3, aValue.size());
		assertNull(aValue.get(0));
		assertEquals(4, aValue.get(1));
		assertNull(aValue.get(2));
		
		List<?> bValue = map.get("b");
		assertEquals(1, bValue.size());
		assertEquals("4", bValue.get(0));

		assertEquals(MediaType.MULTIPART_FORM_DATA, request.getHeaders().getContentType());
	}
	/**
	 * This test and the one below might look identical, but they are not.
	 * This test injected "5" into the list as String resulting in 
	 * the Content-TYpe being application/x-www-form-urlencoded 
	 * @throws Exception
	 */
	@Test
	public void stringKeyNullCollectionValueMixedFormDataString() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		List<Object> list = new ArrayList<Object>();
		list.add(null);
		list.add("5");
		list.add(null);
		form.put("a", list);
		form.put("b", "4");
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		List<?> aValue = map.get("a");
		assertEquals(3, aValue.size());
		assertNull(aValue.get(0));
		assertEquals("5", aValue.get(1));
		assertNull(aValue.get(2));
		
		List<?> bValue = map.get("b");
		assertEquals(1, bValue.size());
		assertEquals("4", bValue.get(0));

		assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getHeaders().getContentType());
	}
	/**
	 * This test and the one above might look identical, but they are not.
	 * This test injected 5 into the list as int resulting in 
	 * Content-type being multipart/form-data
	 * @throws Exception
	 */
	@Test
	public void stringKeyNullCollectionValueMixedFormDataObject() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		List<Object> list = new ArrayList<Object>();
		list.add(null);
		list.add(5);
		list.add(null);
		form.put("a", list);
		form.put("b", "4");
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		List<?> aValue = map.get("a");
		assertEquals(3, aValue.size());
		assertNull(aValue.get(0));
		assertEquals(5, aValue.get(1));
		assertNull(aValue.get(2));
		
		List<?> bValue = map.get("b");
		assertEquals(1, bValue.size());
		assertEquals("4", bValue.get(0));

		assertEquals(MediaType.MULTIPART_FORM_DATA, request.getHeaders().getContentType());
	}

	@Test
	public void stringKeyStringCollectionValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		List<String> listA = new ArrayList<String>();
		listA.add("1");
		listA.add("2");
		form.put("a", listA);
		form.put("b", Collections.EMPTY_LIST);
		form.put("c", Collections.singletonList("3"));
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		
		List<?> aValue = map.get("a");
		assertEquals(2, aValue.size());
		assertEquals("1", aValue.get(0));
		assertEquals("2", aValue.get(1));
		
		List<?> bValue = map.get("b");
		assertEquals(0, bValue.size());
		
		List<?> cValue = map.get("c");
		assertEquals(1, cValue.size());
		assertEquals("3", cValue.get(0));
		
		assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getHeaders().getContentType());
	}
	
	@Test
	public void stringKeyObjectCollectionValueFormData() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		List<Object> listA = new ArrayList<Object>();
		listA.add(new City("Philadelphia"));
		listA.add(new City("Ambler"));
		form.put("a", listA);
		form.put("b", Collections.EMPTY_LIST);
		form.put("c", Collections.singletonList(new City("Mohnton")));
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap <?, ?>) body;
		
		
		List<?> aValue = map.get("a");
		assertEquals(2, aValue.size());
		assertEquals("Philadelphia", aValue.get(0).toString());
		assertEquals("Ambler", aValue.get(1).toString());
		
		List<?> bValue = map.get("b");
		assertEquals(0, bValue.size());
		
		List<?> cValue = map.get("c");
		assertEquals(1, cValue.size());
		assertEquals("Mohnton", cValue.get(0).toString());
		
		assertEquals(MediaType.MULTIPART_FORM_DATA, request.getHeaders().getContentType());
	}

	@Test
	public void nameOnlyWithNullValues() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		Map<String, Object> form = new LinkedHashMap<String, Object>();
		form.put("a", null);
		form.put("b", "foo");
		form.put("c", null);
		Message<?> message = MessageBuilder.withPayload(form).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof MultiValueMap<?, ?>);
		MultiValueMap<?, ?> map = (MultiValueMap<?, ?>) body;
		assertTrue(map.containsKey("a"));
		assertTrue(map.get("a").size() == 1);
		assertNull(map.get("a").get(0));
		List<?> entryB = map.get("b");
		assertEquals("foo", entryB.get(0));
		assertTrue(map.containsKey("c"));
		assertTrue(map.get("c").size() == 1);
		assertNull(map.get("c").get(0));
		assertEquals(MediaType.APPLICATION_FORM_URLENCODED, request.getHeaders().getContentType());
	}
	
	@SuppressWarnings("cast")
	@Test
	public void contentAsByteArray() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		
		byte[] bytes = "Hello World".getBytes();
		Message<?> message = MessageBuilder.withPayload(bytes).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof byte[]);
		assertEquals("Hello World", new String((byte[])bytes));
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, request.getHeaders().getContentType());
	}
	
	@Test
	public void contentAsXmlSource() throws Exception {
		HttpRequestExecutingMessageHandler handler = new HttpRequestExecutingMessageHandler("http://www.springsource.org/spring-integration");
		MockRestTemplate template = new MockRestTemplate();
		new DirectFieldAccessor(handler).setPropertyValue("restTemplate", template);
		handler.setHttpMethod(HttpMethod.POST);
		
		Message<?> message = MessageBuilder.withPayload(mock(Source.class)).build();
		Exception exception = null;
		try {
			handler.handleMessage(message);
		}
		catch (Exception e) {
			exception = e;
		}
		assertEquals("intentional", exception.getCause().getMessage());
		HttpEntity<?> request = template.lastRequestEntity.get();
		Object body = request.getBody();
		assertTrue(body instanceof Source);
		assertEquals(MediaType.TEXT_XML, request.getHeaders().getContentType());
	}
	
	public static class City{
		private String name;
		public City(String name){
			this.name = name;
		}
		public String toString(){
			return name;
		}
	}

	private static class MockRestTemplate extends RestTemplate {

		private final AtomicReference<HttpEntity<?>> lastRequestEntity = new AtomicReference<HttpEntity<?>>();

		@Override
		public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
				Class<T> responseType, Map<String, ?> uriVariables) throws RestClientException {
			this.lastRequestEntity.set(requestEntity);
			throw new RuntimeException("intentional");
		}
	}
}
