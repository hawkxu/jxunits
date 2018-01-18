package win.zqxu.jxunits.jpa;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import win.zqxu.jxunits.jre.XDefaultObjectCreator;
import win.zqxu.jxunits.jre.XObjectUtils;

/**
 * utility class for JPA
 * 
 * @author zqxu
 */
public class XEntityUtils {
  /**
   * Determine whether the object is an entity. an object considered as entity if its
   * class with @Entity annotation. Always returns false for null.
   * 
   * @param em
   *          the entity manager
   * @param object
   *          the object
   * @return true if the object is an entity
   */
  public static boolean isEntity(EntityManager em, Object object) {
    return object != null && isEntityClass(em, object.getClass());
  }

  /**
   * check if the object is an entity
   * 
   * @param em
   *          the entity manager
   * @param object
   *          the object
   * @throws IllegalArgumentException
   *           if the object is null or not an entity
   * @see #checkEntityClass(EntityManager, Class)
   */
  public static void checkEntity(EntityManager em, Object object) {
    checkEntityClass(em, object == null ? null : object.getClass());
  }

  /**
   * Determine whether the class is an entity class. an class with @Entity annotation
   * considered as entity class. Always returns false for null.
   * 
   * @param em
   *          the entity manager
   * @param entityClass
   *          the entity class
   * @return true if the class is an entity class
   */
  public static boolean isEntityClass(EntityManager em, Class<?> entityClass) {
    return entityClass != null && em.getMetamodel().entity(entityClass) != null;
  }

  /**
   * check if the class is entity class, see {@link #isEntityClass(EntityManager, Class)}
   * 
   * @param em
   *          the entity manager
   * @param entityClass
   *          the entity class
   * @throws IllegalArgumentException
   *           if the class is null or not entity class
   */
  public static void checkEntityClass(EntityManager em, Class<?> entityClass) {
    if (!isEntityClass(em, entityClass))
      throw new IllegalArgumentException(entityClass + " isn't entity class");
  }

  /**
   * get entity name for the entity class
   * 
   * @param em
   *          the entity manager
   * @param entityClass
   *          the entity class
   * @return entity name for the entity class
   * @throws IllegalArgumentException
   *           if the entityClass is null or not an entity class
   * @see #isEntityClass(EntityManager, Class)
   */
  public static String getEntityName(EntityManager em, Class<?> entityClass) {
    checkEntityClass(em, entityClass);
    return em.getMetamodel().entity(entityClass).getName();
  }

  /**
   * Get the schema name for the entity, null for default schema
   * 
   * @param entityClass
   *          the entity class
   * @return schema name
   */
  public static String getSchemaName(Class<?> entityClass) {
    Table anno = entityClass.getDeclaredAnnotation(Table.class);
    return anno == null || anno.schema().isEmpty() ? null : anno.schema();
  }

  /**
   * Get the underlying table name linked to the entity class
   * 
   * @param entityClass
   *          the entity class
   * @return the underlying table name linked to the entity class
   */
  public static String getTableName(Class<?> entityClass) {
    Table anno = entityClass.getDeclaredAnnotation(Table.class);
    String table = anno == null ? null : anno.name();
    return XObjectUtils.isEmpty(table) ? entityClass.getSimpleName() : table;
  }

  /**
   * get column name for the entity attribute
   * 
   * @param em
   *          the entity manager
   * @param entityClass
   *          the entity class
   * @param attribute
   *          the attribute name
   * @return column name for the entity attribute
   * @throws IllegalArgumentException
   *           if the class is not an entity class or the attribute is not exists
   */
  public static String getColumnName(EntityManager em, Class<?> entityClass, String attribute) {
    checkEntityClass(em, entityClass);
    EntityType<?> type = em.getMetamodel().entity(entityClass);
    Attribute<?, ?> attr = type.getAttribute(attribute);
    if (attr == null)
      throw new IllegalArgumentException("There is no attribute "
          + attribute + " in class " + entityClass.getName());
    return getColumnName(attr.getJavaMember());
  }

  /**
   * get column name for the entity member, the caller must be ensured that the member is
   * mapped to an entity attribute
   * 
   * @param member
   *          java member for the entity attribute
   * @return column name for the entity member
   */
  public static String getColumnName(Member member) {
    if (member instanceof Field) {
      Column anno = ((Field) member).getAnnotation(Column.class);
      String column = anno == null ? "" : anno.name();
      return !column.isEmpty() ? column : member.getName();
    } else if (member instanceof Method) {
      Column anno = ((Method) member).getAnnotation(Column.class);
      String column = anno == null ? "" : anno.name();
      return !column.isEmpty() ? column
          : Introspector.decapitalize(member.getName().substring(3));
    } else
      throw new IllegalArgumentException(member.getClass().getName()
          + " is not a valid entity attribute member type");
  }

