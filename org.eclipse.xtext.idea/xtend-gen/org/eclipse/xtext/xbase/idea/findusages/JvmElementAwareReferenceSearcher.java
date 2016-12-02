/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.idea.findusages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.idea.findusages.IReferenceSearcher;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.psi.PsiEObject;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.scoping.featurecalls.OperatorMapping;
import org.eclipse.xtext.xbase.util.PropertyUtil;

/**
 * @author kosyakov - Initial contribution and API
 */
@Singleton
@SuppressWarnings("all")
public class JvmElementAwareReferenceSearcher implements IReferenceSearcher {
  @Inject
  @Extension
  private OperatorMapping _operatorMapping;
  
  @Inject
  @Extension
  private IJvmModelAssociations _iJvmModelAssociations;
  
  @Override
  public void processQuery(final ReferencesSearch.SearchParameters queryParameters, final Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    final Set<String> words = this.collectWords(element);
    boolean _isEmpty = words.isEmpty();
    if (_isEmpty) {
      return;
    }
    final boolean caseSensitive = element.getLanguage().isCaseSensitive();
    for (final String word : words) {
      queryParameters.getOptimizer().searchWord(word, 
        queryParameters.getEffectiveSearchScope(), caseSensitive, element);
    }
  }
  
  protected Set<String> collectWords(final PsiElement element) {
    HashSet<String> _xblockexpression = null;
    {
      final HashSet<String> words = CollectionLiterals.<String>newHashSet();
      final Procedure1<String> _function = (String word) -> {
        this.accept(words, word);
      };
      final Procedure1<String> acceptor = _function;
      if ((element instanceof PsiEObject)) {
        Set<EObject> _jvmElements = this._iJvmModelAssociations.getJvmElements(((PsiEObject)element).getEObject());
        for (final EObject jvmElement : _jvmElements) {
          this.collectWords(jvmElement, acceptor);
        }
      }
      _xblockexpression = words;
    }
    return _xblockexpression;
  }
  
  protected void accept(final Set<String> words, final String word) {
    if ((word != null)) {
      words.add(word);
    }
  }
  
  protected void _collectWords(final EObject jvmElement, final Procedure1<? super String> acceptor) {
  }
  
  protected void _collectWords(final Void jvmElement, final Procedure1<? super String> acceptor) {
  }
  
  protected void _collectWords(final JvmIdentifiableElement jvmElement, final Procedure1<? super String> acceptor) {
    acceptor.apply(jvmElement.getSimpleName());
  }
  
  protected void _collectWords(final JvmFeature jvmElement, final Procedure1<? super String> acceptor) {
    acceptor.apply(jvmElement.getSimpleName());
    final QualifiedName simpleOperator = this._operatorMapping.getOperator(QualifiedName.create(jvmElement.getSimpleName()));
    if ((simpleOperator != null)) {
      acceptor.apply(simpleOperator.toString());
      final QualifiedName compoundOperator = this._operatorMapping.getCompoundOperator(simpleOperator);
      if ((compoundOperator != null)) {
        acceptor.apply(compoundOperator.toString());
      }
    } else {
      acceptor.apply(PropertyUtil.getPropertyName(jvmElement));
    }
  }
  
  protected void collectWords(final EObject jvmElement, final Procedure1<? super String> acceptor) {
    if (jvmElement instanceof JvmFeature
         && acceptor != null) {
      _collectWords((JvmFeature)jvmElement, acceptor);
      return;
    } else if (jvmElement instanceof JvmIdentifiableElement
         && acceptor != null) {
      _collectWords((JvmIdentifiableElement)jvmElement, acceptor);
      return;
    } else if (jvmElement != null
         && acceptor != null) {
      _collectWords(jvmElement, acceptor);
      return;
    } else if (jvmElement == null
         && acceptor != null) {
      _collectWords((Void)null, acceptor);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(jvmElement, acceptor).toString());
    }
  }
}
