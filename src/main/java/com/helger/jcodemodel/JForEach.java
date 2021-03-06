/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright 2013-2014 Philip Helger
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.helger.jcodemodel;

import com.helger.jcodemodel.optimize.ExpressionAccessor;
import com.helger.jcodemodel.optimize.ExpressionCallback;
import com.helger.jcodemodel.optimize.ExpressionContainer;
import com.helger.jcodemodel.optimize.Loop;

import javax.annotation.Nonnull;

/**
 * ForEach Statement This will generate the code for statement based on the new
 * j2se 1.5 j.l.s.
 * 
 * @author Bhakti
 */
public class JForEach implements IJStatement, Loop
{
  private final AbstractJType _type;
  private final String _var;
  private JBlock _body; // lazily created
  private IJExpression _collection;
  private final JVar _loopVar;

  protected JForEach (@Nonnull final AbstractJType vartype,
                      @Nonnull final String variable,
                      @Nonnull final IJExpression collection)
  {
    this._type = vartype;
    this._var = variable;
    this._collection = collection;
    _loopVar = new JVar (JMods.forVar (JMod.FINAL), _type, _var, collection);
  }

  @Nonnull
  public AbstractJType type ()
  {
    return _type;
  }

  /**
   * Returns a reference to the loop variable.
   */
  @Nonnull
  public JVar var ()
  {
    return _loopVar;
  }

  @Nonnull
  public IJExpression collection ()
  {
    return _collection;
  }

  public ExpressionContainer statementsExecutedOnce ()
  {
    return new ExpressionContainer ()
    {
      public boolean forAllSubExpressions (ExpressionCallback callback)
      {
        return AbstractJExpressionImpl.visitWithSubExpressions (callback,
            new ExpressionAccessor ()
        {
          public void set (IJExpression newExpression)
          {
            _collection = newExpression;
          }

          public IJExpression get ()
          {
            return _collection;
          }
        });
      }
    };
  }

  public ExpressionContainer statementsExecutedOnEachIteration ()
  {
    return new ExpressionContainer ()
    {
      public boolean forAllSubExpressions (ExpressionCallback callback)
      {
        return callback.visitAssignmentTarget (_loopVar);
      }
    };
  }

  @Nonnull
  public JBlock body ()
  {
    if (_body == null)
      _body = new JBlock ();
    return _body;
  }

  public void state (@Nonnull final JFormatter f)
  {
    f.print ("for (");
    f.generable (_type).id (_var).print (": ").generable (_collection);
    f.print (')');
    if (_body != null)
      f.generable (_body).newline ();
    else
      f.print (';').newline ();
  }
}
