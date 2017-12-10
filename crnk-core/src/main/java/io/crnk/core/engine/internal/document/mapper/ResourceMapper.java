package io.crnk.core.engine.internal.document.mapper;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.links.SelfLinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

public class ResourceMapper {

	private static final String SELF_FIELD_NAME = "self";

	private final String RELATED_FIELD_NAME = "related";

	private final ResourceFilterDirectory resourceFilterDirectory;

	private DocumentMapperUtil util;

	private boolean client;

	private ObjectMapper objectMapper;

	public ResourceMapper(DocumentMapperUtil util, boolean client, ObjectMapper objectMapper,
			ResourceFilterDirectory resourceFilterDirectory) {
		this.util = util;
		this.client = client;
		this.objectMapper = objectMapper;
		this.resourceFilterDirectory = resourceFilterDirectory;
	}

	public Resource toData(Object entity, QueryAdapter queryAdapter) {
		if (entity instanceof Resource) {
			// Resource and ResourceId
			return (Resource) entity;
		}
		else {
			// map resource objects
			Class<?> dataClass = entity.getClass();

			ResourceInformation resourceInformation = util.getResourceInformation(dataClass);

			Resource resource = new Resource();
			resource.setId(util.getIdString(entity, resourceInformation));
			resource.setType(resourceInformation.getResourceType());
			if (!client) {
				util.setLinks(resource, getResourceLinks(entity, resourceInformation));
				util.setMeta(resource, getResourceMeta(entity, resourceInformation));
			}
			setAttributes(resource, entity, resourceInformation, queryAdapter);
			setRelationships(resource, entity, resourceInformation, queryAdapter);
			return resource;
		}
	}

	private MetaInformation getResourceMeta(Object entity, ResourceInformation resourceInformation) {
		if (resourceInformation.getMetaField() != null) {
			return (MetaInformation) resourceInformation.getMetaField().getAccessor().getValue(entity);
		}
		return null;
	}

	public LinksInformation getResourceLinks(Object entity, ResourceInformation resourceInformation) {
		LinksInformation info;
		if (resourceInformation.getLinksField() != null) {
			info = (LinksInformation) resourceInformation.getLinksField().getAccessor().getValue(entity);
		}
		else {
			info = new DocumentMapperUtil.DefaultSelfRelatedLinksInformation();
		}
		if (info instanceof SelfLinksInformation) {
			SelfLinksInformation self = (SelfLinksInformation) info;
			if (self.getSelf() == null) {
				self.setSelf(util.getSelfUrl(resourceInformation, entity));
			}
		}
		return info;
	}

	protected void setAttributes(Resource resource, Object entity, ResourceInformation resourceInformation,
			QueryAdapter queryAdapter) {
		// fields legacy may further limit the number of fields
		List<ResourceField> fields = DocumentMapperUtil
				.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getAttributeFields(), false);

