/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.apache.drill.exec.physical.config;

import java.util.List;

import org.apache.drill.common.logical.data.NamedExpression;
import org.apache.drill.exec.physical.OperatorCost;
import org.apache.drill.exec.physical.base.AbstractSingle;
import org.apache.drill.exec.physical.base.PhysicalOperator;
import org.apache.drill.exec.physical.base.PhysicalVisitor;
import org.apache.drill.exec.physical.base.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("project")
public class Project extends AbstractSingle{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Project.class);

  private final List<NamedExpression> exprs;
  
  @JsonCreator
  public Project(@JsonProperty("exprs") List<NamedExpression> exprs, @JsonProperty("child") PhysicalOperator child) {
    super(child);
    this.exprs = exprs;
  }

  public List<NamedExpression> getExprs() {
    return exprs;
  }

  @Override
  public <T, X, E extends Throwable> T accept(PhysicalVisitor<T, X, E> physicalVisitor, X value) throws E{
    return physicalVisitor.visitProject(this, value);
  }

  @Override
  public OperatorCost getCost() {
    return new OperatorCost(0, 0, 1000, child.getSize().getRecordCount());
  }
  
  @Override
  public Size getSize() {
    //TODO: This should really change the row width...
    return child.getSize();
  }

  @Override
  protected PhysicalOperator getNewWithChild(PhysicalOperator child) {
    return new Project(exprs, child);
  }
  
  
}