  /**
   * Get primary key object of the entity, null for null entity
   * 
   * @param em
   *          the entity manager
   * @param entity
   *          the entity to get primary key
   * @return primary key object, null for null entity
   */
  public static Object getPrimaryKey(EntityManager em, Object entity) {
    if (entity == null) return null;
    EntityType<?> type = em.getMetamodel().entity(entity.getClass());
    SingularAttribute<?, ?>[] ids = getEntityIds(type);
    if (type.hasSingleIdAttribute()) {
      return getAttributeValue(entity, ids[0]);
    } else {
      return extractIdClassObject(entity, type, ids);
    }
  }

  private static Object extractIdClassObject(Object entity, EntityType<?> type,
      SingularAttribute<?, ?>[] ids) {
    Class<?> idClass = type.getIdType().getJavaType();
    try {
      Object idValue = XDefaultObjectCreator.getInstance().create(idClass);
      for (SingularAttribute<?, ?> id : ids) {
        Member source = id.getJavaMember();
        Member target = getIdMember(idClass, source);
        Object value = XObjectUtils.getAttributeValue(entity, source);
        XObjectUtils.setAttributeValue(idValue, target, value);
      }
      return idValue;
    } catch (ReflectiveOperationException ex) {
      throw new UnsupportedOperationException("extract id class object", ex);
    }
  }

  private static Member getIdMember(Class<?> idClass, Member source) {
    try {
      if (source instanceof Field)
        return idClass.getDeclaredField(source.getName());
      else
        return idClass.getDeclaredMethod(source.getName());
    } catch (Exception ex) {
      return getIdMember(idClass.getSuperclass(), source);
    }
  }

  /**
   * Get all id attributes of the entity type
   * 
   * @param type
   *          the entity type
   * @return all id attributes of the entity type
   */
  public static SingularAttribute<?, ?>[] getEntityIds(EntityType<?> type) {
    if (type.hasSingleIdAttribute()) {
      for (SingularAttribute<?, ?> attr : type.getSingularAttributes()) {
        if (attr.isId()) return new SingularAttribute<?, ?>[]{attr};
      }
    }
    return type.getIdClassAttributes().toArray(new SingularAttribute<?, ?>[0]);
  }

  /**
   * Get attribute value of the entity, returns null for null entity
   * 
   * @param em
   *          the entity manager
   * @param entity
   *          the entity
   * @param attribute
   *          the attribute name
   * @return attribute value of the entity
   * @throws IllegalArgumentException
   *           if attribute of the given name is not present in the entity
   * @throws UnsupportedOperationException
   *           if get the attribute value failed
   */
  public static Object getAttributeValue(EntityManager em, Object entity, String attribute) {
    if (entity == null) return null;
    EntityType<?> type = em.getMetamodel().entity(entity.getClass());
    return getAttributeValue(entity, type.getAttribute(attribute));
  }

  /**
   * Get attribute value of the entity, returns null for null entity
   * 
   * @param entity
   *          the entity
   * @param attribute
   *          the attribute
   * @return attribute value of the entity
   * @throws NullPointerException
   *           if the attribute is null
   * @throws UnsupportedOperationException
   *           if get attribute value failed
   */
  public static Object getAttributeValue(Object entity, Attribute<?, ?> attribute) {
    return XObjectUtils.getAttributeValue(entity, attribute.getJavaMember());
  }

  /**
   * Set attribute value of the entity, the value must be type-compatible. no operation if
   * the entity is null
   * 
   * @param em
   *          the entity manager
   * @param entity
   *          the entity
   * @param attribute
   *          the attribute name
   * @param value
   *          the attribute value to set
   * @throws UnsupportedOperationException
   *           if set attribute value failed
   */
  public static void setAttributeValue(EntityManager em, Object entity, String attribute,
      Object value) {
    if (entity == null) return;
    EntityType<?> type = em.getMetamodel().entity(entity.getClass());
    setAttributeValue(entity, type.getAttribute(attribute), value);
  }

  /**
   * Set attribute value of the entity, the value must be type-compatible. no operation if
   * the entity is null
   * 
   * @param entity
   *          the entity
   * @param attribute
   *          the attribute
   * @param value
   *          the attribute value to set
   * @throws UnsupportedOperationException
   *           if set attribute value failed
   */
  public static void setAttributeValue(Object entity, Attribute<?, ?> attribute, Object value) {
    XObjectUtils.setAttributeValue(entity, attribute.getJavaMember(), value);
  }
}
