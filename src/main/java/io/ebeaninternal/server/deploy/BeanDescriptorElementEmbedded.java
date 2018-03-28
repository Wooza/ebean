package io.ebeaninternal.server.deploy;

import io.ebean.PersistenceIOException;
import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.text.json.ReadJson;
import io.ebeaninternal.server.text.json.SpiJsonWriter;

import java.io.IOException;

/**
 * Bean descriptor used with element collection of list/set of embeddable.
 */
class BeanDescriptorElementEmbedded<T> extends BeanDescriptorElement<T> {

  private final BeanPropertyAssocOne embeddedProperty;

  private final EntityBean prototype;

  private BeanDescriptor targetDescriptor;

  BeanDescriptorElementEmbedded(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy, elementHelp);
    try {
      this.prototype = (EntityBean) beanType.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to create entity bean prototype for "+beanType);
    }
    BeanPropertyAssocOne<?>[] embedded = propertiesEmbedded();
    if (embedded.length == 1) {
      embeddedProperty = embedded[0];
    } else {
      embeddedProperty = null;
    }
  }

  @Override
  public void initialiseOther(BeanDescriptorInitContext initContext) {
    super.initialiseOther(initContext);
    this.targetDescriptor = embeddedProperty.getTargetDescriptor();
  }

  @Override
  public EntityBean createEntityBeanForJson() {
    return (EntityBean)prototype._ebean_newInstance();
  }

  public void bindElementValue(SqlUpdate insert, Object value) {
    targetDescriptor.bindElementValue(insert, value);
  }

  @Override
  public void jsonWriteElement(SpiJsonWriter ctx, Object element) {
    writeJsonElement(ctx, element);
  }

  @Override
  public T jsonRead(ReadJson jsonRead, String path) throws IOException {
    return readJsonElement(jsonRead, path);
  }

  @SuppressWarnings("unchecked")
  T readJsonElement(ReadJson jsonRead, String path) throws IOException {
    return (T)targetDescriptor.jsonRead(jsonRead, path);
  }

  void writeJsonElement(SpiJsonWriter ctx, Object element) {
    try {
      if (element == null) {
        ctx.writeNull();
      } else {
        targetDescriptor.jsonWrite(ctx, (EntityBean)element);
      }
    } catch (IOException e) {
      throw new PersistenceIOException(e);
    }
  }
}