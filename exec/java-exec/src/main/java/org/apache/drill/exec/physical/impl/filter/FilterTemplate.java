package org.apache.drill.exec.physical.impl.filter;

import javax.inject.Named;

import org.apache.drill.exec.exception.SchemaChangeException;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.record.BatchSchema.SelectionVectorMode;
import org.apache.drill.exec.record.RecordBatch;
import org.apache.drill.exec.record.TransferPair;
import org.apache.drill.exec.record.selection.SelectionVector2;

public abstract class FilterTemplate implements Filterer{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FilterTemplate.class);
  
  private SelectionVector2 outgoingSelectionVector;
  private SelectionVector2 incomingSelectionVector;
  private SelectionVectorMode svMode;
  private TransferPair[] transfers;
  
  @Override
  public void setup(FragmentContext context, RecordBatch incoming, RecordBatch outgoing, TransferPair[] transfers) throws SchemaChangeException{
    this.transfers = transfers;
    this.outgoingSelectionVector = outgoing.getSelectionVector2();
    this.svMode = incoming.getSchema().getSelectionVectorMode();
    
    switch(svMode){
    case NONE:
      break;
    case TWO_BYTE:
      this.incomingSelectionVector = incoming.getSelectionVector2();
      break;
    default:
      throw new UnsupportedOperationException();
    }
    doSetup(context, incoming, outgoing);
  }

  private void doTransfers(){
    for(TransferPair t : transfers){
      t.transfer();
    }
  }
  
  public void filterBatch(int recordCount){
    switch(svMode){
    case NONE:
      filterBatchNoSV(recordCount);
      break;
    case TWO_BYTE:
      filterBatchSV2(recordCount);
      break;
    default:
      throw new UnsupportedOperationException();
    }
    doTransfers();
  }
  
  private void filterBatchSV2(int recordCount){
    int svIndex = 0;
    final int count = recordCount;
    for(int i = 0; i < count; i++){
      char index = incomingSelectionVector.getIndex(i);
      if(doEval(i, 0)){
        outgoingSelectionVector.setIndex(svIndex, index);
        svIndex++;
      }
    }
    outgoingSelectionVector.setRecordCount(svIndex);
  }
  
  private void filterBatchNoSV(int recordCount){
    int svIndex = 0;
    for(char i =0; i < recordCount; i++){
      
      if(doEval(i, 0)){
        outgoingSelectionVector.setIndex(svIndex, i);
        svIndex++;
      }
    }
    outgoingSelectionVector.setRecordCount(svIndex);
  }
  
  public abstract void doSetup(@Named("context") FragmentContext context, @Named("incoming") RecordBatch incoming, @Named("outgoing") RecordBatch outgoing);
  public abstract boolean doEval(@Named("inIndex") int inIndex, @Named("outIndex") int outIndex);

}
