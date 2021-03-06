package org.apache.drill.exec.physical.impl.agg;

import static org.junit.Assert.*;
import mockit.Injectable;
import mockit.NonStrictExpectations;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.common.expression.ExpressionPosition;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.util.FileUtils;
import org.apache.drill.exec.expr.fn.FunctionImplementationRegistry;
import org.apache.drill.exec.memory.BufferAllocator;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.physical.PhysicalPlan;
import org.apache.drill.exec.physical.base.FragmentRoot;
import org.apache.drill.exec.physical.impl.ImplCreator;
import org.apache.drill.exec.physical.impl.SimpleRootExec;
import org.apache.drill.exec.planner.PhysicalPlanReader;
import org.apache.drill.exec.proto.CoordinationProtos;
import org.apache.drill.exec.proto.ExecProtos.FragmentHandle;
import org.apache.drill.exec.rpc.user.UserServer.UserClientConnection;
import org.apache.drill.exec.server.DrillbitContext;
import org.apache.drill.exec.vector.BigIntVector;
import org.apache.drill.exec.vector.IntVector;
import org.junit.AfterClass;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.yammer.metrics.MetricRegistry;

public class TestAgg {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestAgg.class);
  DrillConfig c = DrillConfig.create();
  
  private SimpleRootExec doTest(final DrillbitContext bitContext, UserClientConnection connection, String file) throws Exception{
    new NonStrictExpectations(){{
      bitContext.getMetrics(); result = new MetricRegistry("test");
      bitContext.getAllocator(); result = BufferAllocator.getAllocator(c);
    }};
    
    PhysicalPlanReader reader = new PhysicalPlanReader(c, c.getMapper(), CoordinationProtos.DrillbitEndpoint.getDefaultInstance());
    PhysicalPlan plan = reader.readPhysicalPlan(Files.toString(FileUtils.getResourceAsFile(file), Charsets.UTF_8));
    FunctionImplementationRegistry registry = new FunctionImplementationRegistry(c);
    FragmentContext context = new FragmentContext(bitContext, FragmentHandle.getDefaultInstance(), connection, null, registry);
    SimpleRootExec exec = new SimpleRootExec(ImplCreator.getExec(context, (FragmentRoot) plan.getSortedOperators(false).iterator().next()));
    return exec;
  }
  
  @Test
  public void oneKeyAgg(@Injectable final DrillbitContext bitContext, @Injectable UserClientConnection connection) throws Throwable{
    SimpleRootExec exec = doTest(bitContext, connection, "/agg/test1.json");
    
    while(exec.next()){
      BigIntVector cnt = exec.getValueVectorById(new SchemaPath("cnt", ExpressionPosition.UNKNOWN), BigIntVector.class);
      IntVector key = exec.getValueVectorById(new SchemaPath("blue", ExpressionPosition.UNKNOWN), IntVector.class);
      long[] cntArr = {10001, 9999};
      int[] keyArr = {Integer.MIN_VALUE, Integer.MAX_VALUE};
      
      for(int i =0; i < exec.getRecordCount(); i++){
        assertEquals(cntArr[i], cnt.getAccessor().getObject(i));
        assertEquals(keyArr[i], key.getAccessor().getObject(i));
      }
    }
    
    if(exec.getContext().getFailureCause() != null){
      throw exec.getContext().getFailureCause();
    }
    assertTrue(!exec.getContext().isFailed());

  }
  
  @Test
  public void twoKeyAgg(@Injectable final DrillbitContext bitContext, @Injectable UserClientConnection connection) throws Throwable{
    SimpleRootExec exec = doTest(bitContext, connection, "/agg/twokey.json");
    
    while(exec.next()){
      IntVector key1 = exec.getValueVectorById(new SchemaPath("key1", ExpressionPosition.UNKNOWN), IntVector.class);
      BigIntVector key2 = exec.getValueVectorById(new SchemaPath("key2", ExpressionPosition.UNKNOWN), BigIntVector.class);
      BigIntVector cnt = exec.getValueVectorById(new SchemaPath("cnt", ExpressionPosition.UNKNOWN), BigIntVector.class);
      BigIntVector total = exec.getValueVectorById(new SchemaPath("total", ExpressionPosition.UNKNOWN), BigIntVector.class);
      int[] keyArr1 = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
      long[] keyArr2 = {0,1,2,0,1,2};
      long[] cntArr = {34,34,34,34,34,34};
      long[] totalArr = {0,34,68,0,34,68};
      
      for(int i =0; i < exec.getRecordCount(); i++){
//        System.out.print(key1.getAccessor().getObject(i));
//        System.out.print("\t");
//        System.out.print(key2.getAccessor().getObject(i));
//        System.out.print("\t");
//        System.out.print(cnt.getAccessor().getObject(i));
//        System.out.print("\t");
//        System.out.print(total.getAccessor().getObject(i));
//        System.out.println();
        assertEquals(cntArr[i], cnt.getAccessor().getObject(i));
        assertEquals(keyArr1[i], key1.getAccessor().getObject(i));
        assertEquals(keyArr2[i], key2.getAccessor().getObject(i));
        assertEquals(totalArr[i], total.getAccessor().getObject(i));
      }
    }
    
    if(exec.getContext().getFailureCause() != null){
      throw exec.getContext().getFailureCause();
    }
    assertTrue(!exec.getContext().isFailed());

  }
  
  @AfterClass
  public static void tearDown() throws Exception{
    // pause to get logger to catch up.
    Thread.sleep(1000);
  }
}
