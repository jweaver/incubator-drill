package org.apache.drill.exec.physical.impl.svremover;

import org.apache.drill.exec.compile.TemplateClassDefinition;
import org.apache.drill.exec.exception.SchemaChangeException;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.record.RecordBatch;
import org.apache.drill.exec.vector.allocator.VectorAllocator;

public interface Copier {
  public static TemplateClassDefinition<Copier> TEMPLATE_DEFINITION2 = new TemplateClassDefinition<Copier>(Copier.class, CopierTemplate2.class);
  public static TemplateClassDefinition<Copier> TEMPLATE_DEFINITION4 = new TemplateClassDefinition<Copier>(Copier.class, CopierTemplate4.class);

  public void setupRemover(FragmentContext context, RecordBatch incoming, RecordBatch outgoing, VectorAllocator[] allocators) throws SchemaChangeException;
  public abstract void copyRecords();


}