		// serialize the individual attributes
		for (ResourceField field : fields) {
			if (!isIgnored(field) && field.getAccess().isReadable()) {
				setAttribute(resource, field, entity);
			}
		}
	}

	protected boolean isIgnored(ResourceField field) { // NOSONAR signature is ok since protected
		return resourceFilterDirectory != null && resourceFilterDirectory.get(field, HttpMethod.GET) != FilterBehavior.NONE;
	}

	class ResourceBeanSerializer extends BeanSerializer {

		public ResourceBeanSerializer(JavaType type,
				BeanSerializerBuilder builder,
				BeanPropertyWriter[] properties,
				BeanPropertyWriter[] filteredProperties) {
			super(type, builder, properties, filteredProperties);
		}
	}

	public static void main(String[] args) throws Exception {

		ObjectMapper mapper = new ObjectMapper();


		Task task = new Task();
		task.name = "hello";


		JsonNode jsonNode = mapper.valueToTree(task);
		System.out.println(jsonNode);


		SerializerProvider serializerProvider = mapper.getSerializerProviderInstance();

		DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
		JavaType javaType = deserializationConfig.constructType(Task.class);

		TypeDeserializer typeDeserializer = deserializationConfig.findTypeDeserializer(javaType);
		System.out.println(typeDeserializer);

		JsonParser parser = mapper.treeAsTokens(jsonNode);

		DefaultDeserializationContext deserializationContext = (DefaultDeserializationContext) mapper.getDeserializationContext();
		DefaultDeserializationContext context = deserializationContext.createInstance(deserializationConfig, parser, null);
		BeanDeserializer deserializer = (BeanDeserializer) context.findRootValueDeserializer(javaType);

		System.out.println( parser.nextToken());
		System.out.println( parser.nextToken());
		System.out.println( parser.nextToken());

		//Object deserialize = deserializer.deserialize(parser, context);

		Iterator<SettableBeanProperty> iterator = deserializer.properties();
		while(iterator.hasNext()){
			SettableBeanProperty beanProp = iterator.next();
			Object deserialize = beanProp.deserialize(parser, context);
			System.out.println(deserialize);
		}



		System.out.println(deserializer);


		Task task1 = mapper.treeToValue(jsonNode, Task.class);
		System.out.println(task1);

		BeanSerializer serializer = (BeanSerializer) serializerProvider.findTypedValueSerializer(Task.class, true, null);
		Iterator<PropertyWriter> properties = serializer.properties();
		System.out.println(serializer + " " + properties);

		while (properties.hasNext()) {
			PropertyWriter next = properties.next();
			System.out.println(next);

			TokenBuffer buf = new TokenBuffer(mapper, false);


			next.serializeAsElement(task, buf, serializerProvider);

			JsonParser jsonParser = buf.asParser();
			TreeNode treeNode = jsonParser.readValueAsTree();
			System.out.println("treeNode: " + treeNode);
			System.out.println("treeNode: " + treeNode.getClass());

		}

	}

	public static class Task {

		public String name;
	}

	protected void setAttribute(Resource resource, ResourceField field, Object entity) {
		PropertyWriter propertyWriter = field.getPropertyWriter();
		JsonNode valueNode;
		if (propertyWriter != null) {
			try {
				SerializerProvider serializerProvider = objectMapper.getSerializerProviderInstance();
				TokenBuffer buf = new TokenBuffer(objectMapper, false);
				propertyWriter.serializeAsElement(entity, buf, serializerProvider);
				JsonParser jsonParser = buf.asParser();
				valueNode = jsonParser.readValueAsTree();
			}
			catch (Exception e) {
				throw new IllegalStateException("failed to serialize with Jackson", e);
			}
		}
		else {
			Object value = field.getAccessor().getValue(entity);
			valueNode = objectMapper.valueToTree(value);
		}

		resource.getAttributes().put(field.getJsonName(), valueNode);
	}

	protected void setRelationships(Resource resource, Object entity, ResourceInformation resourceInformation,
			QueryAdapter queryAdapter) {
		List<ResourceField> fields = DocumentMapperUtil
				.getRequestedFields(resourceInformation, queryAdapter, resourceInformation.getRelationshipFields(), true);
		for (ResourceField field : fields) {
			if (!isIgnored(field)) {
				setRelationship(resource, field, entity, resourceInformation, queryAdapter);
			}
		}
	}

	protected void setRelationship(Resource resource, ResourceField field, Object entity, ResourceInformation
			resourceInformation,
			QueryAdapter queryAdapter) {
		{ // NOSONAR signature is ok since protected
			SerializerUtil serializerUtil = DocumentMapperUtil.getSerializerUtil();

			ObjectNode relationshipLinks = objectMapper.createObjectNode();
			String selfUrl = util.getRelationshipLink(resourceInformation, entity, field, false);
			serializerUtil.serializeLink(objectMapper, relationshipLinks, SELF_FIELD_NAME, selfUrl);
			String relatedUrl = util.getRelationshipLink(resourceInformation, entity, field, true);
			serializerUtil.serializeLink(objectMapper, relationshipLinks, RELATED_FIELD_NAME, relatedUrl);

			Relationship relationship = new Relationship();
			relationship.setLinks(relationshipLinks);
			resource.getRelationships().put(field.getJsonName(), relationship);
		}
	}
}
